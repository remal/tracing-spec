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
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import com.google.common.collect.ImmutableMap;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanKey;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpanEndpoint;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpan;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S109")
class ZipkinSpanConverterTest {

    @Test
    void spanKey() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder()
                    .traceId("trace")
                    .id("id")
                    .build()
            ),
            hasProperty("spanKey", equalTo(SpecSpanKey.builder()
                .traceId("trace")
                .spanId("id")
                .build()
            ))
        );
    }

    @Test
    void parentSpanKey() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder()
                    .traceId("trace")
                    .id("id")
                    .parentId("parentId")
                    .build()
            ),
            hasProperty("parentSpanKey", equalTo(Optional.of(SpecSpanKey.builder()
                .traceId("trace")
                .spanId("parentId")
                .build()
            )))
        );
    }

    @Test
    void name() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().traceId("0").id("0")
                    .name("test name")
                    .build()
            ),
            hasProperty("name", equalTo(Optional.of("test name")))
        );
    }

    @Test
    void serviceName_only_remoteEndpoint() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().traceId("0").id("0")
                    .remoteEndpoint(ZipkinSpanEndpoint.builder()
                        .serviceName("remote service")
                        .build()
                    )
                    .build()
            ),
            hasProperty("serviceName", equalTo(Optional.of("remote service")))
        );
    }

    @Test
    void serviceName_only_localEndpoint() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().traceId("0").id("0")
                    .localEndpoint(ZipkinSpanEndpoint.builder()
                        .serviceName("local service")
                        .build()
                    )
                    .build()
            ),
            hasProperty("serviceName", equalTo(Optional.of("local service")))
        );
    }

    @Test
    void serviceName_both_endpoints() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().traceId("0").id("0")
                    .remoteEndpoint(ZipkinSpanEndpoint.builder()
                        .serviceName("remote service")
                        .build()
                    )
                    .localEndpoint(ZipkinSpanEndpoint.builder()
                        .serviceName("local service")
                        .build()
                    )
                    .build()
            ),
            hasProperty("serviceName", equalTo(Optional.of("remote service")))
        );
    }

    @Test
    void startedAt() {
        val now = Instant.now();
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().traceId("0").id("0")
                    .timestamp(MICROSECONDS.convert(now.getEpochSecond(), SECONDS)
                        + MICROSECONDS.convert(now.getNano(), NANOSECONDS)
                    )
                    .build()
            ),
            hasProperty("startedAt", equalTo(Optional.of(now)))
        );
    }

    @Test
    void duration() {
        val duration = Duration.between(LocalTime.MIDNIGHT, LocalTime.now());
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().traceId("0").id("0")
                    .duration(MICROSECONDS.convert(duration.getSeconds(), SECONDS)
                        + MICROSECONDS.convert(duration.getNano(), NANOSECONDS)
                    )
                    .build()
            ),
            hasProperty("duration", equalTo(Optional.of(duration)))
        );
    }

    @Test
    void tags() {
        val tags = ImmutableMap.of("tag", "value");
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().traceId("0").id("0")
                    .tags(tags)
                    .build()
            ),
            hasProperty("tags", equalTo(tags))
        );
    }

}
