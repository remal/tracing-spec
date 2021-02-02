package name.remal.tracingspec.renderer.processor.script;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.junit.jupiter.api.Test;

class JsScriptEngineProcessorTest {

    @Test
    void graph() throws Throwable {
        val graph = new SpecSpansGraph().addRoot(nextSpecSpanNode());
        val processor = new JsScriptEngineProcessor("graph.removeRoot(graph.getRoots()[0])");
        processor.processGraph(graph);
        assertThat(graph.getRoots(), empty());
    }

    @Test
    void node() throws Throwable {
        val node = nextSpecSpanNode(it -> {
            it.setServiceName("empty");
        });
        val processor = new JsScriptEngineProcessor("node.serviceName = 'service'");
        processor.processNode(node);
        assertThat(node.getServiceName(), equalTo("service"));
    }

}
