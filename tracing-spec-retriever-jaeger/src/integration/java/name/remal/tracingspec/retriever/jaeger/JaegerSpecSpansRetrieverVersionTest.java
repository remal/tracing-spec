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

package name.remal.tracingspec.retriever.jaeger;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.testcontainers.images.PullPolicy.ageBased;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.thrift.internal.senders.HttpSender;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.test.container.JaegerAllInOneContainer;

@SuppressWarnings({"java:S1450", "java:S109"})
class JaegerSpecSpansRetrieverVersionTest {

    private static final String SERVICE_NAME = "service-name";

    private final JaegerAllInOneContainer jaegerContainer = new JaegerAllInOneContainer()
        .withImagePullPolicy(ageBased(Duration.ofHours(1)));

    private JaegerTracer tracer;

    private JaegerSpecSpansRetriever retriever;

    @BeforeEach
    void beforeEach() {
        jaegerContainer.start();

        val endpoint = format("http://localhost:%d/api/traces", jaegerContainer.getCollectorThriftPort());
        val sender = new HttpSender.Builder(endpoint)
            .build();
        val reporter = new RemoteReporter.Builder()
            .withSender(sender)
            .withFlushInterval(1)
            .build();
        tracer = new JaegerTracer.Builder(SERVICE_NAME)
            .withSampler(new ConstSampler(true))
            .withReporter(reporter)
            .build();

        val retrieverProperties = new JaegerSpecSpansRetrieverProperties();
        retrieverProperties.setHost("localhost");
        retrieverProperties.setPort(jaegerContainer.getQueryPort());
        retrieverProperties.setTimeoutMillis(5_000);
        retriever = new JaegerSpecSpansRetriever(retrieverProperties);
    }

    @AfterEach
    void afterEach() {
        tracer.close();
        jaegerContainer.stop();
    }


    @Test
    void test() {
        val rootSpan = tracer.buildSpan("root").start();
        val childSpan = tracer.buildSpan("child").asChildOf(rootSpan)
            .withTag("spec.description", "some text")
            .withTag("spec.is-async", true)
            .start();
        childSpan.finish();
        rootSpan.finish();

        val traceId = rootSpan.context().getTraceId();
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
                .spanId(rootSpan.context().toSpanId())
                .name("root")
                .serviceName(SERVICE_NAME)
                .startedAt(Instant.ofEpochSecond(
                    0,
                    NANOSECONDS.convert(rootSpan.getStart(), MICROSECONDS)
                ))
                .duration(Duration.ofSeconds(
                    0,
                    NANOSECONDS.convert(rootSpan.getDuration(), MICROSECONDS)
                ))
                .build(),

            SpecSpan.builder()
                .spanId(childSpan.context().toSpanId())
                .parentSpanId(rootSpan.context().toSpanId())
                .name("child")
                .serviceName(SERVICE_NAME)
                .startedAt(Instant.ofEpochSecond(
                    0,
                    NANOSECONDS.convert(childSpan.getStart(), MICROSECONDS)
                ))
                .duration(Duration.ofSeconds(
                    0,
                    NANOSECONDS.convert(childSpan.getDuration(), MICROSECONDS)
                ))
                .description("some text")
                .async(true)
                .build()
        ));
    }

}
