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

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.sleuth.zipkin2.ZipkinProperties;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;
import utils.test.container.ZipkinContainer;

@Component
@Role(ROLE_INFRASTRUCTURE)
@ConditionalOnBean(ZipkinContainer.class)
public class ZipkinPropertiesBeanPostProcessor
    extends AbstractContainerPropertiesBeanPostProcessor<ZipkinProperties, ZipkinContainer> {

    @Override
    protected void configure(ZipkinProperties props, ZipkinContainer container) {
        props.setBaseUrl(container.getZipkinBaseUrl());
        props.setDiscoveryClientEnabled(false);
        props.setMessageTimeout(1);
    }

}
