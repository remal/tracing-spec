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

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static utils.test.datetime.DateTimePrecisionUtils.withMicrosecondsPrecision;

import com.google.protobuf.ByteString;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;
import lombok.val;
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
    void spanId() {
        assertThat(
            JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                Span.newBuilder()
                    .setSpanId(ByteString.copyFrom(new byte[]{1, -1, 9}))
                    .build()
            ),
            hasProperty("spanId", equalTo("1ff09"))
        );
    }

    @Test
    void parentSpanId() {
        assertThat(
            JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                Span.newBuilder()
                    .addAllReferences(singletonList(
                        SpanRef.newBuilder()
                            .setRefType(SpanRefType.CHILD_OF)
                            .setSpanId(ByteString.copyFrom(new byte[]{1, -1, 9}))
                            .build()
                    ))
                    .build()
            ),
            hasProperty("parentSpanId", equalTo(Optional.of("1ff09")))
        );
    }

    @Test
    void leadingSpanId() {
        assertThat(
            JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                Span.newBuilder()
                    .addAllReferences(singletonList(
                        SpanRef.newBuilder()
                            .setRefType(SpanRefType.FOLLOWS_FROM)
                            .setSpanId(ByteString.copyFrom(new byte[]{1, -1, 9}))
                            .build()
                    ))
                    .build()
            ),
            hasProperty("leadingSpanId", equalTo(Optional.of("1ff09")))
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
    void description() {
        val baseTag = KeyValue.newBuilder()
            .setVStr("string value")
            .setVBool(true)
            .setVInt64(Integer.MAX_VALUE + 10L)
            .setVFloat64(Float.MAX_VALUE + 10.0D)
            .setVBinary(ByteString.copyFrom(new byte[]{1, -1, 9}))
            .build();
        for (val valueType : ValueType.values()) {
            final String expectedValue;
            if (valueType == ValueType.STRING) {
                expectedValue = baseTag.getVStr();
            } else if (valueType == ValueType.BOOL) {
                expectedValue = String.valueOf(baseTag.getVBool());
            } else if (valueType == ValueType.INT64) {
                expectedValue = String.valueOf(baseTag.getVInt64());
            } else if (valueType == ValueType.FLOAT64) {
                expectedValue = String.valueOf(baseTag.getVFloat64());
            } else if (valueType == ValueType.BINARY) {
                expectedValue = String.valueOf(baseTag.getVBinary());
            } else if (valueType == ValueType.UNRECOGNIZED) {
                continue;
            } else {
                throw new AssertionError("Unknown value type: " + valueType);
            }

            assertThat(
                "Value type " + valueType,
                JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                    Span.newBuilder()
                        .addTags(
                            KeyValue.newBuilder(baseTag).setKey("spec.description").setVType(valueType).build()
                        )
                        .build()
                ),
                hasProperty("description", equalTo(Optional.ofNullable(expectedValue)))
            );
        }
    }

    @Test
    void async() {
        val baseTag = KeyValue.newBuilder()
            .setVStr("tRuE")
            .setVBool(true)
            .setVInt64(1L)
            .setVFloat64(1.0D)
            .setVBinary(ByteString.copyFrom(new byte[]{1}))
            .build();
        for (val valueType : ValueType.values()) {
            final boolean expectedValue;
            if (valueType == ValueType.STRING) {
                expectedValue = true;
            } else if (valueType == ValueType.BOOL) {
                expectedValue = true;
            } else if (valueType == ValueType.INT64) {
                expectedValue = true;
            } else if (valueType == ValueType.FLOAT64) {
                expectedValue = false;
            } else if (valueType == ValueType.BINARY) {
                expectedValue = false;
            } else if (valueType == ValueType.UNRECOGNIZED) {
                continue;
            } else {
                throw new AssertionError("Unknown value type: " + valueType);
            }

            assertThat(
                "Value type " + valueType,
                JaegerSpanConverter.convertJaegerSpanToSpecSpan(
                    Span.newBuilder()
                        .addTags(
                            KeyValue.newBuilder(baseTag).setKey("spec.is-async").setVType(valueType).build()
                        )
                        .build()
                ),
                hasProperty("async", equalTo(expectedValue))
            );
        }
    }

}
