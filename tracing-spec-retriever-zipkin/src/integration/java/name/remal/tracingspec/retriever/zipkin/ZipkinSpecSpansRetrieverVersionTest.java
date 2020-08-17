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

package name.remal.tracingspec.retriever.zipkin;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.testcontainers.images.PullPolicy.ageBased;

import brave.Tracer;
import brave.Tracing;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.test.container.ZipkinContainer;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.brave.ZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@SuppressWarnings({"java:S1450", "java:S109"})
class ZipkinSpecSpansRetrieverVersionTest {

    private static final String SERVICE_NAME = "service-name";

    private final ZipkinContainer zipkinContainer = new ZipkinContainer()
        .withImagePullPolicy(ageBased(Duration.ofHours(1)));

    private Tracer tracer;

    private ZipkinSpecSpansRetriever retriever;

    @BeforeEach
    void beforeEach() {
        zipkinContainer.start();

        val sender = URLConnectionSender.create(zipkinContainer.getZipkinCollectorUrl());
        val reporter = AsyncReporter.builder(sender)
            .messageTimeout(1, MILLISECONDS)
            .build();
        val spanHandler = ZipkinSpanHandler.create(reporter);
        val tracing = Tracing.newBuilder()
            .addSpanHandler(spanHandler)
            .localServiceName(SERVICE_NAME)
            .build();
        tracer = tracing.tracer();

        val retrieverProperties = new ZipkinSpecSpansRetrieverProperties();
        retrieverProperties.setUrl(format("http://localhost:%d/", zipkinContainer.getZipkinPort()));
        retrieverProperties.setConnectTimeoutMillis(1_000);
        retrieverProperties.setWriteTimeoutMillis(1_000);
        retrieverProperties.setReadTimeoutMillis(5_000);
        retriever = new ZipkinSpecSpansRetriever(retrieverProperties);
    }

    @AfterEach
    void afterEach() {
        zipkinContainer.stop();
    }


    @Test
    void test() {
        final long start;
        {
            val now = Instant.now();
            start = MICROSECONDS.convert(now.getEpochSecond(), SECONDS)
                + MICROSECONDS.convert(now.getNano(), NANOSECONDS);
        }
        val rootSpan = tracer.newTrace()
            .name("root")
            .start(start);
        val childSpan = tracer.newChild(rootSpan.context())
            .name("child")
            .tag("spec.description", "some text")
            .tag("spec.is-async", "true")
            .start(start + 5);
        childSpan.finish(start + 8);
        rootSpan.finish(start + 11);

        val traceId = rootSpan.context().traceIdString();
        List<SpecSpan> specSpans = new ArrayList<>();
        await().atMost(Duration.ofSeconds(30)).until(
            () -> {
                specSpans.clear();
                specSpans.addAll(retriever.retrieveSpecSpansForTrace(traceId));
                return specSpans;
            },
            hasSize(2)
        );

        assertThat(specSpans, containsInAnyOrder(
            SpecSpan.builder()
                .spanId(rootSpan.context().spanIdString())
                .name("root")
                .serviceName(SERVICE_NAME)
                .startedAt(Instant.ofEpochSecond(
                    0,
                    NANOSECONDS.convert(start, MICROSECONDS)
                ))
                .build(),

            SpecSpan.builder()
                .spanId(childSpan.context().spanIdString())
                .parentSpanId(rootSpan.context().spanIdString())
                .name("child")
                .serviceName(SERVICE_NAME)
                .startedAt(Instant.ofEpochSecond(
                    0,
                    NANOSECONDS.convert(start + 5, MICROSECONDS)
                ))
                .description("some text")
                .async(true)
                .build()
        ));
    }

}
