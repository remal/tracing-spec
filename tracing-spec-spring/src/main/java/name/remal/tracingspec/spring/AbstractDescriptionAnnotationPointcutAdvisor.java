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

import static org.springframework.aop.support.AopUtils.getMostSpecificMethod;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import brave.Tracer;
import com.google.common.reflect.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.api.RelocateClasses;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.core.Ordered;

@Internal
@RelocateClasses(TypeToken.class)
@SuppressWarnings({"UnstableApiUsage", "java:S2160", "java:S1948"})
abstract class AbstractDescriptionAnnotationPointcutAdvisor<A extends Annotation> extends AbstractPointcutAdvisor {

    protected abstract Function<A, String> getDescriptionGetter();


    private final Tracer tracer;
    private final TracingSpecSpringProperties properties;

    private final Class<A> annotationType;
    private final Function<A, String> descriptionGetter;

    private final Pointcut pointcut;
    private final MethodInterceptor advice;

    protected AbstractDescriptionAnnotationPointcutAdvisor(Tracer tracer, TracingSpecSpringProperties properties) {
        this.tracer = tracer;
        this.properties = properties;

        this.annotationType = getAnnotationType();
        this.descriptionGetter = getDescriptionGetter();

        this.pointcut = new AnnotationMatchingPointcut(null, this.annotationType, true);
        this.advice = new DescriptionAnnotationAdvice();
    }

    @SuppressWarnings("unchecked")
    private Class<A> getAnnotationType() {
        val advisorType = TypeToken.of(getClass())
            .getSupertype(AbstractDescriptionAnnotationPointcutAdvisor.class)
            .getType();
        if (!(advisorType instanceof ParameterizedType)) {
            throw new IllegalStateException(advisorType + " is not an instance of ParameterizedType");
        }
        val advisorParameterizedType = (ParameterizedType) advisorType;
        return (Class<A>) TypeToken.of(advisorParameterizedType.getActualTypeArguments()[0]).getRawType();
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public MethodInterceptor getAdvice() {
        return advice;
    }


    private class DescriptionAnnotationAdvice implements IntroductionInterceptor, Ordered {

        @Override
        @Nullable
        @SneakyThrows
        public Object invoke(MethodInvocation invocation) {
            if (!properties.isEnabled()) {
                return invocation.proceed();
            }

            val span = tracer.currentSpan();
            if (span == null || span.isNoop()) {
                return invocation.proceed();
            }

            if (properties.isDescriptionOnlyIfDebug() && !span.context().debug()) {
                return invocation.proceed();
            }

            val description = Optional.ofNullable(invocation.getMethod())
                .map(it -> getMostSpecificMethod(it, invocation.getThis().getClass()))
                .map(it -> findAnnotation(it, annotationType))
                .map(descriptionGetter)
                .orElse(null);
            if (description == null || description.isEmpty()) {
                return invocation.proceed();
            }

            span.tag("spec.description", description);

            return invocation.proceed();
        }


        @Override
        public boolean implementsInterface(Class<?> intf) {
            return true;
        }

        @Override
        public int getOrder() {
            return LOWEST_PRECEDENCE;
        }

    }

}
