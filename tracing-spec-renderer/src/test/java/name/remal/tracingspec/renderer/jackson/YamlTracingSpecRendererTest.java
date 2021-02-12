package name.remal.tracingspec.renderer.jackson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.resource.Resources.readTextResource;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.junit.jupiter.api.Test;

class YamlTracingSpecRendererTest {

    final YamlTracingSpecRenderer renderer = new YamlTracingSpecRenderer();

    @Test
    void renderTracingSpec() {
        val parent = nextSpecSpanNode(it -> {
            it.setName("parent");
            it.setServiceName("service");
        });
        val child = nextSpecSpanNode(it -> {
            it.setParent(parent);
            it.setName("child");
            it.setServiceName("service");
        });
        val graph = new SpecSpansGraph()
            .addRoot(parent);

        val result = renderer.renderTracingSpec(graph);
        val normalizedResult = result.trim();

        val expectedResult = readTextResource("expected.yml");
        val normalizedExpectedResult = expectedResult.trim();

        assertThat(normalizedResult, equalTo(normalizedExpectedResult));
    }

}
