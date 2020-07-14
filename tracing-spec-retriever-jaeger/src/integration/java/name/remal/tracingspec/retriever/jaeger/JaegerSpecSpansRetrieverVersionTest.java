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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
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
import java.util.Optional;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpanKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.container.JaegerAllInOneContainer;

@SuppressWarnings({"java:S1450", "java:S109"})
public class JaegerSpecSpansRetrieverVersionTest {

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
            .withTag("string-tag", "string")
            .withTag("number-tag", 47)
            .withTag("boolean-tag", true)
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
            allOf(
                hasProperty("spanKey", equalTo(SpecSpanKey.builder()
                    .traceId(traceId)
                    .spanId(rootSpan.context().toSpanId())
                    .build()
                )),
                hasProperty("name", equalTo(Optional.of("root"))),
                hasProperty("serviceName", equalTo(Optional.of(SERVICE_NAME))),
                hasProperty("parentSpanKey", equalTo(Optional.empty())),
                hasProperty("startedAt", equalTo(Optional.of(Instant.ofEpochSecond(
                    0,
                    NANOSECONDS.convert(rootSpan.getStart(), MICROSECONDS)
                )))),
                hasProperty("duration", equalTo(Optional.of(Duration.ofSeconds(
                    0,
                    NANOSECONDS.convert(rootSpan.getDuration(), MICROSECONDS)
                ))))
            ),

            allOf(
                hasProperty("spanKey", equalTo(SpecSpanKey.builder()
                    .traceId(traceId)
                    .spanId(childSpan.context().toSpanId())
                    .build()
                )),
                hasProperty("name", equalTo(Optional.of("child"))),
                hasProperty("serviceName", equalTo(Optional.of(SERVICE_NAME))),
                hasProperty("parentSpanKey", equalTo(Optional.of(SpecSpanKey.builder()
                    .traceId(traceId)
                    .spanId(rootSpan.context().toSpanId())
                    .build()
                ))),
                hasProperty("startedAt", equalTo(Optional.of(Instant.ofEpochSecond(
                    0,
                    NANOSECONDS.convert(childSpan.getStart(), MICROSECONDS)
                )))),
                hasProperty("duration", equalTo(Optional.of(Duration.ofSeconds(
                    0,
                    NANOSECONDS.convert(childSpan.getDuration(), MICROSECONDS)
                )))),
                hasProperty("tags", allOf(
                    hasEntry("string-tag", "string"),
                    hasEntry("number-tag", "47"),
                    hasEntry("boolean-tag", "true")
                ))
            )
        ));
    }

}
