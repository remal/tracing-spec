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

import static name.remal.tracingspec.model.SpecSpanKind.CLIENT;
import static name.remal.tracingspec.model.SpecSpanKind.CONSUMER;
import static name.remal.tracingspec.model.SpecSpanKind.PRODUCER;
import static name.remal.tracingspec.model.SpecSpanKind.SERVER;
import static name.remal.tracingspec.model.SpecSpanKind.parseSpecSpanKind;
import static name.remal.tracingspec.retriever.jaeger.JaegerIdUtils.decodeJaegerId;

import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpanAnnotation;
import name.remal.tracingspec.model.SpecSpanKind;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.KeyValue;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.Span;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.SpanRefType;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.ValueType;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
abstract class JaegerSpanConverter {

    @SuppressWarnings("java:S3776")
    public static SpecSpan convertJaegerSpanToSpecSpan(Span jaegerSpan) {
        val spanId = decodeJaegerId(jaegerSpan.getSpanId().toByteArray());
        val specSpan = new SpecSpan(spanId);

        jaegerSpan.getReferencesList().forEach(ref -> {
            val refSpanId = decodeJaegerId(ref.getSpanId().toByteArray());

            val refType = ref.getRefType();
            if (refType == SpanRefType.CHILD_OF) {
                specSpan.setParentSpanId(refSpanId);
            } else {
                LogManager.getLogger(JaegerSpanConverter.class).warn(
                    "Span {}: Unsupported ref type: {}",
                    spanId,
                    refType
                );
            }
        });

        Optional.ofNullable(jaegerSpan.getOperationName())
            .filter(it -> !it.isEmpty())
            .ifPresent(specSpan::setName);

        if (jaegerSpan.hasProcess()) {
            Optional.ofNullable(jaegerSpan.getProcess().getServiceName())
                .filter(it -> !it.isEmpty())
                .ifPresent(specSpan::setServiceName);
        }

        if (jaegerSpan.hasStartTime()) {
            val startTime = jaegerSpan.getStartTime();
            specSpan.setStartedAt(Instant.ofEpochSecond(
                startTime.getSeconds(),
                startTime.getNanos()
            ));
        }

        jaegerSpan.getTagsList().forEach(tag ->
            processKeyValue(spanId, tag, specSpan::putTag)
        );

        jaegerSpan.getLogsList().forEach(log ->
            log.getFieldsList().forEach(field ->
                processKeyValue(spanId, field, (key, value) -> {
                    if (log.hasTimestamp()) {
                        val jaegerTimestamp = log.getTimestamp();
                        val instant = Instant.ofEpochSecond(
                            jaegerTimestamp.getSeconds(),
                            jaegerTimestamp.getNanos()
                        );
                        specSpan.addAnnotation(new SpecSpanAnnotation(instant, key, value));
                    } else {
                        specSpan.addAnnotation(new SpecSpanAnnotation(key, value));
                    }
                })
            )
        );

        if (specSpan.getKind() == null) {
            specSpan.setKind(parseSpecSpanKind(specSpan.getTag("span.kind")));
        }

        if (specSpan.getKind() == null) {
            specSpan.getAnnotations().stream()
                .sorted()
                .filter(annotation -> "event".equals(annotation.getKey()))
                .map(SpecSpanAnnotation::getValue)
                .filter(Objects::nonNull)
                .map(EVENT_TO_KIND_MAPPINGS::get)
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(specSpan::setKind);
        }

        return specSpan;
    }

    private static void processKeyValue(String spanId, KeyValue keyValue, BiConsumer<String, String> consumer) {
        val key = keyValue.getKey();

        final String value;
        val valueType = keyValue.getVType();
        if (valueType == ValueType.STRING) {
            value = keyValue.getVStr();
        } else if (valueType == ValueType.BOOL) {
            value = keyValue.getVBool() + "";
        } else if (valueType == ValueType.INT64) {
            value = keyValue.getVInt64() + "";
        } else if (valueType == ValueType.FLOAT64) {
            value = keyValue.getVFloat64() + "";
        } else if (valueType == ValueType.BINARY) {
            value = keyValue.getVBinary().toString();
        } else {
            LogManager.getLogger(JaegerSpanConverter.class).warn(
                "Span {}: Unsupported value type for key {} of {}: {}",
                spanId,
                key,
                keyValue,
                valueType
            );
            return;
        }

        consumer.accept(key, value);
    }

    private static final Map<String, SpecSpanKind> EVENT_TO_KIND_MAPPINGS = ImmutableMap.<String, SpecSpanKind>builder()
        .put("ms", PRODUCER)
        .put("mr", CONSUMER)
        .put("cs", CLIENT)
        .put("sr", SERVER)
        .put("ss", SERVER)
        .put("cr", CLIENT)
        .build();


    private JaegerSpanConverter() {
    }

}
