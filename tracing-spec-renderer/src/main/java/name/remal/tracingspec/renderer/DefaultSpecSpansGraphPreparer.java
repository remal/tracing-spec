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

package name.remal.tracingspec.renderer;

import static name.remal.tracingspec.model.SpecSpansGraphs.createSpecSpansGraph;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpansGraph;

@RequiredArgsConstructor
@ToString
public class DefaultSpecSpansGraphPreparer implements SpecSpansGraphPreparer {

    private final RenderingOptions options;

    @Override
    public SpecSpansGraph prepareSpecSpansGraph(Iterable<? extends SpecSpan> specSpansIterable) {
        SpecSpansGraph graph = createSpecSpansGraph(specSpansIterable);
        processSpecSpansGraph(graph);
        return graph;
    }

    @Override
    @SneakyThrows
    public void processSpecSpansGraph(SpecSpansGraph graph) {
        for (SpecSpansGraphProcessor graphProcessor : options.getGraphProcessors()) {
            graphProcessor.processGraph(graph);
        }
        for (SpecSpanNodeProcessor nodeProcessor : options.getNodeProcessors()) {
            graph.visit(nodeProcessor::processNode);
        }

        postProcessSpecSpansGraph(graph);
    }

    private void postProcessSpecSpansGraph(SpecSpansGraph graph) {
        leaveOnlyDisplayableTags(graph);
        clearRemoteServiceNameIfItEqualsToServiceName(graph);
    }

    private void leaveOnlyDisplayableTags(SpecSpansGraph graph) {
        val tagsToDisplay = options.getTagsToDisplay();
        graph.visit(node ->
            node.getTags().keySet().removeIf(tagName -> !tagsToDisplay.contains(tagName))
        );
    }

    private static void clearRemoteServiceNameIfItEqualsToServiceName(SpecSpansGraph graph) {
        graph.visit(node -> {
            if (Objects.equals(node.getServiceName(), node.getRemoteServiceName())) {
                node.setRemoteServiceName(null);
            }
        });
    }

}
