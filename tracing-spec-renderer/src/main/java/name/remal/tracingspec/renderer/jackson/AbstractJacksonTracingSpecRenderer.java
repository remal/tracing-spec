package name.remal.tracingspec.renderer.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import name.remal.tracingspec.model.SpecSpansGraph;
import name.remal.tracingspec.renderer.AbstractStringTracingSpecRenderer;

abstract class AbstractJacksonTracingSpecRenderer extends AbstractStringTracingSpecRenderer {

    protected final ObjectMapper objectMapper;

    protected AbstractJacksonTracingSpecRenderer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @SneakyThrows
    protected String renderTracingSpecImpl(SpecSpansGraph graph) {
        return objectMapper.writeValueAsString(graph);
    }

}
