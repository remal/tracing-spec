package name.remal.tracingspec.renderer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@SpringBootApplication
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
class TracingSpecRendererAutoConfigurationTest {

    final RenderingOptions renderingOptions;

    @Test
    void renderingOptions() {
        assertThat(renderingOptions.getGraphProcessors(), hasItem(instanceOf(TestValueSpecSpansGraphProcessor.class)));
        assertThat(renderingOptions.getNodeProcessors(), hasItem(instanceOf(TestSpecSpanNodeProcessor.class)));
        assertThat(renderingOptions.getNodeProcessors(), hasItem(instanceOf(TestValueSpecSpanNodeProcessor.class)));
    }

}
