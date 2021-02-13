package name.remal.tracingspec.renderer;

import javax.annotation.Nullable;
import lombok.Data;
import name.remal.tracingspec.model.SpecSpansGraph;

@Data
public class TestValueSpecSpansGraphProcessor implements SpecSpansGraphProcessor {

    @Nullable
    final Object value;

    @Override
    public void processGraph(SpecSpansGraph graph) {
        graph.visit(node -> node.putTag("graph-test-value", value));
    }

}
