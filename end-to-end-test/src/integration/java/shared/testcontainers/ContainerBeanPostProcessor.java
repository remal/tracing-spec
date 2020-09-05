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
