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

package name.remal.tracingspec.model;

import java.util.ArrayList;
import java.util.List;
import lombok.val;
import name.remal.tracingspec.model.ImmutableSpecSpansGraphNode.SpecSpansGraphNodeBuilder;

public abstract class SpecSpansGraphs {

    public static SpecSpansGraph createSpecSpansGraph(Iterable<? extends SpecSpan> specSpans) {
        List<SpecSpan> specSpanList = new ArrayList<>();
        specSpans.forEach(specSpanList::add);

        val graphBuilder = SpecSpansGraph.builder();

        for (val specSpan : specSpanList) {
            if (specSpan.hasNoParentSpanId()) {
                val rootNodeBuilder = SpecSpansGraphNode.builder().from(specSpan);
                processChildren(
                    specSpan.getSpanId(),
                    rootNodeBuilder,
                    specSpanList
                );
                val rootNode = rootNodeBuilder.build();
                graphBuilder.addRoot(rootNode);
            }
        }

        return graphBuilder.build();
    }

    private static void processChildren(
        String nodeSpanId,
        SpecSpansGraphNodeBuilder nodeBuilder,
        List<SpecSpan> specSpanList
    ) {
        for (val specSpan : specSpanList) {
            if (nodeSpanId.equals(specSpan.getParentSpanId().orElse(null))) {
                val childNodeBuilder = SpecSpansGraphNode.builder().from(specSpan);
                processChildren(
                    specSpan.getSpanId(),
                    childNodeBuilder,
                    specSpanList
                );
                val childNode = childNodeBuilder.build();
                nodeBuilder.addChild(childNode);
            }
        }
    }


    private SpecSpansGraphs() {
    }

}
