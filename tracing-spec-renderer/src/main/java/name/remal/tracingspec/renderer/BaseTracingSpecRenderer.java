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

import static java.nio.file.Files.createDirectories;
import static name.remal.tracingspec.model.SpecSpansGraphs.createSpecSpansGraph;

import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.jetbrains.annotations.Contract;

public abstract class BaseTracingSpecRenderer<Result> implements TracingSpecRenderer<Result> {

    protected abstract Result renderSpecSpansGraph(SpecSpansGraph graph, RenderingOptions options);

    protected abstract void writeResultToPath(Result result, Path path);


    @Override
    public final Result renderTracingSpec(List<SpecSpan> specSpans, RenderingOptions options) {
        if (specSpans.isEmpty()) {
            throw new IllegalArgumentException("specSpans must not be empty");
        }

        val graph = createSpecSpansGraph(specSpans);
        processGraph(graph, options);
        processNodes(graph, options);
        leaveOnlyDisplayableTags(graph, options);
        checkServiceNameExistence(graph);
        return renderSpecSpansGraph(graph, options);
    }

    @SneakyThrows
    private static void processGraph(SpecSpansGraph graph, RenderingOptions options) {
        for (val graphProcessor : options.getGraphProcessors()) {
            graphProcessor.processGraph(graph);
        }
    }

    private static void processNodes(SpecSpansGraph graph, RenderingOptions options) {
        for (val nodeProcessor : options.getNodeProcessors()) {
            graph.visit(nodeProcessor::processNode);
        }
    }

    private static void checkServiceNameExistence(SpecSpansGraph graph) {
        graph.visit(node -> {
            if (node.getServiceName() == null) {
                throw new IllegalStateException("Node doesn't have service name: " + node);
            }
        });
    }

    private static void leaveOnlyDisplayableTags(SpecSpansGraph graph, RenderingOptions options) {
        graph.visit(node ->
            node.getTags().keySet().removeIf(tagName -> !options.getTagsToDisplay().contains(tagName))
        );
    }


    @Override
    @SneakyThrows
    public void renderTracingSpecToPath(List<SpecSpan> specSpans, RenderingOptions options, Path path) {
        path = path.toAbsolutePath();
        val parentPath = path.getParent();
        if (parentPath != null && !parentPath.equals(path)) {
            createDirectories(parentPath);
        }

        val result = renderTracingSpec(specSpans, options);
        writeResultToPath(result, path);
    }


    @Contract("null -> true")
    protected static boolean isEmpty(@Nullable CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    @Contract("null -> false")
    protected static boolean isNotEmpty(@Nullable CharSequence charSequence) {
        return !isEmpty(charSequence);
    }


    @Contract("null, _ -> param2")
    protected static <T> T defaultValue(@Nullable T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    protected static String defaultValue(@Nullable String value) {
        return defaultValue(value, "");
    }

}
