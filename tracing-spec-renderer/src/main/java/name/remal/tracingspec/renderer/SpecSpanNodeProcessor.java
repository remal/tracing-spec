package name.remal.tracingspec.renderer;

import name.remal.tracingspec.model.SpecSpanNode;

@FunctionalInterface
public interface SpecSpanNodeProcessor {

    void processNode(SpecSpanNode node) throws Throwable;

}
