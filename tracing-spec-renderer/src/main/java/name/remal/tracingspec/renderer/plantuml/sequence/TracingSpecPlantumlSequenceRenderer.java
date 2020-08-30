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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanNode;
import name.remal.tracingspec.model.SpecSpanNodeVisitor;
import name.remal.tracingspec.model.SpecSpansGraph;
import name.remal.tracingspec.renderer.plantuml.BaseTracingSpecPlantumlRenderer;

public class TracingSpecPlantumlSequenceRenderer extends BaseTracingSpecPlantumlRenderer {

    @Override
    @SuppressWarnings("java:S3776")
    protected String renderSpecSpansGraph(SpecSpansGraph specSpansGraph) {
        List<String> diagram = new ArrayList<>();
        diagram.add("@startuml");
        specSpansGraph.visit(new SpecSpanNodeVisitor() {
            @Override
            public void visit(SpecSpanNode node) {
                val serviceName = node.getServiceName();

                val previousNode = node.getPrevious();
                if (previousNode != null) {
                    if (Objects.equals(previousNode.getServiceName(), serviceName)) {
                        diagram.add("|||");
                    }
                }

                if (node.getParent() == null) {
                    diagram.add(format(
                        "[%s %s: %s",
                        node.isSync() ? "->" : "->>",
                        quoteString(serviceName),
                        escapeString(node.getName())
                    ));
                } else {
                    diagram.add(format(
                        "%s %s %s: %s",
                        quoteString(node.getParent().getServiceName()),
                        node.isSync() ? "->" : "->>",
                        quoteString(serviceName),
                        escapeString(node.getName())
                    ));
                }

                if (!isNotEmpty(node.getDescription())) {
                    diagram.add(format("note right: %s", escapeString(node.getDescription())));
                }

                diagram.add(format("activate %s", quoteString(serviceName)));
            }

            @Override
            public void postVisit(SpecSpanNode node) {
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
