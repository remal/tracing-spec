package name.remal.tracingspec.application;

import static lombok.AccessLevel.PROTECTED;

import java.nio.file.Path;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import name.remal.tracingspec.renderer.SpecSpansGraphPreparer;
import name.remal.tracingspec.retriever.SpecSpansRetriever;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Internal
@Command(name = "render-trace", description = "Render tracing spans into a file")
@Component
@RequiredArgsConstructor
@ToString
@Setter(PROTECTED)
@Getter(PROTECTED)
@SuppressWarnings("java:S3749")
class RenderTraceCommand implements CommandLineCommand {

    private final ObjectProvider<SpecSpansRetriever> retriever;

    private final SpecSpansGraphPreparer graphPreparer;

    private final RendersRegistry rendersRegistry;


    @Parameters(index = "0", description = "Trace ID")
    private String traceId;

    @Parameters(index = "1", description = "Renderer name")
    private String rendererName;

    @Parameters(index = "2", description = "Output file path")
    private Path outputPath;


    @Override
    public void run() {
        val renderer = rendersRegistry.getRendererByName(rendererName);
        val spans = retriever.getObject().retrieveSpecSpansForTrace(traceId);
        val graph = graphPreparer.prepareSpecSpansGraph(spans);
        renderer.renderTracingSpecToPath(graph, outputPath);
    }

}
