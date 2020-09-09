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

import static java.util.Objects.requireNonNull;
import static name.remal.gradle_plugins.api.BuildTimeConstants.getClassSimpleName;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanNode;
import name.remal.tracingspec.model.SpecSpanNodeVisitor;
import name.remal.tracingspec.model.SpecSpansGraph;
import name.remal.tracingspec.renderer.BaseTracingSpecRenderer;
import name.remal.tracingspec.renderer.plantuml.BaseTracingSpecPlantumlRenderer;
import org.jetbrains.annotations.Contract;

public class TracingSpecPlantumlSequenceRenderer extends BaseTracingSpecPlantumlRenderer {

    @Override
    @SuppressWarnings({"java:S3776", "java:S1854"})
    protected String renderSpecSpansGraph(SpecSpansGraph graph) {
        preprocessGraph(graph);

        val diagram = new Diagram();
        Deque<Message> messagesStack = new ArrayDeque<>();

        val rootAndAsyncNodes = collectRootAndAsyncNodes(graph);
        rootAndAsyncNodes.forEach(topLevelNode -> topLevelNode.visit(new SpecSpanNodeVisitor() {
            @Override
            public boolean filterNode(SpecSpanNode node) {
                return node.isRoot() || node.isSync() || node == topLevelNode;
            }

            @Override
            public void visit(SpecSpanNode node) {
                val lastMessage = messagesStack.peekLast();
                if (lastMessage == null) {
                    val rootMessage = diagram.newRoot(node);
                    messagesStack.addLast(rootMessage);
                } else {
                    val childMessage = lastMessage.newChild(node);
                    messagesStack.addLast(childMessage);
                }
            }

            @Override
            public void postVisit(SpecSpanNode node) {
                val lastMessage = messagesStack.pollLast();
                if (lastMessage == null) {
                    throw new IllegalStateException("messagesStack is empty");
                }
            }
        }));

        return diagram.toString();
    }


    private static void preprocessGraph(SpecSpansGraph graph) {
        clearRemoteServiceNameIfItEqualsToServiceName(graph);
        addIntermediateNodes(graph);
    }

    private static void clearRemoteServiceNameIfItEqualsToServiceName(SpecSpansGraph graph) {
        graph.visit(node -> {
            if (Objects.equals(node.getServiceName(), node.getRemoteServiceName())) {
                node.setRemoteServiceName(null);
            }
        });
    }

    @SuppressWarnings("java:S3776")
    private static void addIntermediateNodes(SpecSpansGraph graph) {
        graph.visit(parent ->
            new ArrayList<>(parent.getChildren()).forEach(child -> {
                if (child.isAsync()) {
                    return;
                }
                if (getTargetServiceName(child) == null) {
                    // Child has target
                    return;
                }

                val intermediateChild = new SpecSpanNode();
                intermediateChild.setHidden(true);

                val childSourceServiceName = getSourceServiceName(child);
                val parentTargetServiceName = getTargetServiceName(parent);
                if (parentTargetServiceName != null) {
                    if (parentTargetServiceName.equals(childSourceServiceName)) {
                        // Parent has a target that is the same as child's source
                        return;
                    } else {
                        intermediateChild.setServiceName(parentTargetServiceName);
                        intermediateChild.setRemoteServiceName(childSourceServiceName);
                    }

                } else {
                    val parentSourceServiceName = getSourceServiceName(parent);
                    if (Objects.equals(parentSourceServiceName, childSourceServiceName)) {
                        // Parent has *no* target and has the same source
                        return;
                    } else {
                        intermediateChild.setServiceName(parentSourceServiceName);
                        intermediateChild.setRemoteServiceName(childSourceServiceName);
                    }
                }

                parent.addChildAfter(intermediateChild, child);
                child.setParent(intermediateChild);
            })
        );
    }


    private static String getSourceServiceName(SpecSpanNode node) {
        val kind = node.getKind();
        val remoteServiceName = node.getRemoteServiceName();
        if (kind != null && kind.isRemoteSource()) {
            if (remoteServiceName != null) {
                return remoteServiceName;
            }
        }

        return requireNonNull(
            node.getServiceName(),
            getClassSimpleName(BaseTracingSpecRenderer.class) + " doesn't allow service name to be NULL"
        );
    }

    @Nullable
    private static String getTargetServiceName(SpecSpanNode node) {
        val kind = node.getKind();
        if (kind != null && kind.isRemoteSource()) {
            val serviceName = requireNonNull(
                node.getServiceName(),
                getClassSimpleName(BaseTracingSpecRenderer.class) + " doesn't allow service name to be NULL"
            );
            return serviceName;
        }

        return node.getRemoteServiceName();
    }


    private static List<SpecSpanNode> collectRootAndAsyncNodes(SpecSpansGraph graph) {
        List<SpecSpanNode> rootAndAsyncNodes = new ArrayList<>();
        graph.visit(node -> {
            if (node.isRoot() || node.isAsync()) {
                rootAndAsyncNodes.add(node);
            }
        });
        return rootAndAsyncNodes;
    }


    private class Diagram {

        public Message newRoot(SpecSpanNode node) {
            val child = new Message(null, node);
            this.children.add(child);
            return child;
        }


        private final List<Message> children = new ArrayList<>();

        @Override
        public String toString() {
            val sb = new StringBuilder();
            sb.append("@startuml");
            sb.append("\nskinparam maxmessagesize 500");
            sb.append("\nskinparam responseMessageBelowArrow true");
            children.forEach(sb::append);
            sb.append("\n@enduml");
            return sb.toString();
        }

    }

    private class Message {

        @Contract("_ -> new")
        public Message newChild(SpecSpanNode node) {
            val child = new Message(this, node);
            this.children.add(child);
            return child;
        }


        @Nullable
        private final Message parent;

        private final List<Message> children = new ArrayList<>();


        private final boolean minimized;

        private final boolean async;

        @Nullable
        private final String sourceServiceName;

        private final String targetServiceName;

        @Nullable
        private final String name;

        @Nullable
        private final String description;

        private final Map<String, String> tags = new LinkedHashMap<>();


        @SuppressWarnings("java:S3776")
        private Message(@Nullable Message parent, SpecSpanNode node) {
            this.parent = parent;

            this.minimized = node.isHidden();
            this.async = node.isAsync();

            val curSourceServiceName = getSourceServiceName(node);
            val curTargetServiceName = getTargetServiceName(node);
            val parentNode = node.getParent();
            if (parentNode == null || curTargetServiceName != null) {
                this.sourceServiceName = curSourceServiceName;
                this.targetServiceName = curTargetServiceName != null ? curTargetServiceName : curSourceServiceName;
            } else {
                val parentTargetServiceName = getTargetServiceName(parentNode);
                if (parentTargetServiceName != null) {
                    this.sourceServiceName = parentTargetServiceName;
                    this.targetServiceName = curSourceServiceName;
                } else {
                    val parentSourceServiceName = getSourceServiceName(parentNode);
                    if (curSourceServiceName.equals(parentSourceServiceName)) {
                        this.sourceServiceName = curSourceServiceName;
                        this.targetServiceName = curSourceServiceName;
                    } else {
                        this.sourceServiceName = parentSourceServiceName;
                        this.targetServiceName = curSourceServiceName;
                    }
                }
            }

            if (this.minimized) {
                this.name = null;
                this.description = null;

            } else {
                this.name = node.getName();
                this.description = node.getDescription();
                node.getTags().forEach((tagName, tagValue) -> {
                    if (isNotEmpty(tagName)
                        && tagValue != null
                        && isDisplayableTag(tagName)
                    ) {
                        this.tags.put(tagName, tagValue);
                    }
                });
            }
        }

        @Override
        @SuppressWarnings("java:S3776")
        public String toString() {
            val hidden = minimized
                && parent != null
                && !async
                && Objects.equals(sourceServiceName, targetServiceName);
            if (hidden) {
                val sb = new StringBuilder();
                children.forEach(sb::append);
                return sb.toString();
            }

            val sb = new StringBuilder();
            sb.append('\n');

            if (parent == null && Objects.equals(sourceServiceName, targetServiceName)) {
                sb.append('[');
            } else if (sourceServiceName != null) {
                sb.append(quoteString(sourceServiceName));
                sb.append(' ');
            } else {
                sb.append('[');
            }
            if (async) {
                sb.append("->>");
            } else {
                sb.append("->");
            }
            sb.append(' ');
            sb.append(quoteString(targetServiceName));

            if (isNotEmpty(name)) {
                sb.append(": ").append(escapeString(name));
            }
            tags.forEach((tagName, tagValue) ->
                sb.append("\\n<size:10>")
                    .append(escapeString(tagName))
                    .append('=')
                    .append(escapeString(tagValue))
                    .append("</size>")
            );

            val sourceShouldBeActivated = parent == null
                && !async
                && !Objects.equals(sourceServiceName, targetServiceName);
            if (sourceShouldBeActivated) {
                sb.append("\nactivate ").append(quoteString(sourceServiceName));
            }

            sb.append("\nactivate ").append(quoteString(targetServiceName));

            if (isNotEmpty(description)) {
                sb.append("\nnote right: ").append(escapeString(description));
            }

            children.forEach(sb::append);

            if (async) {
                sb.append("\ndeactivate ").append(quoteString(targetServiceName));
            } else {
                sb.append("\nreturn");
                if (isNotEmpty(name)) {
                    sb.append(" <size:9>").append(escapeString(name)).append("</size>");
                }
            }

            if (sourceShouldBeActivated) {
                sb.append("\ndeactivate ").append(quoteString(sourceServiceName));
            }

            return sb.toString();
        }

    }

}
