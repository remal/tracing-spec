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
import org.springframework.context.annotation.Primary;
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

    @Primary
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
