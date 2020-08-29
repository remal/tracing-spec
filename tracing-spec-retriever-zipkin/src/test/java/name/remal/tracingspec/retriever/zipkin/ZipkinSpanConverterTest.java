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
import static name.remal.tracingspec.model.SpecSpanKind.parseSpecSpanKind;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static utils.test.datetime.DateTimePrecisionUtils.withMicrosecondsPrecision;

import java.time.Instant;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanAnnotation;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpan;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpanAnnotation;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpanEndpoint;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpanKind;
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
            hasProperty("parentSpanId", equalTo("4213435656"))
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
            hasProperty("name", equalTo("test name"))
        );
    }

    @Test
    void kind() {
        for (val zipkinKind : ZipkinSpanKind.values()) {
            val kind = parseSpecSpanKind(zipkinKind.name());
            assertThat(zipkinKind.name(), kind, notNullValue());

            assertThat(
                zipkinKind.name(),
                ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                    ZipkinSpan.builder().id("0")
                        .kind(zipkinKind)
                        .build()
                ),
                hasProperty("kind", equalTo(kind))
            );
        }
    }

    @Test
    void serviceName() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
                    .localEndpoint(ZipkinSpanEndpoint.builder()
                        .serviceName("service")
                        .build()
                    )
                    .build()
            ),
            hasProperty("serviceName", equalTo("service"))
        );
    }

    @Test
    void remoteServiceName() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
                    .remoteEndpoint(ZipkinSpanEndpoint.builder()
                        .serviceName("service")
                        .build()
                    )
                    .build()
            ),
            hasProperty("remoteServiceName", equalTo("service"))
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
            hasProperty("startedAt", equalTo(now))
        );
    }

    @Test
    void tags() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
                    .putTag("key", "value")
                    .build()
            ),
            hasProperty("tags", hasEntry("key", "value"))
        );
    }

    @Test
    void annotations() {
        assertThat(
            ZipkinSpanConverter.convertZipkinSpanToSpecSpan(
                ZipkinSpan.builder().id("0")
                    .addAnnotation(ZipkinSpanAnnotation.builder()
                        .timestamp(123)
                        .value("annotation")
                        .build()
                    )
                    .build()
            ),
            hasProperty("annotations", contains(new SpecSpanAnnotation("annotation")))
        );
    }

}
