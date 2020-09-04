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

import static apps.common.TraceIdWebFilter.TRACE_ID_HTTP_HEADER;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static utils.test.normalizer.PumlNormalizer.normalizePuml;
import static utils.test.resource.Resources.readTextResource;

import apps.documents.DocumentsApplication;
import apps.documents.DocumentsClient;
import apps.schemas.ImmutableSchema;
import apps.schemas.ImmutableSchemaReference;
import apps.schemas.SchemasApplication;
import apps.schemas.SchemasClient;
import apps.users.UsersApplication;
import java.io.Flushable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.renderer.nodeprocessor.AppendServerToClientNodeProcessor;
import name.remal.tracingspec.renderer.nodeprocessor.KafkaRemoteServiceNameNodeProcessor;
import name.remal.tracingspec.renderer.plantuml.sequence.TracingSpecPlantumlSequenceRenderer;
import name.remal.tracingspec.retriever.jaeger.JaegerSpecSpansRetriever;
import name.remal.tracingspec.retriever.jaeger.JaegerSpecSpansRetrieverProperties;
import name.remal.tracingspec.retriever.zipkin.ZipkinSpecSpansRetriever;
import name.remal.tracingspec.retriever.zipkin.ZipkinSpecSpansRetrieverProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.ParentContextApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import shared.SharedConfiguration;
import utils.test.container.JaegerAllInOneContainer;
import utils.test.container.ZipkinContainer;
import zipkin2.reporter.Reporter;

class EndToEndTest {

    private static final boolean IS_DEBUG = getRuntimeMXBean().getInputArguments().toString().contains("jdwp");
    private static final Duration AWAIT_TIMEOUT = IS_DEBUG ? Duration.ofHours(1) : Duration.ofSeconds(30);

    private static final Logger logger = LogManager.getLogger(EndToEndTest.class);

    @Test
    void test() throws Throwable {
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

        val schemasClient = sharedContext.getBean(SchemasClient.class);
        final String traceId;
        {
            val response = schemasClient.saveSchema(schema);
            traceId = requireNonNull(response.getHeaders().getFirst(TRACE_ID_HTTP_HEADER));
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

        val renderer = new TracingSpecPlantumlSequenceRenderer();
        renderer.addNodeProcessor(new AppendServerToClientNodeProcessor());
        renderer.addNodeProcessor(new KafkaRemoteServiceNameNodeProcessor());
        renderer.addTagToDisplay("kafka.topic");

        int expectedSpansCount = 12;
        val expectedDiagram = normalizePuml(readTextResource("expected.puml"));

        {
            val retrieverProperties = new ZipkinSpecSpansRetrieverProperties();
            retrieverProperties.setUrl(sharedContext.getBean(ZipkinContainer.class).getZipkinBaseUrl());
            val retriever = new ZipkinSpecSpansRetriever(retrieverProperties);

            List<SpecSpan> specSpans = new ArrayList<>();
            await().atMost(AWAIT_TIMEOUT).ignoreExceptions().until(() -> {
                specSpans.addAll(retriever.retrieveSpecSpansForTrace(traceId));
                if (specSpans.size() < expectedSpansCount) {
                    specSpans.clear();
                }
                return !specSpans.isEmpty();
            });

            val diagram = renderer.renderTracingSpec(specSpans);
            val normalizedDiagram = normalizePuml(diagram);
            assertThat("Zipkin", normalizedDiagram, equalTo(expectedDiagram));
        }

        {
            val retrieverProperties = new JaegerSpecSpansRetrieverProperties();
            retrieverProperties.setHost("localhost");
            retrieverProperties.setPort(sharedContext.getBean(JaegerAllInOneContainer.class).getQueryPort());
            val retriever = new JaegerSpecSpansRetriever(retrieverProperties);

            List<SpecSpan> specSpans = new ArrayList<>();
            await().atMost(AWAIT_TIMEOUT).ignoreExceptions().until(() -> {
                specSpans.addAll(retriever.retrieveSpecSpansForTrace(traceId));
                if (specSpans.size() < expectedSpansCount) {
                    specSpans.clear();
                }
                return !specSpans.isEmpty();
            });

            val diagram = renderer.renderTracingSpec(specSpans);
            val normalizedDiagram = normalizePuml(diagram);
            assertThat("Jaeger", normalizedDiagram, equalTo(expectedDiagram));
        }
    }

    private static final AnnotationConfigApplicationContext sharedContext = new AnnotationConfigApplicationContext();

    private static final Map<Class<?>, ConfigurableApplicationContext> applicationContexts = new HashMap<>();

    @BeforeAll
    static void startApplications() {
        sharedContext.register(SharedConfiguration.class);
        sharedContext.refresh();
        sharedContext.start();

        asList(
            UsersApplication.class,
            DocumentsApplication.class,
            SchemasApplication.class
        ).forEach(applicationClass -> {
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
