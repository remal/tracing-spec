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
