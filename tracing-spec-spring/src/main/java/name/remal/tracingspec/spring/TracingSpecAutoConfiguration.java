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

package name.remal.tracingspec.spring;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

import brave.Tracer;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.annotation.SleuthAnnotationAutoConfiguration;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@Role(ROLE_INFRASTRUCTURE)
@ConditionalOnProperty(value = "tracingspec.spring.enabled", matchIfMissing = true)
@EnableConfigurationProperties(TracingSpecSpringProperties.class)
@ConditionalOnClass(AbstractPointcutAdvisor.class)
@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter({
    TraceAutoConfiguration.class,
    SleuthAnnotationAutoConfiguration.class
})
@SuppressWarnings("java:S1118")
public class TracingSpecAutoConfiguration {

    @Bean
    static BeanPostProcessor sleuthAdvisorOrderChanger() {
        return new SleuthAdvisorOrderChanger();
    }

    @Bean
    static SpecSpanTagsPointcutAdvisor specSpanTagsPointcutAdvisor(
        Tracer tracer,
        TracingSpecSpringProperties properties
    ) {
        return new SpecSpanTagsPointcutAdvisor(tracer, properties);
    }

    @Configuration
    @ConditionalOnClass(Operation.class)
    static class Swagger3Configuration {
        @Bean
        Swagger3PointcutAdvisor swagger3PointcutAdvisor(Tracer tracer, TracingSpecSpringProperties properties) {
            return new Swagger3PointcutAdvisor(tracer, properties);
        }
    }

    @Configuration
    @ConditionalOnClass(ApiOperation.class)
    static class Swagger2Configuration {
        @Bean
        Swagger2PointcutAdvisor swagger2PointcutAdvisor(Tracer tracer, TracingSpecSpringProperties properties) {
            return new Swagger2PointcutAdvisor(tracer, properties);
        }
    }

}
