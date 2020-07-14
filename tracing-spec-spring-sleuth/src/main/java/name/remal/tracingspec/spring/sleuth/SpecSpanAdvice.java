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

package name.remal.tracingspec.spring.sleuth;

import static org.springframework.aop.support.AopUtils.getMostSpecificMethod;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import brave.Span;
import brave.Tracer;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.aop.IntroductionInterceptor;

@Internal
@RequiredArgsConstructor
class SpecSpanAdvice implements IntroductionInterceptor {

    private final Tracer tracer;

    @Override
    @Nullable
    @SneakyThrows
    public Object invoke(MethodInvocation invocation) {
        val method = Optional.ofNullable(invocation.getMethod())
            .map(it -> getMostSpecificMethod(it, invocation.getThis().getClass()))
            .orElse(null);
        if (method == null) {
            return invocation.proceed();
        }

        val specSpan = findAnnotation(method, SpecSpan.class);
        if (specSpan == null) {
            return invocation.proceed();
        }

        val span = tracer.currentSpan();
        if (span == null) {
            return invocation.proceed();
        }

        adjustSpan(span, specSpan);

        return invocation.proceed();
    }

    private void adjustSpan(Span span, SpecSpan specSpan) {
        Optional.of(specSpan.description())
            .filter(StringUtils::isNotEmpty)
            .ifPresent(description -> span.tag("spec.description", description));

        if (specSpan.isAsync()) {
            span.tag("spec.is-async", "true");
        }
    }

    @Override
    public boolean implementsInterface(Class<?> intf) {
        return true;
    }

}
