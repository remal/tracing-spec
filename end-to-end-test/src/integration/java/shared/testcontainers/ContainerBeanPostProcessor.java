package shared.testcontainers;

import static org.testcontainers.containers.Network.SHARED;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

@RequiredArgsConstructor
class ContainerBeanPostProcessor implements BeanPostProcessor, Ordered {

    private final TestcontainersStarter testcontainersStarter;

    @Nullable
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Container) {
            val container = (Container<?>) bean;
            container.withNetwork(SHARED);
        }

        if (bean instanceof GenericContainer) {
            val genericContainer = (GenericContainer<?>) bean;
            genericContainer.withReuse(false);

            testcontainersStarter.start(genericContainer);
        }

        return bean;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
