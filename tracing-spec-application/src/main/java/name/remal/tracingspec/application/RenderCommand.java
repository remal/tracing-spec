/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package name.remal.tracingspec.application;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PROTECTED;
import static name.remal.tracingspec.application.ExitException.INCORRECT_CMD_PARAM_EXIT_CODE;

import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import name.remal.tracingspec.renderer.SpecSpansGraphPreparer;
import name.remal.tracingspec.renderer.TracingSpecRenderer;
import name.remal.tracingspec.retriever.SpecSpansRetriever;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Internal
@Command(name = "render", description = "Render tracing spans into a file")
@Component
@RequiredArgsConstructor
@ToString
@Setter(PROTECTED)
@Getter(PROTECTED)
@SuppressWarnings("java:S3749")
class RenderCommand implements CommandLineCommand {

    private final ObjectProvider<SpecSpansRetriever> retriever;

    private final SpecSpansGraphPreparer graphPreparer;

    private final List<TracingSpecRenderer<?>> renderers;


    @Parameters(index = "0", description = "Trace ID")
    private String traceId;

    @Parameters(index = "1", description = "Renderer name")
    private String rendererName;

    @Parameters(index = "2", description = "Output file path")
    private Path outputPath;


    @Override
    public void run() {
        val renderer = renderers.stream()
            .filter(it -> it.getRendererName().equals(rendererName))
            .findAny()
            .orElse(null);
        if (renderer == null) {
            throw new ExitException(format(
                "Renderer doesn't exist or isn't configured: %s. These renderers are configured: %s.",
                rendererName,
                String.join(", ", getConfiguredRendererNames())
            ), INCORRECT_CMD_PARAM_EXIT_CODE);
        }

        val spans = retriever.getObject().retrieveSpecSpansForTrace(traceId);
        val graph = graphPreparer.prepareSpecSpansGraph(spans);
        renderer.renderTracingSpecToPath(graph, outputPath);
    }

    private List<String> getConfiguredRendererNames() {
        return renderers.stream()
            .map(TracingSpecRenderer::getRendererName)
            .collect(toList());
    }

}
