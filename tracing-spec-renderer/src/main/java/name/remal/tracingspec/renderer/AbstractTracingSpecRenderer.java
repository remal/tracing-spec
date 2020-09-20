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

import java.nio.file.Path;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.jetbrains.annotations.Contract;

public abstract class AbstractTracingSpecRenderer<Result> implements TracingSpecRenderer<Result> {

    protected abstract Result renderTracingSpecImpl(SpecSpansGraph graph);

    protected abstract void writeResultToPath(Result result, Path path);


    @Override
    public final Result renderTracingSpec(SpecSpansGraph graph) {
        checkServiceNameExistence(graph);
        return renderTracingSpecImpl(graph);
    }

    private static void checkServiceNameExistence(SpecSpansGraph graph) {
        graph.visit(node -> {
            if (node.getServiceName() == null) {
                throw new IllegalStateException("Node doesn't have service name: " + node);
            }
        });
    }


    @Override
    @SneakyThrows
    public void renderTracingSpecToPath(SpecSpansGraph graph, Path path) {
        path = path.toAbsolutePath();
        val parentPath = path.getParent();
        if (parentPath != null && !parentPath.equals(path)) {
            createDirectories(parentPath);
        }

        val result = renderTracingSpec(graph);
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
