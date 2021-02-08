package name.remal.tracingspec.retriever.jaeger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JaegerSpecSpansRetrieverProperties.class)
@ConditionalOnProperty(value = "tracingspec.retriever.jaeger.url")
public class JaegerSpecSpansRetrieverAutoConfiguration {

    @Bean
    public JaegerSpecSpansRetriever jaegerSpecSpansRetriever(JaegerSpecSpansRetrieverProperties properties) {
        return new JaegerSpecSpansRetriever(properties);
    }

}
