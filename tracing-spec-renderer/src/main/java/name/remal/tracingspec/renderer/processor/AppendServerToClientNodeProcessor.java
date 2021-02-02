package name.remal.tracingspec.renderer.processor;

import static name.remal.tracingspec.model.SpecSpanKind.CLIENT;
import static name.remal.tracingspec.model.SpecSpanKind.SERVER;

import lombok.val;
import name.remal.tracingspec.model.SpecSpanNode;
import name.remal.tracingspec.renderer.SpecSpanNodeProcessor;

public class AppendServerToClientNodeProcessor implements SpecSpanNodeProcessor {

    @Override
    public void processNode(SpecSpanNode parent) {
        val children = parent.getChildren();
        if (children.size() != 1) {
            return;
        }

        val child = children.get(0);
        if (parent.getKind() == CLIENT && child.getKind() == SERVER) {
            if (parent.getName() != null && child.getName() != null) {
                if (child.getName().startsWith(parent.getName())) {
                    parent.setName(child.getName());
                }
            }

            if (parent.getRemoteServiceName() == null) {
                parent.setRemoteServiceName(child.getServiceName());
            }

            parent.append(child);
            parent.sortChildren();
        }
    }

}
