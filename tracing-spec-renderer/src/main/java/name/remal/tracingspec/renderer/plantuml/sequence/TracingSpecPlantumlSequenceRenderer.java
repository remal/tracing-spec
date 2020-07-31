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

package name.remal.tracingspec.renderer.plantuml.sequence;

import static java.lang.String.format;
import static name.remal.tracingspec.model.SpecSpansGraphUtils.getPreviousNodeFor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import name.remal.tracingspec.model.SpecSpansGraphNode;
import name.remal.tracingspec.model.SpecSpansGraphVisitor;
import name.remal.tracingspec.renderer.plantuml.BaseTracingSpecPlantumlRenderer;

public class TracingSpecPlantumlSequenceRenderer extends BaseTracingSpecPlantumlRenderer {

    @Override
    @SuppressWarnings("java:S3776")
    protected String renderFilteredTracingSpec(SpecSpansGraph specSpansGraph) {
        List<String> diagram = new ArrayList<>();
        diagram.add("@startuml");
        specSpansGraph.visit(new SpecSpansGraphVisitor() {
            @Override
            public void visitNode(SpecSpansGraphNode node, @Nullable SpecSpansGraphNode parentNode) {
                val serviceName = node.getServiceName();

                val previousNode = getPreviousNodeFor(node, parentNode);
                if (previousNode != null) {
                    if (previousNode.getServiceName().equals(serviceName)) {
                        diagram.add("|||");
                    }
                }

                if (parentNode == null) {
                    diagram.add(format(
                        "[%s %s: %s",
                        node.isSync() ? "->" : "->>",
                        quoteString(serviceName),
                        escapeString(node.getName())
                    ));
                } else {
                    diagram.add(format(
                        "%s %s %s: %s",
                        quoteString(parentNode.getServiceName()),
                        node.isSync() ? "->" : "->>",
                        quoteString(serviceName),
                        escapeString(node.getName())
                    ));
                }

                node.getDescription().ifPresent(description ->
                    diagram.add(format("note right: %s", escapeString(description)))
                );

                diagram.add(format("activate %s", quoteString(serviceName)));
            }

            @Override
            public void postVisitNode(SpecSpansGraphNode node, @Nullable SpecSpansGraphNode parentNode) {
                if (node.isSync()) {
                    diagram.add("return");
                } else {
                    diagram.add(format("deactivate %s", quoteString(node.getServiceName())));
                }
            }
        });
        diagram.add("@enduml");
        return String.join("\n", diagram);
    }

}
