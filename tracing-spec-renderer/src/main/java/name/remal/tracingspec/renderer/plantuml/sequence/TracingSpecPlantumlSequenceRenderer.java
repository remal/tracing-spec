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
import static java.util.Objects.requireNonNull;

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
    @SuppressWarnings({"java:S3776", "java:S1854"})
    protected String renderSpecSpansGraph(SpecSpansGraph graph) {
        List<String> diagram = new ArrayList<>();
        diagram.add("@startuml");

        val rootAndAsyncNodes = collectRootAndAsyncNodes(graph);
        rootAndAsyncNodes.forEach(topLevelNode -> topLevelNode.visit(new SpecSpanNodeVisitor() {
            @Override
            public boolean filterNode(SpecSpanNode node) {
                return node.isRoot() || node.isSync() || node == topLevelNode;
            }

            @Override
            public void visit(SpecSpanNode node) {
                if (node.isRoot()) {
                    if (node.isSync()) {
                        visitSyncRoot(node);
                    } else {
                        visitAsyncRoot(node);
                    }

                } else {
                    if (node.isSync()) {
                        visitSyncChild(requireNonNull(node.getParent()), node);
                    } else {
                        visitAsyncChild(requireNonNull(node.getParent()), node);
                    }
                }
            }

            @Override
            public void postVisit(SpecSpanNode node) {
                if (node.isRoot()) {
                    if (node.isSync()) {
                        postVisitSyncRoot(node);
                    } else {
                        postVisitAsyncRoot(node);
                    }

                } else {
                    if (node.isSync()) {
                        postVisitSyncChild(requireNonNull(node.getParent()), node);
                    } else {
                        postVisitAsyncChild(requireNonNull(node.getParent()), node);
                    }
                }
            }


            private void visitSyncRoot(SpecSpanNode root) {
                if (root.getRemoteServiceName() == null
                    || Objects.equals(root.getServiceName(), root.getRemoteServiceName())
                ) {
                    diagram.add(format(
                        "[-> %s: %s%s",
                        quoteString(root.getServiceName()),
                        escapeString(root.getName()),
                        renderTags(root)
                    ));
                    diagram.add(format(
                        "activate %s",
                        quoteString(root.getServiceName())
                    ));

                } else {
                    diagram.add(format(
                        "%s -> %s: %s%s",
                        quoteString(root.getServiceName()),
                        quoteString(root.getRemoteServiceName()),
                        escapeString(root.getName()),
                        renderTags(root)
                    ));
                    diagram.add(format(
                        "activate %s",
                        quoteString(root.getServiceName())
                    ));
                    diagram.add(format(
                        "activate %s",
                        quoteString(root.getRemoteServiceName())
                    ));
                }
            }

            private void postVisitSyncRoot(SpecSpanNode root) {
                diagram.add(format("return <size:9>%s</size>", escapeString(root.getName())));

                if (root.getRemoteServiceName() != null
                    && !Objects.equals(root.getServiceName(), root.getRemoteServiceName())
                ) {
                    diagram.add(format(
                        "deactivate %s",
                        quoteString(root.getServiceName())
                    ));
                }
            }


            private void visitSyncChild(SpecSpanNode parent, SpecSpanNode child) {
                if (child.getRemoteServiceName() != null
                    && !Objects.equals(child.getServiceName(), child.getRemoteServiceName())
                    && !Objects.equals(child.getServiceName(), parent.getServiceName())
                    && !Objects.equals(child.getServiceName(), parent.getRemoteServiceName())
                ) {
                    boolean isActivated = false;
                    SpecSpanNode prev = child.getPrevious();
                    while (prev != null) {
                        if (prev.isSync() && Objects.equals(child.getServiceName(), prev.getServiceName())) {
                            isActivated = true;
                            break;
                        }
                        prev = prev.getPrevious();
                    }
                    if (!isActivated) {
                        diagram.add(format(
                            "%s -> %s",
                            quoteString(parent.getServiceName()),
                            quoteString(child.getServiceName())
                        ));
                        diagram.add(format(
                            "activate %s",
                            quoteString(child.getServiceName())
                        ));
                    }
                }

                if (child.getRemoteServiceName() == null
                    || Objects.equals(child.getServiceName(), child.getRemoteServiceName())
                ) {
                    final String parentServiceName;
                    if (parent.getKind() != null
                        && parent.getKind().isRemoteSource()
                        && parent.getServiceName() != null
                    ) {
                        parentServiceName = parent.getServiceName();
                    } else if (parent.getRemoteServiceName() != null) {
                        parentServiceName = parent.getRemoteServiceName();
                    } else {
                        parentServiceName = parent.getServiceName();
                    }
                    diagram.add(format(
                        "%s -> %s: %s%s",
                        quoteString(parentServiceName),
                        quoteString(child.getServiceName()),
                        escapeString(child.getName()),
                        renderTags(child)
                    ));
                    diagram.add(format(
                        "activate %s",
                        quoteString(child.getServiceName())
                    ));

                } else {
                    diagram.add(format(
                        "%s -> %s: %s%s",
                        quoteString(child.getServiceName()),
                        quoteString(child.getRemoteServiceName()),
                        escapeString(child.getName()),
                        renderTags(child)
                    ));
                    diagram.add(format(
                        "activate %s",
                        quoteString(child.getRemoteServiceName())
                    ));
                }
            }

            private void postVisitSyncChild(SpecSpanNode parent, SpecSpanNode child) {
                diagram.add(format("return <size:9>%s</size>", escapeString(child.getName())));

                if (child.getRemoteServiceName() != null
                    && !Objects.equals(child.getServiceName(), child.getRemoteServiceName())
                    && !Objects.equals(child.getServiceName(), parent.getServiceName())
                    && !Objects.equals(child.getServiceName(), parent.getRemoteServiceName())
                ) {
                    boolean isLast = true;
                    SpecSpanNode next = child.getNext();
                    while (next != null) {
                        if (next.isSync() && Objects.equals(child.getServiceName(), next.getServiceName())) {
                            isLast = false;
                            break;
                        }
                        next = next.getNext();
                    }
                    if (isLast) {
                        diagram.add("return");
                    }
                }
            }


            private void visitAsyncRoot(SpecSpanNode root) {
                if (root.getRemoteServiceName() == null
                    || Objects.equals(root.getServiceName(), root.getRemoteServiceName())
                ) {
                    diagram.add(format(
                        "[->> %s: %s%s",
                        quoteString(root.getServiceName()),
                        escapeString(root.getName()),
                        renderTags(root)
                    ));
                    diagram.add(format(
                        "activate %s",
                        quoteString(root.getServiceName())
                    ));

                } else if (root.getKind() != null && root.getKind().isRemoteSource()) {
                    diagram.add(format(
                        "%s ->> %s: %s%s",
                        quoteString(root.getRemoteServiceName()),
                        quoteString(root.getServiceName()),
                        escapeString(root.getName()),
                        renderTags(root)
                    ));
                    diagram.add(format(
                        "activate %s",
                        quoteString(root.getServiceName())
                    ));

                } else {
                    diagram.add(format(
                        "%s ->> %s: %s%s",
                        quoteString(root.getServiceName()),
                        quoteString(root.getRemoteServiceName()),
                        escapeString(root.getName()),
                        renderTags(root)
                    ));
                    diagram.add(format(
                        "activate %s",
                        quoteString(root.getRemoteServiceName())
                    ));
                }
            }

            private void postVisitAsyncRoot(SpecSpanNode root) {
                if (root.getRemoteServiceName() == null
                    || Objects.equals(root.getServiceName(), root.getRemoteServiceName())
                ) {
                    diagram.add(format(
                        "deactivate %s",
                        quoteString(root.getServiceName())
                    ));

                } else if (root.getKind() != null && root.getKind().isRemoteSource()) {
                    diagram.add(format(
                        "deactivate %s",
                        quoteString(root.getServiceName())
                    ));

                } else {
                    diagram.add(format(
                        "deactivate %s",
                        quoteString(root.getRemoteServiceName())
                    ));
                }
            }


            private void visitAsyncChild(SpecSpanNode parent, SpecSpanNode child) {
                if (child.getRemoteServiceName() == null
                    || Objects.equals(child.getServiceName(), child.getRemoteServiceName())
                ) {
                    diagram.add(format(
                        "%s ->> %s: %s%s",
                        quoteString(parent.getServiceName()),
                        quoteString(child.getServiceName()),
                        escapeString(child.getName()),
                        renderTags(child)
                    ));
                    diagram.add(format(
                        "activate %s",
                        quoteString(child.getServiceName())
                    ));

                } else if (child.getKind() != null && child.getKind().isRemoteSource()) {
                    diagram.add(format(
                        "%s ->> %s: %s%s",
                        quoteString(child.getRemoteServiceName()),
                        quoteString(child.getServiceName()),
                        escapeString(child.getName()),
                        renderTags(child)
                    ));
                    diagram.add(format(
                        "activate %s",
                        quoteString(child.getServiceName())
                    ));

                } else {
                    diagram.add(format(
                        "%s ->> %s: %s%s",
                        quoteString(child.getServiceName()),
                        quoteString(child.getRemoteServiceName()),
                        escapeString(child.getName()),
                        renderTags(child)
                    ));
                    diagram.add(format(
                        "activate %s",
                        quoteString(child.getRemoteServiceName())
                    ));
                }
            }

            @SuppressWarnings("java:S1172")
            private void postVisitAsyncChild(SpecSpanNode parent, SpecSpanNode child) {
                if (child.getRemoteServiceName() == null
                    || Objects.equals(child.getServiceName(), child.getRemoteServiceName())
                ) {
                    diagram.add(format(
                        "deactivate %s",
                        quoteString(child.getServiceName())
                    ));

                } else if (child.getKind() != null && child.getKind().isRemoteSource()) {
                    diagram.add(format(
                        "deactivate %s",
                        quoteString(child.getServiceName())
                    ));

                } else {
                    diagram.add(format(
                        "deactivate %s",
                        quoteString(child.getRemoteServiceName())
                    ));
                }
            }
        }));

        diagram.add("@enduml");
        return String.join("\n", diagram);
    }

    private String renderTags(SpecSpanNode node) {
        val sb = new StringBuilder();
        node.getTags().forEach((key, value) -> {
            if (isDisplayableTag(key)) {
                sb.append("\\n<size:10>")
                    .append(escapeString(key))
                    .append('=')
                    .append(escapeString(value))
                    .append("</size>");
            }
        });
        return sb.toString();
    }

    private static List<SpecSpanNode> collectRootAndAsyncNodes(SpecSpansGraph graph) {
        List<SpecSpanNode> rootAndAsyncNodes = new ArrayList<>();
        graph.visit(new SpecSpanNodeVisitor() {
            @Override
            public void visit(SpecSpanNode node) {
                if (node.isRoot() || node.isAsync()) {
                    rootAndAsyncNodes.add(node);
                }
            }
        });
        return rootAndAsyncNodes;
    }

}
