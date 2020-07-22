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
import static utils.test.datetime.DateTimePrecisionUtils.withMicrosecondsPrecision;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;
import lombok.val;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpan;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpanEndpoint;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S109")
class ZipkinSpanConverterTest {

    @Test
    void spanId() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder()
                    .id("35124234")
                    .build()
            ),
            hasProperty("spanId", equalTo("35124234"))
        );
    }

    @Test
    void parentSpanId() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
                    .parentId("4213435656")
                    .build()
            ),
            hasProperty("parentSpanId", equalTo(Optional.of("4213435656")))
        );
    }

    @Test
    void name() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
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
                ZipkinSpan.builder().id("0")
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
                ZipkinSpan.builder().id("0")
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
                ZipkinSpan.builder().id("0")
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
        val now = withMicrosecondsPrecision(Instant.now());
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
                    .timestamp(
                        MICROSECONDS.convert(now.getEpochSecond(), SECONDS)
                            + MICROSECONDS.convert(now.getNano(), NANOSECONDS)
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
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
                    .duration(
                        MICROSECONDS.convert(duration.getSeconds(), SECONDS)
                            + MICROSECONDS.convert(duration.getNano(), NANOSECONDS)
                    )
                    .build()
            ),
            hasProperty("duration", equalTo(Optional.of(duration)))
        );
    }

    @Test
    void description() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
                    .putTag("spec.description", "some text")
                    .build()
            ),
            hasProperty("description", equalTo(Optional.of("some text")))
        );
    }

    @Test
    void async() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
                    .build()
            ),
            hasProperty("async", equalTo(false))
        );

        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
                    .putTag("spec.is-async", "1")
                    .build()
            ),
            hasProperty("async", equalTo(true))
        );

        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
                    .putTag("spec.is-async", "TrUe")
                    .build()
            ),
            hasProperty("async", equalTo(true))
        );
    }

}
