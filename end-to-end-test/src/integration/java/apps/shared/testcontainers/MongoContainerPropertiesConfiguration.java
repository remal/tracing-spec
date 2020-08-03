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

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

import com.mongodb.client.MongoClient;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.testcontainers.containers.MongoDBContainer;

@Configuration
@ConditionalOnClass(MongoDBContainer.class)
@ImportAutoConfiguration(MongoAutoConfiguration.class)
@AutoConfigureBefore(MongoAutoConfiguration.class)
@Role(ROLE_INFRASTRUCTURE)
public class MongoContainerPropertiesConfiguration {

    @Bean
    public static BeanPostProcessor mongoContainerBeanPostProcessor(
        ObjectProvider<MongoDBContainer> mongoContainerProvider
    ) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof MongoProperties) {
                    val props = (MongoProperties) bean;

                    val mongoContainer = mongoContainerProvider.getIfAvailable();
                    if (mongoContainer != null) {
                        mongoContainer.start();
                        props.setUri(mongoContainer.getReplicaSetUrl());
                        props.setHost(null);
                        props.setPort(null);
                        props.setUsername(null);
                        props.setPassword(null);
                        props.setDatabase(null);
                    }
                }

                return bean;
            }
        };
    }

    @Configuration
    @ConditionalOnClass(MongoClient.class)
    public static class KafkaContainerConfiguration {

        @Bean
        public MongoDBContainer mongoContainer() {
            return new MongoDBContainer();
        }

    }

}
