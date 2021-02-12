package name.remal.tracingspec.renderer;

import javax.annotation.Nullable;
import lombok.Data;
import name.remal.tracingspec.model.SpecSpanNode;

@Data
public class TestValueSpecSpanNodeProcessor implements SpecSpanNodeProcessor {

    @Nullable
    final Object value;

    @Override
    public void processNode(SpecSpanNode node) {
        node.putTag("node-test-value", value);
    }

}
