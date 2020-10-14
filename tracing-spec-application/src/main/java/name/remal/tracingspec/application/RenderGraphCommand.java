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

import static lombok.AccessLevel.PROTECTED;
import static name.remal.tracingspec.application.SerializationUtils.readYamlOrJson;

import java.nio.file.Path;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Internal
@Command(name = "render-graph", description = "Render tracing spans graph into a file")
@Component
@RequiredArgsConstructor
@ToString
@Setter(PROTECTED)
@Getter(PROTECTED)
class RenderGraphCommand implements CommandLineCommand {

    private final RendersRegistry rendersRegistry;


    @Parameters(index = "0", description = "Tracing graph file (YAML/JSON/JSON5)")
    private Path graphFile;

    @Parameters(index = "1", description = "Renderer name")
    private String rendererName;

    @Parameters(index = "2", description = "Output file path")
    private Path outputPath;


    @Override
    public void run() {
        val renderer = rendersRegistry.getRendererByName(rendererName);
        val graph = readYamlOrJson(SpecSpansGraph.class, graphFile);
        renderer.renderTracingSpecToPath(graph, outputPath);
    }

}
