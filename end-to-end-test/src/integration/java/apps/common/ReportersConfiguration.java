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

package apps.common;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static zipkin2.codec.SpanBytesEncoder.JSON_V2;

import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.testcontainers.containers.GenericContainer;
import shared.testcontainers.TestcontainersStarter;
import utils.test.container.JaegerAllInOneContainer;
import utils.test.container.WithZipkinCollectorUrl;
import utils.test.container.ZipkinContainer;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@Configuration
@Role(ROLE_INFRASTRUCTURE)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ReportersConfiguration {

    @Bean
    public Reporter<Span> logSpanReporter() {
        return new LogSpanReporter();
    }

    @Bean
    @ConditionalOnBean(ZipkinContainer.class)
    public Reporter<Span> zipkinReporter(
        TestcontainersStarter containersStarter,
        ZipkinContainer container
    ) {
        return createReporter(containersStarter, container);
    }

    @Bean
    @ConditionalOnBean(JaegerAllInOneContainer.class)
    public Reporter<Span> jaegerReporter(
        TestcontainersStarter containersStarter,
        JaegerAllInOneContainer container
    ) {
        return createReporter(containersStarter, container);
    }

    private <Container extends GenericContainer<Container> & WithZipkinCollectorUrl> Reporter<Span> createReporter(
        TestcontainersStarter containersStarter,
        Container container
    ) {
        containersStarter.start(container);

        val sender = URLConnectionSender.create(container.getZipkinCollectorUrl());

        return AsyncReporter.builder(sender)
            .queuedMaxSpans(1000)
            .messageTimeout(1, MICROSECONDS)
            .closeTimeout(10, SECONDS)
            .build(JSON_V2);
    }

}
