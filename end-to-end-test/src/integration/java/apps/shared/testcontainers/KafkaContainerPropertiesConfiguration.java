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

package apps.shared.testcontainers;

import static java.util.Collections.singletonList;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

import lombok.val;
import org.apache.kafka.clients.KafkaClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.testcontainers.containers.KafkaContainer;

@Configuration
@ConditionalOnClass(KafkaContainer.class)
@ImportAutoConfiguration(KafkaAutoConfiguration.class)
@AutoConfigureBefore(KafkaAutoConfiguration.class)
@Role(ROLE_INFRASTRUCTURE)
public class KafkaContainerPropertiesConfiguration {

    @Bean
    public static BeanPostProcessor kafkaContainerBeanPostProcessor(
        ObjectProvider<KafkaContainer> kafkaContainerProvider
    ) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof KafkaProperties) {
                    val props = (KafkaProperties) bean;

                    val kafkaContainer = kafkaContainerProvider.getIfAvailable();
                    if (kafkaContainer != null) {
                        kafkaContainer.start();
                        props.setBootstrapServers(singletonList(kafkaContainer.getBootstrapServers()));
                    }
                }

                return bean;
            }
        };
    }

    @Configuration
    @ConditionalOnClass(KafkaClient.class)
    public static class KafkaContainerConfiguration {

        @Bean
        public KafkaContainer kafkaContainer() {
            return new KafkaContainer();
        }

    }

}
