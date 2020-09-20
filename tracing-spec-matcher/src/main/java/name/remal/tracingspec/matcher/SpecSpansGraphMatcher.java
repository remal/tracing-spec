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

package name.remal.tracingspec.matcher;

import java.util.List;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanNode;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Contract;

@Experimental
@Data
public class SpecSpansGraphMatcher {

    final SpecSpansGraph patternGraph;

    public SpecSpansGraphMatcher(SpecSpansGraph patternGraph) {
        if (patternGraph.getRoots().isEmpty()) {
            throw new IllegalArgumentException("patternGraph must have roots");
        }
        this.patternGraph = patternGraph;
    }

    @Contract("null -> false")
    public boolean matches(@Nullable SpecSpansGraph graph) {
        if (graph == null) {
            return false;
        }

        return matches(graph.getRoots(), patternGraph.getRoots());
    }

    private static boolean matches(List<SpecSpanNode> nodes, List<SpecSpanNode> patternNodes) {
        if (nodes.size() != patternNodes.size()) {
            return false;
        }

        for (int index = 0; index < nodes.size(); ++index) {
            val node = nodes.get(index);
            val patternNode = patternNodes.get(index);
            if (!new SpecSpanInfoMatcher(patternNode).matches(node)) {
                return false;
            }
            if (!matches(node.getChildren(), patternNode.getChildren())) {
                return false;
            }
        }

        return true;
    }

}
