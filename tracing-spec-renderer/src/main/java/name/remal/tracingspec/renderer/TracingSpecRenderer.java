package name.remal.tracingspec.renderer;

import java.nio.file.Path;
import name.remal.tracingspec.model.SpecSpansGraph;

public interface TracingSpecRenderer<Result> {

    String getRendererName();

    Result renderTracingSpec(SpecSpansGraph graph);

    void renderTracingSpecToPath(SpecSpansGraph graph, Path path);

}
