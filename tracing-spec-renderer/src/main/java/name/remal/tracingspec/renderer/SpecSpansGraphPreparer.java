package name.remal.tracingspec.renderer;

import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface SpecSpansGraphPreparer {

    SpecSpansGraph prepareSpecSpansGraph(Iterable<? extends SpecSpan> specSpansIterable);

    @Internal
    void processSpecSpansGraph(SpecSpansGraph graph);

}
