package name.remal.tracingspec.renderer;

import name.remal.tracingspec.model.SpecSpansGraph;

@FunctionalInterface
public interface SpecSpansGraphProcessor {

    void processGraph(SpecSpansGraph graph) throws Throwable;

}
