package name.remal.tracingspec.spring;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

import brave.Tracer;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@Role(ROLE_INFRASTRUCTURE)
@ConditionalOnProperty(value = "tracingspec.spring.enabled", matchIfMissing = true)
@EnableConfigurationProperties(TracingSpecSpringProperties.class)
@ConditionalOnClass(AbstractPointcutAdvisor.class)
@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter(name = {
    "org.springframework.cloud.sleuth.autoconfig.instrument.web.TraceWebAutoConfiguration",
    "org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration",
    "org.springframework.cloud.sleuth.annotation.SleuthAnnotationConfiguration",
    "org.springframework.cloud.sleuth.annotation.SleuthAnnotationAutoConfiguration",
})
@SuppressWarnings("java:S1118")
public class TracingSpecAutoConfiguration {

    @Bean
    static BeanPostProcessor sleuthAdvisorOrderChanger() {
        return new SleuthAdvisorOrderChanger();
    }

    @Bean
    static SpecSpanTagsPointcutAdvisor specSpanTagsPointcutAdvisor(
        ObjectProvider<Tracer> tracer,
        TracingSpecSpringProperties properties
    ) {
        return new SpecSpanTagsPointcutAdvisor(tracer, properties);
    }

    @Configuration
    @ConditionalOnClass(Operation.class)
    static class Swagger3Configuration {
        @Bean
        Swagger3PointcutAdvisor swagger3PointcutAdvisor(
            ObjectProvider<Tracer> tracer,
            TracingSpecSpringProperties properties
        ) {
            return new Swagger3PointcutAdvisor(tracer, properties);
        }
    }

    @Configuration
    @ConditionalOnClass(ApiOperation.class)
    static class Swagger2Configuration {
        @Bean
        Swagger2PointcutAdvisor swagger2PointcutAdvisor(
            ObjectProvider<Tracer> tracer,
            TracingSpecSpringProperties properties
        ) {
            return new Swagger2PointcutAdvisor(tracer, properties);
        }
    }

}
