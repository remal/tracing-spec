package name.remal.tracingspec.renderer;

import lombok.Data;
import name.remal.tracingspec.model.SpecSpanNode;

@Data
public class TestSpecSpanNodeProcessor implements SpecSpanNodeProcessor {

    @Override
    public void processNode(SpecSpanNode node) {
        node.putTag("node-test", true);
    }

}
