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

package utils.test.sleuth;

import static brave.sampler.Sampler.ALWAYS_SAMPLE;
import static zipkin2.reporter.Reporter.NOOP;

import brave.Tracer;
import brave.handler.SpanHandler;
import brave.sampler.Sampler;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

@TestConfiguration
@ConditionalOnClass(Tracer.class)
@AutoConfigureBefore(name = {
    "org.springframework.cloud.sleuth.autoconfig.instrument.web.TraceWebAutoConfiguration",
    "org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration",
    "org.springframework.cloud.sleuth.annotation.SleuthAnnotationConfiguration",
    "org.springframework.cloud.sleuth.annotation.SleuthAnnotationAutoConfiguration",
})
@ConditionalOnMissingClass("org.springframework.cloud.sleuth.zipkin2.ZipkinAutoConfiguration")
public class TestSleuthAutoConfiguration {

    @Bean
    Sampler testTracingSampler() {
        return ALWAYS_SAMPLE;
    }

    @Bean
    Reporter<Span> testTracingReporter() {
        return NOOP;
    }

    @Bean
    SpanHandler testSpanHandler() {
        return new TestSpanHandler();
    }

}
