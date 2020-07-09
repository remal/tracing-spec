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

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpanKey;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinEndpoint;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpan;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
interface ZipkinSpanConverter {

    static SpecSpan convertZipkinSpanToSpecSpan(ZipkinSpan zipkinSpan) {
        val builder = SpecSpan.builder();

        builder.spanKey(SpecSpanKey.builder()
            .traceId(zipkinSpan.getTraceId())
            .spanId(zipkinSpan.getId())
            .build()
        );

        zipkinSpan.getParentId().ifPresent(parentSpanId ->
            builder.parentSpanKey(SpecSpanKey.builder()
                .traceId(zipkinSpan.getTraceId())
                .spanId(parentSpanId)
                .build()
            )
        );

        builder.name(zipkinSpan.getName());

        val endpoint = zipkinSpan.getRemoteEndpoint().orElseGet(() -> zipkinSpan.getLocalEndpoint().orElse(null));
        builder.serviceName(Optional.ofNullable(endpoint).flatMap(ZipkinEndpoint::getServiceName));

        zipkinSpan.getTimestamp().ifPresent(timestamp ->
            builder.startedAt(Instant.ofEpochSecond(
                0,
                NANOSECONDS.convert(timestamp, MICROSECONDS)
            ))
        );

        zipkinSpan.getDuration().ifPresent(duration ->
            builder.duration(Duration.ofSeconds(
                0,
                NANOSECONDS.convert(duration, MICROSECONDS)
            ))
        );

        builder.tags(zipkinSpan.getTags());

        return builder.build();
    }

}
