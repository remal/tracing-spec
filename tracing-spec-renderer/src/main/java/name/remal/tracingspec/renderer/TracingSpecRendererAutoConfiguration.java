package name.remal.tracingspec.renderer;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import name.remal.tracingspec.renderer.jackson.JsonTracingSpecRenderer;
import name.remal.tracingspec.renderer.jackson.YamlTracingSpecRenderer;
import name.remal.tracingspec.renderer.plantuml.sequence.TracingSpecPlantumlSequenceRenderer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@EnableConfigurationProperties(RenderingOptions.class)
public class TracingSpecRendererAutoConfiguration {

    @Bean
    @Role(ROLE_INFRASTRUCTURE)
    static RenderingOptionsBeanPostProcessor renderingOptionsBeanPostProcessor(ApplicationContext context) {
        return new RenderingOptionsBeanPostProcessor(context);
    }

    @Bean
    @ConditionalOnMissingBean(SpecSpansGraphPreparer.class)
    public SpecSpansGraphPreparer specSpansGraphPreparer(RenderingOptions options) {
        return new DefaultSpecSpansGraphPreparer(options);
    }

    @Bean
    public TracingSpecPlantumlSequenceRenderer tracingSpecPlantumlSequenceRenderer() {
        return new TracingSpecPlantumlSequenceRenderer();
    }

    @Configuration
    @ConditionalOnClass(ObjectMapper.class)
    static class JsonTracingSpecRendererConfiguration {
        @Bean
        public JsonTracingSpecRenderer jsonTracingSpecRenderer() {
            return new JsonTracingSpecRenderer();
        }
    }

    @Configuration
    @ConditionalOnClass(YAMLMapper.class)
    static class YamlTracingSpecRendererConfiguration {
        @Bean
        public YamlTracingSpecRenderer yamlTracingSpecRenderer() {
            return new YamlTracingSpecRenderer();
        }
    }

}
