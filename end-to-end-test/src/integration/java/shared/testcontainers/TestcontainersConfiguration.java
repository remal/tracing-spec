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

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static org.testcontainers.containers.Network.SHARED;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.lifecycle.Startable;
import utils.test.container.JaegerAllInOneContainer;
import utils.test.container.ZipkinContainer;

@Configuration
@ConditionalOnClass(Startable.class)
@Role(ROLE_INFRASTRUCTURE)
public class TestcontainersConfiguration {

    @Bean
    public ZipkinContainer zipkinContainer() {
        return new ZipkinContainer()
            .withNetwork(SHARED);
    }

    @Bean
    public JaegerAllInOneContainer jaegerContainer() {
        return new JaegerAllInOneContainer()
            .withNetwork(SHARED);
    }

    @Bean
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer()
            .withNetwork(SHARED);
    }

    @Bean
    public TestcontainersLifecycle testcontainersLifecycle(ApplicationContext applicationContext) {
        return new TestcontainersLifecycle(applicationContext);
    }

}
