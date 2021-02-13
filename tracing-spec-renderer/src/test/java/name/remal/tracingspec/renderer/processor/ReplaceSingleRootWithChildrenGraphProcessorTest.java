package name.remal.tracingspec.renderer.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.junit.jupiter.api.Test;

class ReplaceSingleRootWithChildrenGraphProcessorTest {

    private final ReplaceSingleRootWithChildrenGraphProcessor processor
        = new ReplaceSingleRootWithChildrenGraphProcessor();

    @Test
    void positive_scenario() throws Throwable {
        val root = nextSpecSpanNode();
        val parent1 = nextSpecSpanNode(it -> it.setParent(root));
        val child1 = nextSpecSpanNode(it -> it.setParent(parent1));
        val parent2 = nextSpecSpanNode(it -> it.setParent(root));
        val graph = new SpecSpansGraph()
            .addRoot(root);

        processor.processGraph(graph);

        val expectedGraph = new SpecSpansGraph()
            .addRoot(parent1)
            .addRoot(parent2);
        assertThat(graph, equalTo(expectedGraph));
    }

}
