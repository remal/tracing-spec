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

import static java.lang.System.identityHashCode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static utils.test.debug.TestDebug.AWAIT_TIMEOUT;
import static utils.test.normalizer.PumlNormalizer.normalizePuml;
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
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.val;
import name.remal.tracingspec.application.TracingSpecSpringApplication;
import name.remal.tracingspec.retriever.jaeger.JaegerSpecSpansRetriever;
import name.remal.tracingspec.retriever.jaeger.JaegerSpecSpansRetrieverProperties;
import name.remal.tracingspec.retriever.zipkin.ZipkinSpecSpansRetriever;
import name.remal.tracingspec.retriever.zipkin.ZipkinSpecSpansRetrieverProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

    @Test
    void test(@TempDir Path tempDir) throws Throwable {
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

        val tracer = getBeanFromAnyContext(Tracer.class);
        val testSpan = tracer.startScopedSpan("test");
        val traceId = testSpan.context().traceIdString();
        try {
            val schemasClient = sharedContext.getBean(SchemasClient.class);
            schemasClient.saveSchema(schema);
        } finally {
            testSpan.finish();
        }

        await().atMost(AWAIT_TIMEOUT).until(
            () -> documentsClient.getAllDocumentsBySchema(schema.getId()),
            docs -> docs.stream().noneMatch(oldSchemaDocuments::contains)
        );

        for (val context : applicationContexts.values()) {
            for (val reporter : context.getBeansOfType(Reporter.class).values()) {
                if (reporter instanceof Flushable) {
                    ((Flushable) reporter).flush();
                }
            }
        }

        val newSchemaDocuments = documentsClient.getAllDocumentsBySchema(schema.getId());
        assertThat(newSchemaDocuments, not(empty()));

        int expectedSpansCount = 19;
        val expectedDiagram = normalizePuml(readTextResource("expected.puml"));

        {
            val retrieverProperties = applyBeanPostProcessors(
                new ZipkinSpecSpansRetrieverProperties()
            );
            retrieverProperties.setUrl(sharedContext.getBean(ZipkinContainer.class).getZipkinBaseUrl());
            await().atMost(AWAIT_TIMEOUT).ignoreExceptions().until(() -> {
                val retriever = new ZipkinSpecSpansRetriever(retrieverProperties);
                val spans = retriever.retrieveSpecSpansForTrace(traceId);
                return spans.size() >= expectedSpansCount;
            });

            val outputPath = tempDir.resolve("dir/zipkin.puml");
            TracingSpecSpringApplication.main(
                "--spring.sleuth.enabled=false",
                "--tracingspec.retriever.zipkin.url=" + retrieverProperties.getUrl(),
                traceId,
                "plantuml-sequence",
                outputPath.toString()
            );

            val diagramBytes = readAllBytes(outputPath);
            val diagram = new String(diagramBytes, UTF_8);
            val normalizedDiagram = normalizePuml(diagram);
            assertThat("Zipkin", normalizedDiagram, equalTo(expectedDiagram));
        }

        {
            val retrieverProperties = applyBeanPostProcessors(
                new JaegerSpecSpansRetrieverProperties()
            );
            retrieverProperties.setHost("localhost");
            retrieverProperties.setPort(sharedContext.getBean(JaegerAllInOneContainer.class).getQueryPort());
            await().atMost(AWAIT_TIMEOUT).ignoreExceptions().until(() -> {
                val retriever = new JaegerSpecSpansRetriever(retrieverProperties);
                val spans = retriever.retrieveSpecSpansForTrace(traceId);
                return spans.size() >= expectedSpansCount;
            });

            val outputPath = tempDir.resolve("dir/jaeger.puml");
            TracingSpecSpringApplication.main(
                "--spring.sleuth.enabled=false",
                "--tracingspec.retriever.jaeger.host=" + retrieverProperties.getHost(),
                "--tracingspec.retriever.jaeger.port=" + retrieverProperties.getPort(),
                traceId,
                "plantuml-sequence",
                outputPath.toString()
            );

            val diagramBytes = readAllBytes(outputPath);
            val diagram = new String(diagramBytes, UTF_8);
            val normalizedDiagram = normalizePuml(diagram);
            assertThat("Jaeger", normalizedDiagram, equalTo(expectedDiagram));
        }
    }

    private static ConfigurableApplicationContext getAnyContext() {
        return applicationContexts.values().iterator().next();
    }

    private static <T> T getBeanFromAnyContext(Class<T> type) {
        val context = getAnyContext();
        return context.getBean(type);
    }

    @SuppressWarnings("unchecked")
    private static <T> T applyBeanPostProcessors(T object) {
        val beanFactory = getAnyContext().getAutowireCapableBeanFactory();
        val beanName = object.getClass().getName() + '$' + identityHashCode(object);
        object = (T) beanFactory.applyBeanPostProcessorsBeforeInitialization(object, beanName);
        object = (T) beanFactory.applyBeanPostProcessorsAfterInitialization(object, beanName);
        return object;
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

}
