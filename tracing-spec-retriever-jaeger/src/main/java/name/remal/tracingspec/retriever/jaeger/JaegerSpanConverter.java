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

import static name.remal.tracingspec.model.SpecSpanTag.processAllTagsIntoBuilder;
import static name.remal.tracingspec.retriever.jaeger.JaegerIdUtils.decodeJaegerId;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.Span;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.SpanRefType;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.ValueType;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
interface JaegerSpanConverter {

    static SpecSpan convertJaegerSpanToSpecSpan(Span jaegerSpan) {
        val builder = SpecSpan.builder();

        val spanId = decodeJaegerId(jaegerSpan.getSpanId().toByteArray());
        builder.spanId(spanId);

        Optional.ofNullable(jaegerSpan.getOperationName())
            .filter(it -> !it.isEmpty())
            .ifPresent(builder::name);

        if (jaegerSpan.hasProcess()) {
            Optional.ofNullable(jaegerSpan.getProcess().getServiceName())
                .filter(it -> !it.isEmpty())
                .ifPresent(builder::serviceName);
        }

        if (jaegerSpan.hasStartTime()) {
            val startTime = jaegerSpan.getStartTime();
            builder.startedAt(Instant.ofEpochSecond(
                startTime.getSeconds(),
                startTime.getNanos()
            ));
        }

        Map<String, Object> tags = new HashMap<>();
        jaegerSpan.getTagsList().forEach(tag -> {
            val tagKey = tag.getKey();

            final String tagValue;
            val valueType = tag.getVType();
            if (valueType == ValueType.STRING) {
                tagValue = tag.getVStr();
            } else if (valueType == ValueType.BOOL) {
                tagValue = tag.getVBool() + "";
            } else if (valueType == ValueType.INT64) {
                tagValue = tag.getVInt64() + "";
            } else if (valueType == ValueType.FLOAT64) {
                tagValue = tag.getVFloat64() + "";
            } else if (valueType == ValueType.BINARY) {
                tagValue = tag.getVBinary().toString();
            } else {
                LogManager.getLogger(JaegerSpanConverter.class).warn(
                    "Span {}: Unsupported value type for tag {}: {}",
                    spanId,
                    tagKey,
                    valueType
                );
                return;
            }

            tags.put(tagKey, tagValue);
        });
        processAllTagsIntoBuilder(tags, builder);

        jaegerSpan.getReferencesList().forEach(ref -> {
            val refSpanId = decodeJaegerId(ref.getSpanId().toByteArray());

            val refType = ref.getRefType();
            if (refType == SpanRefType.CHILD_OF) {
                builder.parentSpanId(refSpanId);
            } else {
                LogManager.getLogger(JaegerSpanConverter.class).warn(
                    "Span {}: Unsupported ref type: {}",
                    spanId,
                    refType
                );
            }
        });

        return builder.build();
    }

}
