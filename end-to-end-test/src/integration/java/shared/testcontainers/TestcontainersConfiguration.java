package shared.testcontainers;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import utils.test.container.JaegerAllInOneContainer;
import utils.test.container.ZipkinContainer;

@Configuration
@ConditionalOnClass(Container.class)
@Role(ROLE_INFRASTRUCTURE)
public class TestcontainersConfiguration {

    @Bean
    public static TestcontainersStarter testcontainersStarter() {
        return new TestcontainersStarter();
    }

    @Bean
    public static BeanPostProcessor containerBeanPostProcessor(TestcontainersStarter testcontainersStarter) {
        return new ContainerBeanPostProcessor(testcontainersStarter);
    }


    @Bean
    public ZipkinContainer zipkinContainer() {
        return new ZipkinContainer();
    }

    @Bean
    public JaegerAllInOneContainer jaegerContainer() {
        return new JaegerAllInOneContainer();
    }

    @Bean
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"));
    }

    @Bean
    public TestcontainersLifecycle testcontainersLifecycle(ApplicationContext applicationContext) {
        return new TestcontainersLifecycle(applicationContext);
    }

}
