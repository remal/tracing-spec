package test;

import static java.lang.Math.ceil;
import static java.lang.Math.round;
import static java.lang.Math.toIntExact;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.walkFileTree;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static utils.test.awaitility.AwaitilityUtils.await;
import static utils.test.debug.TestDebug.AWAIT_TIMEOUT;
import static utils.test.normalizer.PumlNormalizer.normalizePuml;
import static utils.test.resource.Resources.getResourcePath;
import static utils.test.resource.Resources.readTextResource;

import apps.documents.DocumentsApplication;
import apps.documents.DocumentsClient;
import apps.documents.ImmutableDocument;
import apps.documents.ImmutableDocumentId;
import apps.schemas.ImmutableSchema;
import apps.schemas.ImmutableSchemaReference;
import apps.schemas.SchemasApplication;
import apps.schemas.SchemasClient;
import apps.users.UsersApplication;
import brave.Tracer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.io.Flushable;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.application.TracingSpecSpringApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.ParentContextApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import shared.SharedConfiguration;
import utils.test.container.JaegerAllInOneContainer;
import utils.test.container.ZipkinContainer;
import zipkin2.reporter.Reporter;

class EndToEndTest {

    private static final Logger logger = LogManager.getLogger(EndToEndTest.class);

    @Test
    void end_to_end(@TempDir Path tempDir) {
        // Kafka listeners initialize asynchronously, so let's do several attempts:
        attempt(attempt -> endToEndImpl(tempDir, attempt));
    }

    private void endToEndImpl(Path tempDir, int attempt) {
        cleanTempDir(tempDir);

        // Create initial data:
        val initialSchema = ImmutableSchema.builder()
            .id("task" + attempt)
            .references(singletonList(
                ImmutableSchemaReference.builder()
                    .dataType("user")
                    .idField("userId")
                    .putFieldMapping("email", "userEmail")
                    .build()
            ))
            .build();
        val schemasClient = sharedContext.getBean(SchemasClient.class);
        schemasClient.saveSchema(initialSchema);

        val initialDocument = ImmutableDocument.builder()
            .id(ImmutableDocumentId.builder()
                .schema(initialSchema.getId())
                .key(1)
                .build()
            )
            .content(JsonNodeFactory.instance.objectNode()
                .put("userId", attempt)
            )
            .build();
        val documentsClient = sharedContext.getBean(DocumentsClient.class);
        documentsClient.saveDocument(initialDocument);


        // Update schema:
        val tracer = getBeanFromAnyContext(Tracer.class);
        val testSpan = tracer.startScopedSpan("test");
        val traceId = testSpan.context().traceIdString();
        try {
            val updatedSchema = ImmutableSchema.builder().from(initialSchema)
                .references(singletonList(
                    ImmutableSchemaReference.builder().from(initialSchema.getReferences().get(0))
                        .putFieldMapping("fullName", "userFullName")
                        .build()
                ))
                .build();
            schemasClient.saveSchema(updatedSchema);
        } finally {
            testSpan.finish();
        }

        // Let's wait until all documents are processed:
        await().atMost(AWAIT_TIMEOUT).until(
            () -> documentsClient.getAllDocumentsBySchema(initialSchema.getId()),
            docs -> docs.size() == 1 && !docs.get(0).equals(initialDocument)
        );

        flushAllSpans();


        // Do tests:
        val expectedGraphPath = getResourcePath("expected.yml");
        long matchAttemptsDelayMillis = 2_500;
        int matchAttempts = toIntExact(round(ceil(15_000.0 / matchAttemptsDelayMillis)));

        val expectedDiagram = normalizePuml(readTextResource("expected.puml"));
        {
            val zipkinUrl = sharedContext.getBean(ZipkinContainer.class).getQueryApiUrl();
            assertDoesNotThrow(
                (Executable) () ->
                    TracingSpecSpringApplication.run(
                        "match",
                        "--spring.application.name=zipkin",
                        "--spring.sleuth.enabled=false",
                        "--tracingspec.retriever.zipkin.url=" + zipkinUrl,
                        "--attempts=" + matchAttempts,
                        "--attempts-delay=" + matchAttemptsDelayMillis,
                        traceId,
                        expectedGraphPath.toString()
                    ),
                "Zipkin match"
            );

            val outputPath = tempDir.resolve("dir/zipkin.puml");
            await().untilAsserted(() -> {
                TracingSpecSpringApplication.run(
                    "render-trace",
                    "--spring.application.name=zipkin",
                    "--spring.sleuth.enabled=false",
                    "--tracingspec.retriever.zipkin.url=" + zipkinUrl,
                    traceId,
                    "plantuml-sequence",
                    outputPath.toString()
                );

                val diagramBytes = readAllBytes(outputPath);
                val diagram = new String(diagramBytes, UTF_8);
                val normalizedDiagram = normalizePuml(diagram);
                assertThat("Zipkin diagram", normalizedDiagram, equalTo(expectedDiagram));
            });
        }

        {
            val jaegerUrl = sharedContext.getBean(JaegerAllInOneContainer.class).getQueryApiUrl();
            assertDoesNotThrow(
                (Executable) () ->
                    TracingSpecSpringApplication.run(
                        "match",
                        "--spring.application.name=jaeger",
                        "--spring.sleuth.enabled=false",
                        "--tracingspec.retriever.jaeger.url=" + jaegerUrl,
                        "--attempts=" + matchAttempts,
                        "--attempts-delay=" + matchAttemptsDelayMillis,
                        traceId,
                        expectedGraphPath.toString()
                    ),
                "Jaeger match"
            );

            val outputPath = tempDir.resolve("dir/jaeger.puml");
            await().untilAsserted(() -> {
                TracingSpecSpringApplication.run(
                    "render-trace",
                    "--spring.application.name=jaeger",
                    "--spring.sleuth.enabled=false",
                    "--tracingspec.retriever.jaeger.url=" + jaegerUrl,
                    traceId,
                    "plantuml-sequence",
                    outputPath.toString()
                );

                val diagramBytes = readAllBytes(outputPath);
                val diagram = new String(diagramBytes, UTF_8);
                val normalizedDiagram = normalizePuml(diagram);
                assertThat("Jaeger diagram", normalizedDiagram, equalTo(expectedDiagram));
            });
        }
    }

    private static ConfigurableApplicationContext getAnyContext() {
        return applicationContexts.values().iterator().next();
    }

    private static <T> T getBeanFromAnyContext(Class<T> type) {
        val context = getAnyContext();
        return context.getBean(type);
    }

    @SneakyThrows
    @SuppressWarnings("java:S2925")
    private static void flushAllSpans() {
        boolean isSomethingFlushed = false;
        for (val context : applicationContexts.values()) {
            for (val reporter : context.getBeansOfType(Reporter.class).values()) {
                if (reporter instanceof Flushable) {
                    ((Flushable) reporter).flush();
                    isSomethingFlushed = true;
                }
            }
        }

        if (isSomethingFlushed) {
            // Give spans collectors some time to process our spans
            Thread.sleep(1_000);
        }
    }

    private static final AnnotationConfigApplicationContext sharedContext = new AnnotationConfigApplicationContext();

    private static final Map<Class<?>, ConfigurableApplicationContext> applicationContexts = new ConcurrentHashMap<>();

    @BeforeAll
    static void startApplications() {
        sharedContext.register(SharedConfiguration.class);
        sharedContext.refresh();
        sharedContext.start();

        asList(
            UsersApplication.class,
            DocumentsApplication.class,
            SchemasApplication.class
        ).parallelStream().forEach(applicationClass -> {
            val application = new SpringApplication(applicationClass);
            application.setRegisterShutdownHook(false);
            application.addInitializers(new ParentContextApplicationContextInitializer(sharedContext));

            val applicationContext = application.run();
            applicationContexts.put(applicationClass, applicationContext);
        });
    }

    @AfterAll
    static void stopApplications() {
        applicationContexts.values().forEach(EndToEndTest::closeContext);
        closeContext(sharedContext);
    }

    private static void closeContext(ConfigurableApplicationContext context) {
        if (context.isRunning()) {
            context.stop();
        }

        if (context.isActive()) {
            context.close();
        }
    }

    @SneakyThrows
    private static void cleanTempDir(Path tempDir) {
        if (!exists(tempDir)) {
            return;
        }

        walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.equals(tempDir)) {
                    deleteIfExists(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, @Nullable IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                if (!dir.equals(tempDir)) {
                    deleteIfExists(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void attempt(AttemptAction executable) {
        attempt(executable, 3);
    }

    @SneakyThrows
    @SuppressWarnings({"BusyWait", "java:S2925"})
    private static void attempt(AttemptAction executable, int attempts) {
        if (attempts < 1) {
            throw new IllegalArgumentException("attempts must be greater or equals to 1");
        }

        int attempt = 0;
        while (true) {
            ++attempt;

            try {
                executable.execute(attempt);
                return;

            } catch (Throwable exception) {
                if (attempt >= attempts) {
                    throw exception;
                } else {
                    long sleepMillis = 5_000;
                    logger.warn(
                        "Attempt {}/{} failed, waiting {} millis and try again",
                        attempt,
                        attempts,
                        sleepMillis
                    );
                    Thread.sleep(sleepMillis);
                }
            }
        }
    }

    @FunctionalInterface
    private interface AttemptAction {
        void execute(int attempt) throws Throwable;
    }

}
