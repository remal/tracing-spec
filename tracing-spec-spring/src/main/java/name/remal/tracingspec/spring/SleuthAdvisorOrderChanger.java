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

package name.remal.tracingspec.spring;

import static org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * This component changes order of {@link org.springframework.cloud.sleuth.annotation.SleuthAdvisorConfig} to
 * make it run before all {@link AbstractDescriptionAnnotationPointcutAdvisor} components.
 */
@Internal
@RequiredArgsConstructor
class SleuthAdvisorOrderChanger implements BeanPostProcessor, BeanFactoryAware {

    /**
     * If {@link org.springframework.cloud.sleuth.annotation.SleuthAdvisorConfig} bean has already been added to the
     * bean factory
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof ListableBeanFactory)) {
            return;
        }

        val listableBeanFactory = (ListableBeanFactory) beanFactory;
        val advisorBeanNames = beanNamesForTypeIncludingAncestors(listableBeanFactory, Advisor.class, true, false);
        for (val advisorBeanName : advisorBeanNames) {
            val beanType = beanFactory.getType(advisorBeanName);
            if (beanType != null && isSleuthAdvisorClass(beanType)) {
                val bean = beanFactory.getBean(advisorBeanName);
                adjustSleuthAdvisorOrder(bean);
            }
        }
    }

    @Nullable
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        adjustSleuthAdvisorOrder(bean);
        return bean;
    }


    private static boolean isSleuthAdvisorClass(Class<?> clazz) {
        return clazz.getName().startsWith("org.springframework.cloud.sleuth.");
    }

    private static void adjustSleuthAdvisorOrder(Object bean) {
        if (bean instanceof AbstractPointcutAdvisor) {
            if (isSleuthAdvisorClass(bean.getClass())) {
                val advisor = (AbstractPointcutAdvisor) bean;
                if (advisor.getOrder() == Integer.MAX_VALUE) {
                    advisor.setOrder(Integer.MAX_VALUE - 1);
                }
            }
        }
    }

}
