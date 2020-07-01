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
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.thrift.internal.senders.HttpSender;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S1450")
public class JaegerRetrieverVersionTest {

    private static final String SERVICE_NAME = "service-name";

    private final JaegerAllInOneContainer jaegerContainer = new JaegerAllInOneContainer();

    private JaegerTracer tracer;

    private JaegerRetriever retriever;

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

        retriever = new JaegerRetriever(InetSocketAddress.createUnresolved(
            "localhost",
            jaegerContainer.getQueryPort()
        ));
    }

    @AfterEach
    void afterEach() {
        tracer.close();
        jaegerContainer.stop();
    }


    @Test
    void test() {
        val rootSpan = tracer.buildSpan("root").start();
        val childSpan = tracer.buildSpan("child").asChildOf(rootSpan).start();
        childSpan.finish();
        rootSpan.finish();

        val traceId = rootSpan.context().getTraceId();
        await().atMost(Duration.ofDays(1)).until(() -> retriever.retrieveSpansForTrace(traceId), containsInAnyOrder(
            allOf(
                hasProperty("spanKey", equalTo(SpecSpanKey.builder()
                    .traceId(traceId)
                    .spanId(rootSpan.context().toSpanId())
                    .build()
                )),
                hasProperty("name", equalTo(Optional.of("root"))),
                hasProperty("serviceName", equalTo(Optional.of(SERVICE_NAME))),
                hasProperty("parentSpanKey", equalTo(Optional.empty()))
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
                )))
            )
        ));
    }

}
