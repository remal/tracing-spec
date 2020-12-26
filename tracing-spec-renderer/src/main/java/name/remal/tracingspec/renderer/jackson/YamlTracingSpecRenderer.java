package name.remal.tracingspec.renderer.jackson;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class YamlTracingSpecRenderer extends AbstractJacksonTracingSpecRenderer {

    public YamlTracingSpecRenderer(YAMLMapper objectMapper) {
        super(objectMapper);
    }

    public YamlTracingSpecRenderer() {
        super(YAMLMapper.builder()
            .disable(WRITE_DOC_START_MARKER)
            .enable(MINIMIZE_QUOTES)
            .findAndAddModules()
            .build()
        );
    }

    @Override
    public String getRendererName() {
        return "yaml";
    }

}
