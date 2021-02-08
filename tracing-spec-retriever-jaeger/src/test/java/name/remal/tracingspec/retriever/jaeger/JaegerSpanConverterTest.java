package name.remal.tracingspec.retriever.jaeger;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static name.remal.tracingspec.model.SpecSpanKind.CLIENT;
import static name.remal.tracingspec.model.SpecSpanKind.CONSUMER;
import static name.remal.tracingspec.model.SpecSpanKind.PRODUCER;
import static name.remal.tracingspec.model.SpecSpanKind.SERVER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static utils.test.datetime.DateTimePrecisionUtils.withMicrosecondsPrecision;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Map;
import lombok.val;
import name.remal.tracingspec.retriever.jaeger.internal.ImmutableJaegerKeyValue;
import name.remal.tracingspec.retriever.jaeger.internal.ImmutableJaegerLog;
import name.remal.tracingspec.retriever.jaeger.internal.ImmutableJaegerReference;
import name.remal.tracingspec.retriever.jaeger.internal.ImmutableJaegerSpan;
import name.remal.tracingspec.retriever.jaeger.internal.JaegerReferenceType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S109")
class JaegerSpanConverterTest {

    private static final String PROCESS_ID = "p1";
    private static final String SERVICE_NAME = "service";
    private static final Map<String, String> PROCESS_ID_TO_NAME = ImmutableMap.of(PROCESS_ID, SERVICE_NAME);

    @Test
    void spanId() {
        assertThat(
            JaegerSpanConverter.convertJaegerTraceDataToSpecSpans(
                ImmutableJaegerSpan.builder()
                    .spanId("46e110ae1180e35")
                    .build(),
                PROCESS_ID_TO_NAME
            ),
            hasProperty("spanId", equalTo("046e110ae1180e35"))
        );
    }

    @Test
    void parentSpanId() {
        assertThat(
            JaegerSpanConverter.convertJaegerTraceDataToSpecSpans(
                ImmutableJaegerSpan.builder().spanId("0")
                    .addReference(ImmutableJaegerReference.builder()
                        .refType(JaegerReferenceType.CHILD_OF)
                        .spanId("50be829a1bd75ca057")
                        .build()
                    )
                    .build(),
                PROCESS_ID_TO_NAME
            ),
            hasProperty("parentSpanId", equalTo("0000000000000050be829a1bd75ca057"))
        );
    }

    @Test
    void name() {
        assertThat(
            JaegerSpanConverter.convertJaegerTraceDataToSpecSpans(
                ImmutableJaegerSpan.builder().spanId("0")
                    .operationName("test name")
                    .build(),
                PROCESS_ID_TO_NAME
            ),
            hasProperty("name", equalTo("test name"))
        );
    }

    @Test
    void serviceName() {
        assertThat(
            JaegerSpanConverter.convertJaegerTraceDataToSpecSpans(
                ImmutableJaegerSpan.builder().spanId("0")
                    .processId(PROCESS_ID)
                    .build(),
                PROCESS_ID_TO_NAME
            ),
            hasProperty("serviceName", equalTo(SERVICE_NAME))
        );
    }

    @Test
    void startedAt() {
        val now = withMicrosecondsPrecision(Instant.now());
        val micros = SECONDS.toMicros(now.getEpochSecond()) + NANOSECONDS.toMicros(now.getNano());
        assertThat(
            JaegerSpanConverter.convertJaegerTraceDataToSpecSpans(
                ImmutableJaegerSpan.builder().spanId("0")
                    .startTime(micros)
                    .build(),
                PROCESS_ID_TO_NAME
            ),
            hasProperty("startedAt", equalTo(now))
        );
    }


    @Nested
    class Kind {

        @Test
        void from_tags() {
            assertThat(
                JaegerSpanConverter.convertJaegerTraceDataToSpecSpans(
                    ImmutableJaegerSpan.builder().spanId("0")
                        .addTag(ImmutableJaegerKeyValue.builder()
                            .key("span.kind")
                            .value("client")
                            .build()
                        )
                        .build(),
                    PROCESS_ID_TO_NAME
                ),
                hasProperty("kind", equalTo(CLIENT))
            );
        }

        @Test
        void from_annotations() {
            val pairs = ImmutableList.of(
                ImmutablePair.of("ms", PRODUCER),
                ImmutablePair.of("mr", CONSUMER),
                ImmutablePair.of("ss", SERVER),
                ImmutablePair.of("sr", SERVER),
                ImmutablePair.of("cs", CLIENT),
                ImmutablePair.of("cr", CLIENT)
            );
            for (val pair : pairs) {
                assertThat(
                    pair.toString(),
                    JaegerSpanConverter.convertJaegerTraceDataToSpecSpans(
                        ImmutableJaegerSpan.builder().spanId("0")
                            .addLog(ImmutableJaegerLog.builder()
                                .addField(ImmutableJaegerKeyValue.builder()
                                    .key("event")
                                    .value(pair.getLeft())
                                    .build()
                                )
                                .build()
                            )
                            .build(),
                        PROCESS_ID_TO_NAME
                    ),
                    hasProperty("kind", equalTo(pair.getRight()))
                );
            }
        }

    }

}
