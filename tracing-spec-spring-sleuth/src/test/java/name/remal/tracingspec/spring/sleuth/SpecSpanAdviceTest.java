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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import brave.Span;
import brave.Tracer;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpecSpanAdviceTest {

    private static final String INVOCATION_RESULT = "executed";

    private final Tracer tracer = mock(Tracer.class);

    private final SpecSpanAdvice advice = new SpecSpanAdvice(tracer);

    private final MethodInvocation invocation = mock(MethodInvocation.class);

    private final Span span = mock(Span.class);

    @BeforeEach
    void beforeEach() throws Throwable {
        when(invocation.proceed()).thenReturn(INVOCATION_RESULT);
    }

    @Test
    void not_method_invocation() {
        when(invocation.getMethod()).thenReturn(null);
        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));
    }

    @Test
    void no_SpecSpan_annotation_present() throws Throwable {
        when(invocation.getMethod()).thenReturn(NotAnnotated.class.getMethod("method"));
        when(invocation.getThis()).thenReturn(new NotAnnotated());

        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));
    }

    private static class NotAnnotated {
        public void method() {
        }
    }

    @Test
    void no_current_span_present() throws Throwable {
        when(invocation.getMethod()).thenReturn(Annotated.class.getMethod("paramsDefault"));
        when(invocation.getThis()).thenReturn(new Annotated());

        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));

        when(tracer.currentSpan()).thenReturn(null);
    }

    @Test
    void params_description() throws Throwable {
        when(invocation.getMethod()).thenReturn(Annotated.class.getMethod("paramsDescription"));
        when(invocation.getThis()).thenReturn(new Annotated());

        when(tracer.currentSpan()).thenReturn(span);

        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));

        verify(span, times(1)).tag("spec.description", "description");
        verifyNoMoreInteractions(span);
    }

    @Test
    void params_isAsync() throws Throwable {
        when(invocation.getMethod()).thenReturn(Annotated.class.getMethod("paramsIsAsync"));
        when(invocation.getThis()).thenReturn(new Annotated());

        when(tracer.currentSpan()).thenReturn(span);

        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));

        verify(span, times(1)).tag("spec.is-async", "true");
        verifyNoMoreInteractions(span);
    }

    private static class Annotated {
        @SpecSpan
        public void paramsDefault() {
        }

        @SpecSpan(description = "description")
        public void paramsDescription() {
        }

        @SpecSpan(isAsync = true)
        public void paramsIsAsync() {
        }
    }

}
