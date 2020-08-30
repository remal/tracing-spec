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

import static java.util.Arrays.asList;
import static name.remal.tracingspec.model.SpecSpansGraphs.createSpecSpansGraph;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;

import apps.documents.DocumentsApplication;
import apps.documents.DocumentsClient;
import apps.schemas.ImmutableSchema;
import apps.schemas.ImmutableSchemaReference;
import apps.schemas.SchemasApplication;
import apps.schemas.SchemasClient;
import apps.users.UsersApplication;
import brave.Tracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Flushable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.val;
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

        val tracer = applicationContexts.get(SchemasApplication.class).getBean(Tracer.class);
        val testSpan = tracer.startScopedSpan("test");
        try {
            val schemasClient = sharedContext.getBean(SchemasClient.class);
            schemasClient.saveSchema(schema);
        } catch (Throwable e) {
            testSpan.error(e);
            throw e;
        } finally {
            testSpan.finish();
        }

        await().atMost(Duration.ofSeconds(60)).until(
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
        logger.info("OLD DOCUMENTS: {}", oldSchemaDocuments);
        logger.info("NEW DOCUMENTS: {}", newSchemaDocuments);

        val objectMapper = applicationContexts.values().iterator().next().getBean(ObjectMapper.class);

        {
            val retrieverProperties = new JaegerSpecSpansRetrieverProperties();
            retrieverProperties.setHost("localhost");
            retrieverProperties.setPort(sharedContext.getBean(JaegerAllInOneContainer.class).getQueryPort());
            val retriever = new JaegerSpecSpansRetriever(retrieverProperties);
            val specSpans = retriever.retrieveSpecSpansForTrace(testSpan.context().traceIdString());
            val graph = createSpecSpansGraph(specSpans);
            logger.info("Jaeger graph: {}", graph);
            val graphJson = objectMapper.writeValueAsString(graph);
            logger.info("Jaeger graph JSON: {}", graphJson);
        }

        {
            val retrieverProperties = new ZipkinSpecSpansRetrieverProperties();
            retrieverProperties.setUrl(sharedContext.getBean(ZipkinContainer.class).getZipkinBaseUrl());
            val retriever = new ZipkinSpecSpansRetriever(retrieverProperties);
            val specSpans = retriever.retrieveSpecSpansForTrace(testSpan.context().traceIdString());
            val graph = createSpecSpansGraph(specSpans);
            logger.info("Zipkin graph: {}", graph);
            val graphJson = objectMapper.writeValueAsString(graph);
            logger.info("Zipkin graph JSON: {}", graphJson);
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
