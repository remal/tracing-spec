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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static test.datetime.DateTimePrecisionUtils.withMicrosecondsPrecision;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanKey;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.KeyValue;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.Process;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.Span;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.SpanRef;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.SpanRefType;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.ValueType;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S109")
class JaegerSpanConverterTest {

    @Test
    void spanKey() {
        assertThat(
            JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                Span.newBuilder()
                    .setTraceId(ByteString.copyFrom(new byte[]{1, -1, 9}))
                    .setSpanId(ByteString.copyFrom(new byte[]{1, -1, 9}))
                    .build()
            ),
            hasProperty("spanKey", equalTo(SpecSpanKey.builder()
                .traceId("1ff09")
                .spanId("1ff09")
                .build()
            ))
        );
    }

    @Test
    void parentSpanKey() {
        assertThat(
            JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                Span.newBuilder()
                    .addAllReferences(singletonList(
                        SpanRef.newBuilder()
                            .setRefType(SpanRefType.CHILD_OF)
                            .setTraceId(ByteString.copyFrom(new byte[]{1, -1, 9}))
                            .setSpanId(ByteString.copyFrom(new byte[]{1, -1, 9}))
                            .build()
                    ))
                    .build()
            ),
            hasProperty("parentSpanKey", equalTo(Optional.of(
                SpecSpanKey.builder().traceId("1ff09").spanId("1ff09").build()
            )))
        );
    }

    @Test
    void leadingSpanKey() {
        assertThat(
            JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                Span.newBuilder()
                    .addAllReferences(singletonList(
                        SpanRef.newBuilder()
                            .setRefType(SpanRefType.FOLLOWS_FROM)
                            .setTraceId(ByteString.copyFrom(new byte[]{1, -1, 9}))
                            .setSpanId(ByteString.copyFrom(new byte[]{1, -1, 9}))
                            .build()
                    ))
                    .build()
            ),
            hasProperty("leadingSpanKey", equalTo(Optional.of(
                SpecSpanKey.builder().traceId("1ff09").spanId("1ff09").build()
            )))
        );
    }

    @Test
    void name() {
        assertThat(
            JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                Span.newBuilder().setOperationName("test name").build()
            ),
            hasProperty("name", equalTo(Optional.of("test name")))
        );
    }

    @Test
    void serviceName() {
        assertThat(
            JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                Span.newBuilder()
                    .setProcess(Process.newBuilder()
                        .setServiceName("test name")
                    )
                    .build()
            ),
            hasProperty("serviceName", equalTo(Optional.of("test name")))
        );
    }

    @Test
    void startedAt() {
        val now = withMicrosecondsPrecision(Instant.now());
        assertThat(
            JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                Span.newBuilder()
                    .setStartTime(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .setNanos(now.getNano())
                    )
                    .build()
            ),
            hasProperty("startedAt", equalTo(Optional.of(now)))
        );
    }

    @Test
    void duration() {
        val duration = withMicrosecondsPrecision(Duration.between(LocalTime.MIDNIGHT, LocalTime.now()));
        assertThat(
            JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                Span.newBuilder()
                    .setDuration(com.google.protobuf.Duration.newBuilder()
                        .setSeconds(duration.getSeconds())
                        .setNanos(duration.getNano())
                    )
                    .build()
            ),
            hasProperty("duration", equalTo(Optional.of(duration)))
        );
    }

    @Test
    void tags() {
        val baseTag = KeyValue.newBuilder()
            .setVStr("string")
            .setVBool(true)
            .setVInt64(Integer.MAX_VALUE + 10L)
            .setVFloat64(Float.MAX_VALUE + 10.0D)
            .setVBinary(ByteString.copyFrom(new byte[]{1, -1, 9}))
            .build();
        assertThat(
            JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                Span.newBuilder()
                    .addAllTags(asList(
                        KeyValue.newBuilder(baseTag).setKey("string").setVType(ValueType.STRING).build(),
                        KeyValue.newBuilder(baseTag).setKey("bool").setVType(ValueType.BOOL).build(),
                        KeyValue.newBuilder(baseTag).setKey("int64").setVType(ValueType.INT64).build(),
                        KeyValue.newBuilder(baseTag).setKey("float64").setVType(ValueType.FLOAT64).build(),
                        KeyValue.newBuilder(baseTag).setKey("binary").setVType(ValueType.BINARY).build()
                    ))
                    .build()
            ),
            allOf(
                hasProperty("tags", equalTo(ImmutableMap.of(
                    "string", baseTag.getVStr(),
                    "bool", baseTag.getVBool() + "",
                    "int64", baseTag.getVInt64() + "",
                    "float64", baseTag.getVFloat64() + "",
                    "binary", baseTag.getVBinary().toString()
                )))
            )
        );
    }

}
