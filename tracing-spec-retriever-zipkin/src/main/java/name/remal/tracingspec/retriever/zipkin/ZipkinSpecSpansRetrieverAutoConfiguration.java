package name.remal.tracingspec.retriever.zipkin;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ZipkinSpecSpansRetrieverProperties.class)
@ConditionalOnProperty(value = "tracingspec.retriever.zipkin.url")
public class ZipkinSpecSpansRetrieverAutoConfiguration {

    @Bean
    public ZipkinSpecSpansRetriever zipkinSpecSpansRetriever(ZipkinSpecSpansRetrieverProperties properties) {
        return new ZipkinSpecSpansRetriever(properties);
    }

}
