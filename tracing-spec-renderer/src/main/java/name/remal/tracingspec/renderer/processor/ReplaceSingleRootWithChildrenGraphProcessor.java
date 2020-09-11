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

package name.remal.tracingspec.renderer.processor;

import java.util.ArrayList;
import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import name.remal.tracingspec.renderer.SpecSpansGraphProcessor;

public class ReplaceSingleRootWithChildrenGraphProcessor implements SpecSpansGraphProcessor {

    @Override
    public void processGraph(SpecSpansGraph graph) {
        val roots = graph.getRoots();
        if (roots.size() == 1) {
            val root = roots.get(0);
            graph.removeRoot(root);
            new ArrayList<>(root.getChildren()).forEach(graph::addRoot);
        }
    }

}
