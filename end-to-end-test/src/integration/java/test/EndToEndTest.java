/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import static java.util.Objects.requireNonNull;
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
import apps.schemas.ImmutableSchema;
import apps.schemas.ImmutableSchemaReference;
import apps.schemas.SchemasApplication;
import apps.schemas.SchemasClient;
import apps.users.UsersApplication;
import brave.Tracer;
import java.io.Flushable;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
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
        attempt(() -> endToEndImpl(tempDir));
    }

    private void endToEndImpl(Path tempDir) {
        cleanTempDir(tempDir);

        val schema = ImmutableSchema.builder()
            .id("task")
            .addReference(ImmutableSchemaReference.builder()
                .dataType("user")
                .idField("userId")
                .putFieldMapping("fullName", "userFullName")
                .putFieldMapping("email", "userEmail")
                .build()
            )
            .build();

        val documentsClient = sharedContext.getBean(DocumentsClient.class);
        val oldSchemaDocuments = documentsClient.getAllDocumentsBySchema(schema.getId());

        val traceId = new AtomicReference<String>();
        // Kafka listeners initialize asynchronously, so let's do several attempts:
        await().atMost(AWAIT_TIMEOUT.multipliedBy(2)).untilAsserted(() -> {
            val tracer = getBeanFromAnyContext(Tracer.class);
            val testSpan = tracer.startScopedSpan("test");
            traceId.set(testSpan.context().traceIdString());
            try {
                val schemasClient = sharedContext.getBean(SchemasClient.class);
                schemasClient.saveSchema(schema);
            } finally {
                testSpan.finish();
            }

            // Let's wait until all documents are processed:
            await().atMost(AWAIT_TIMEOUT.dividedBy(5)).until(
                () -> documentsClient.getAllDocumentsBySchema(schema.getId()),
                docs -> docs.stream().noneMatch(oldSchemaDocuments::contains)
            );
        });

        flushAllSpans();


        val zipkinUrl = sharedContext.getBean(ZipkinContainer.class).getZipkinBaseUrl();
        val jaegerPort = sharedContext.getBean(JaegerAllInOneContainer.class).getQueryPort();

        val expectedGraphPath = getResourcePath("expected.yml");
        long matchAttemptsDelayMillis = 1_000;
        int matchAttempts = toIntExact(round(ceil(60_000.0 / matchAttemptsDelayMillis)));

        val expectedDiagram = normalizePuml(readTextResource("expected.puml"));
        {
            assertDoesNotThrow(
                (Executable) () ->
                    TracingSpecSpringApplication.run(
                        "match",
                        "--spring.application.name=zipkin",
                        "--spring.sleuth.enabled=false",
                        "--tracingspec.retriever.zipkin.url=" + zipkinUrl,
                        "--attempts=" + matchAttempts,
                        "--attempts-delay=" + matchAttemptsDelayMillis,
                        requireNonNull(traceId.get()),
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
                    requireNonNull(traceId.get()),
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
            assertDoesNotThrow(
                (Executable) () ->
                    TracingSpecSpringApplication.run(
                        "match",
                        "--spring.application.name=jaeger",
                        "--spring.sleuth.enabled=false",
                        "--tracingspec.retriever.jaeger.host=localhost",
                        "--tracingspec.retriever.jaeger.port=" + jaegerPort,
                        "--attempts=" + matchAttempts,
                        "--attempts-delay=" + matchAttemptsDelayMillis,
                        requireNonNull(traceId.get()),
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
                    "--tracingspec.retriever.jaeger.host=localhost",
                    "--tracingspec.retriever.jaeger.port=" + jaegerPort,
                    requireNonNull(traceId.get()),
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

    private static void attempt(Executable executable) {
        attempt(executable, 3);
    }

    @SneakyThrows
    @SuppressWarnings({"BusyWait", "java:S2925"})
    private static void attempt(Executable executable, int attempts) {
        int attempt = 0;
        while (true) {
            ++attempt;

            try {
                executable.execute();
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

}
