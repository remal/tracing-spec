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

package apps.common;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.synchronizedSet;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.util.IdentityHashMap;
import java.util.Set;
import lombok.val;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.testcontainers.containers.GenericContainer;

abstract class AbstractContainerPropertiesBeanPostProcessor<Props, Container extends GenericContainer<?>>
    implements ApplicationContextAware, BeanPostProcessor, ApplicationListener<ContextRefreshedEvent>, Ordered {

    protected abstract void configure(Props props, Container container);


    private final Class<Props> propsClass;

    private final Class<Container> containerClass;

    @SuppressWarnings("unchecked")
    protected AbstractContainerPropertiesBeanPostProcessor() {
        val untypedType = TypeToken.of(getClass())
            .getSupertype(AbstractContainerPropertiesBeanPostProcessor.class)
            .getType();
        if (!(untypedType instanceof ParameterizedType)) {
            throw new IllegalStateException("Not a ParameterizedType: " + untypedType);
        }
        val type = (ParameterizedType) untypedType;
        this.propsClass = (Class<Props>) TypeToken.of(type.getActualTypeArguments()[0]).getRawType();
        this.containerClass = (Class<Container>) TypeToken.of(type.getActualTypeArguments()[1]).getRawType();
    }


    @SuppressWarnings("NotNullFieldNotInitialized")
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    private final Set<Props> configuredProps = synchronizedSet(newSetFromMap(new IdentityHashMap<>()));

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (propsClass.isInstance(bean)) {
            val props = propsClass.cast(bean);
            applicationContext.getBeanProvider(containerClass).ifAvailable(container -> {
                if (configuredProps.add(props)) {
                    container.start();
                    configure(props, container);
                }
            });
        }

        return bean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        val context = event.getApplicationContext();
        context.getBeanProvider(propsClass).ifAvailable(props -> {
            context.getBeanProvider(containerClass).ifAvailable(container -> {
                if (configuredProps.add(props)) {
                    container.start();
                    configure(props, container);
                }
            });
        });
    }


    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}
