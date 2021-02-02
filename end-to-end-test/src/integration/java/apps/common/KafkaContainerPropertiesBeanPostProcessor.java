package apps.common;

import static java.util.Collections.singletonList;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.KafkaContainer;

@Component
@Role(ROLE_INFRASTRUCTURE)
@RequiredArgsConstructor
@ConditionalOnBean(KafkaContainer.class)
public class KafkaContainerPropertiesBeanPostProcessor
    extends AbstractContainerPropertiesBeanPostProcessor<KafkaProperties, KafkaContainer> {

    @Override
    protected void configure(KafkaProperties props, KafkaContainer container) {
        props.setBootstrapServers(singletonList(container.getBootstrapServers()));
    }

}
