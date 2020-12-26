package name.remal.tracingspec.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import brave.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class SpecSpanTagsPointcutAdvisorTest extends AbstractAnnotationPointcutAdvisorTest {

    @Override
    protected AbstractAnnotationPointcutAdvisor<?> createAdvisor(
        ObjectProvider<Tracer> tracerProvider,
        TracingSpecSpringProperties properties
    ) {
        return new SpecSpanTagsPointcutAdvisor(tracerProvider, properties);
    }

    @Test
    void getHiddenGetter() {
        assertThat(advisor.getHiddenGetter(), notNullValue());
    }

    @Test
    void getKindGetter() {
        assertThat(advisor.getKindGetter(), notNullValue());
    }

    @Test
    void getAsyncGetter() {
        assertThat(advisor.getAsyncGetter(), notNullValue());
    }

    @Test
    void getServiceNameGetter() {
        assertThat(advisor.getServiceNameGetter(), notNullValue());
    }

    @Test
    void getRemoteServiceNameGetter() {
        assertThat(advisor.getRemoteServiceNameGetter(), notNullValue());
    }

    @Test
    void getDescriptionGetter() {
        assertThat(advisor.getDescriptionGetter(), notNullValue());
    }

}
