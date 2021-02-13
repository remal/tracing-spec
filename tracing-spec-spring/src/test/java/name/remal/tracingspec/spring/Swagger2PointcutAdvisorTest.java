package name.remal.tracingspec.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import brave.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class Swagger2PointcutAdvisorTest extends AbstractAnnotationPointcutAdvisorTest {

    @Override
    protected AbstractAnnotationPointcutAdvisor<?> createAdvisor(
        ObjectProvider<Tracer> tracerProvider,
        TracingSpecSpringProperties properties
    ) {
        return new Swagger2PointcutAdvisor(tracerProvider, properties);
    }

    @Test
    void getHiddenGetter() {
        assertThat(advisor.getHiddenGetter(), nullValue());
    }

    @Test
    void getKindGetter() {
        assertThat(advisor.getKindGetter(), nullValue());
    }

    @Test
    void getAsyncGetter() {
        assertThat(advisor.getAsyncGetter(), nullValue());
    }

    @Test
    void getServiceNameGetter() {
        assertThat(advisor.getServiceNameGetter(), nullValue());
    }

    @Test
    void getRemoteServiceNameGetter() {
        assertThat(advisor.getRemoteServiceNameGetter(), nullValue());
    }

    @Test
    void getDescriptionGetter() {
        assertThat(advisor.getDescriptionGetter(), notNullValue());
    }

}
