package name.remal.tracingspec.renderer;

import static java.util.Arrays.asList;
import static name.remal.tracingspec.model.SpecSpansGraphs.createSpecSpansGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpan;

import lombok.val;
import org.junit.jupiter.api.Test;

class DefaultSpecSpansGraphPreparerTest {

    @Test
    void graph_is_created() {
        val preparer = new DefaultSpecSpansGraphPreparer(new RenderingOptions());
        val parent = nextSpecSpan(it -> {
        });
        val child = nextSpecSpan(it -> {
            it.setParentSpanId(parent.getSpanId());
        });

        val expectedGraph = createSpecSpansGraph(asList(child, parent));
        assertThat(preparer.prepareSpecSpansGraph(asList(child, parent)), equalTo(expectedGraph));
    }

    @Test
    void graph_is_processed() {
        val preparer = new DefaultSpecSpansGraphPreparer(new RenderingOptions()
            .addGraphProcessor(graph -> graph.visit(node -> node.setServiceName("graph-processed")))
        );
        val parent = nextSpecSpan(it -> {
        });
        val child = nextSpecSpan(it -> {
            it.setParentSpanId(parent.getSpanId());
        });

        val graph = preparer.prepareSpecSpansGraph(asList(child, parent));

        val expectedGraph = createSpecSpansGraph(asList(child, parent));
        expectedGraph.visit(node -> node.setServiceName("graph-processed"));

        assertThat(graph, equalTo(expectedGraph));
    }

    @Test
    void nodes_are_processed() {
        val preparer = new DefaultSpecSpansGraphPreparer(new RenderingOptions()
            .addNodeProcessor(node -> node.setServiceName("node-processed"))
        );
        val parent = nextSpecSpan(it -> {
        });
        val child = nextSpecSpan(it -> {
            it.setParentSpanId(parent.getSpanId());
        });

        val graph = preparer.prepareSpecSpansGraph(asList(child, parent));

        val expectedGraph = createSpecSpansGraph(asList(child, parent));
        expectedGraph.visit(node -> node.setServiceName("node-processed"));

        assertThat(graph, equalTo(expectedGraph));
    }

    @Test
    void only_displayable_tags_are_left() {
        val preparer = new DefaultSpecSpansGraphPreparer(new RenderingOptions()
            .addTagsToDisplay("2")
        );
        val parent = nextSpecSpan(it -> {
        });
        val child = nextSpecSpan(it -> {
            it.setParentSpanId(parent.getSpanId());
            it.putTag("1", 1);
            it.putTag("2", 2);
        });

        val graph = preparer.prepareSpecSpansGraph(asList(child, parent));

        val expectedGraph = createSpecSpansGraph(asList(child, parent));
        expectedGraph.visit(node -> node.getTags().keySet().removeIf(it -> !it.equals("2")));

        assertThat(graph, equalTo(expectedGraph));
    }

    @Test
    void remote_service_name_is_cleared_if_it_equals_to_service_name() {
        val preparer = new DefaultSpecSpansGraphPreparer(new RenderingOptions()
            .addTagsToDisplay("2")
        );
        val parent = nextSpecSpan(it -> {
        });
        val child = nextSpecSpan(it -> {
            it.setParentSpanId(parent.getSpanId());
            it.setServiceName("service");
            it.setRemoteServiceName("service");
        });

        val graph = preparer.prepareSpecSpansGraph(asList(child, parent));

        val expectedGraph = createSpecSpansGraph(asList(child, parent));
        expectedGraph.visit(node -> node.setRemoteServiceName(null));

        assertThat(graph, equalTo(expectedGraph));
    }

}
