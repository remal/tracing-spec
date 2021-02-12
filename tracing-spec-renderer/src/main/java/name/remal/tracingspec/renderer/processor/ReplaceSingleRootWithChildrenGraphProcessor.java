package name.remal.tracingspec.renderer.processor;

import java.util.ArrayList;
import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import name.remal.tracingspec.renderer.SpecSpansGraphProcessor;

public class ReplaceSingleRootWithChildrenGraphProcessor implements SpecSpansGraphProcessor {

    @Override
    public void processGraph(SpecSpansGraph graph) {
        val roots = graph.getRoots();
        if (roots.size() == 1) {
            val root = roots.get(0);
            graph.removeRoot(root);
            new ArrayList<>(root.getChildren()).forEach(graph::addRoot);
        }
    }

}
