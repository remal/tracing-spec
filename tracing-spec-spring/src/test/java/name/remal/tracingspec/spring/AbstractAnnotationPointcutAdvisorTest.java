package name.remal.tracingspec.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

abstract class AbstractAnnotationPointcutAdvisorTest {

    protected abstract AbstractAnnotationPointcutAdvisor<?> createAdvisor(
        ObjectProvider<Tracer> tracerProvider,
        TracingSpecSpringProperties properties
    );


    private static final String INVOCATION_RESULT = "executed";

    private final Tracer tracer = mock(Tracer.class);

    @SuppressWarnings("unchecked")
    private final ObjectProvider<Tracer> tracerProvider = mock(ObjectProvider.class);

    private final TracingSpecSpringProperties properties = new TracingSpecSpringProperties();

    protected final AbstractAnnotationPointcutAdvisor<?> advisor = createAdvisor(tracerProvider, properties);

    private final MethodInterceptor advice = advisor.getAdvice();

    private final MethodInvocation invocation = mock(MethodInvocation.class);

    private final Span span = mock(Span.class);

    private final TraceContext traceContext = mock(TraceContext.class);

    @BeforeEach
    void beforeEach() throws Throwable {
        when(tracerProvider.getObject()).thenReturn(tracer);

        when(tracer.currentSpan()).thenReturn(span);

        when(invocation.getMethod()).thenReturn(Methods.class.getMethod("annotated"));
        when(invocation.getThis()).thenReturn(new Methods());
        when(invocation.proceed()).thenReturn(INVOCATION_RESULT);

        when(span.isNoop()).thenReturn(false);
        when(span.context()).thenReturn(traceContext);
    }


    @Test
    final void not_enabled() throws Throwable {
        properties.setEnabled(false);
        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));

        verify(span, never()).tag(any(), any());
    }

    @Test
    final void no_current_span_present() throws Throwable {
        when(tracer.currentSpan()).thenReturn(null);

        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));
        verify(span, never()).tag(any(), any());
    }

    @Test
    final void not_sampled() throws Throwable {
        when(span.isNoop()).thenReturn(true);

        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));
        verify(span, never()).tag(any(), any());
    }

    @Test
    final void not_debug_when_should_be_debug() throws Throwable {
        properties.setDescriptionOnlyIfDebug(true);
        when(traceContext.debug()).thenReturn(false);

        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));
        verify(span, never()).tag(any(), any());
    }

    @Test
    final void not_method_invocation() throws Throwable {
        when(invocation.getMethod()).thenReturn(null);

        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));
        verify(span, never()).tag(any(), any());
    }

    @Test
    final void no_annotation_present() throws Throwable {
        when(invocation.getMethod()).thenReturn(Methods.class.getMethod("notAnnotated"));

        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));
        verify(span, never()).tag(any(), any());
    }

    @Test
    final void empty() throws Throwable {
        when(invocation.getMethod()).thenReturn(Methods.class.getMethod("empty"));

        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));
        verify(span, never()).tag(any(), any());
    }

    @Test
    final void full() throws Throwable {
        assertThat(advice.invoke(invocation), equalTo(INVOCATION_RESULT));
        if (advisor.getHiddenGetter() != null) {
            verify(span, times(1)).tag("spec.hidden", "1");
        }
        if (advisor.getKindGetter() != null) {
            verify(span, times(1)).tag("spec.kind", "client");
        }
        if (advisor.getAsyncGetter() != null) {
            verify(span, times(1)).tag("spec.async", "1");
        }
        if (advisor.getServiceNameGetter() != null) {
            verify(span, times(1)).tag("spec.serviceName", "local");
        }
        if (advisor.getRemoteServiceNameGetter() != null) {
            verify(span, times(1)).tag("spec.remoteServiceName", "remote");
        }
        if (advisor.getDescriptionGetter() != null) {
            verify(span, times(1)).tag("spec.description", "description");
        }
    }

    @Test
    final void debug_when_should_be_debug() throws Throwable {
        properties.setDescriptionOnlyIfDebug(true);
        when(traceContext.debug()).thenReturn(true);
        full();
    }


    private static class Methods {
        public void notAnnotated() {
        }

        @SpecSpanTags
        @ApiOperation("")
        @Operation
        public void empty() {
        }

        @SpecSpanTags(
            hidden = true,
            kind = "client",
            async = true,
            serviceName = "local",
            remoteServiceName = "remote",
            description = "description"
        )
        @ApiOperation("description")
        @Operation(summary = "description")
        public void annotated() {
        }
    }

}
