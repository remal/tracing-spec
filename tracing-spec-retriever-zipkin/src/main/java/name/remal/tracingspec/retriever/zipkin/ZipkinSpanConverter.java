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

import java.time.Instant;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpanAnnotation;
import name.remal.tracingspec.model.SpecSpanKind;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpan;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpanEndpoint;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
interface ZipkinSpanConverter {

    static SpecSpan convertZipkinSpanToSpecSpan(ZipkinSpan zipkinSpan) {
        val specSpan = new SpecSpan(zipkinSpan.getId());

        zipkinSpan.getParentId().ifPresent(specSpan::setParentSpanId);

        specSpan.setName(zipkinSpan.getName().orElse(null));

        specSpan.setKind(zipkinSpan.getKind()
            .map(Object::toString)
            .map(SpecSpanKind::parseSpecSpanKind)
            .orElse(null)
        );

        specSpan.setServiceName(zipkinSpan.getLocalEndpoint()
            .flatMap(ZipkinSpanEndpoint::getServiceName)
            .orElse(null)
        );
        specSpan.setRemoteServiceName(zipkinSpan.getRemoteEndpoint()
            .flatMap(ZipkinSpanEndpoint::getServiceName)
            .orElse(null)
        );

        zipkinSpan.getTimestamp().ifPresent(timestamp ->
            specSpan.setStartedAt(Instant.ofEpochSecond(
                0,
                NANOSECONDS.convert(timestamp, MICROSECONDS)
            ))
        );

        zipkinSpan.getTags().forEach(specSpan::putTag);

        for (val zipkinAnnotation : zipkinSpan.getAnnotations()) {
            val value = zipkinAnnotation.getValue().orElse(null);
            if (value == null) {
                continue;
            }

            if (zipkinAnnotation.getTimestamp().isPresent()) {
                val instant = Instant.ofEpochSecond(
                    0,
                    NANOSECONDS.convert(zipkinAnnotation.getTimestamp().getAsLong(), MICROSECONDS)
                );
                specSpan.addAnnotation(new SpecSpanAnnotation(instant, value));
            } else {
                specSpan.addAnnotation(new SpecSpanAnnotation(value));
            }
        }

        return specSpan;
    }

}
