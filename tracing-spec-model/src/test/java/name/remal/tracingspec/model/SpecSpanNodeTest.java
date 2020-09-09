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

import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.singletonList;
import static name.remal.tracingspec.model.SpecSpanKind.CONSUMER;
import static name.remal.tracingspec.model.SpecSpanKind.PRODUCER;
import static name.remal.tracingspec.model.SpecSpanKind.SERVER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.test.json.ObjectMapperProvider.readJsonResource;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SpecSpanNodeTest extends SpecSpanInfoTest<SpecSpanNode> {

    @Test
    void setParent() {
        val child = nextSpecSpanNode();
        val parent1 = nextSpecSpanNode();
        child.setParent(parent1);
        assertThat(child.getParent(), equalTo(parent1));
        assertThat(parent1.getChildren(), contains(child));

        val parent2 = nextSpecSpanNode();
        child.setParent(parent2);
        assertThat(child.getParent(), equalTo(parent2));
        assertThat(parent1.getChildren(), empty());
        assertThat(parent2.getChildren(), contains(child));

        child.setParent(null);
        assertThat(child.getParent(), nullValue());
        assertThat(parent2.getChildren(), empty());
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void getChildren() {
        val parent = nextSpecSpanNode();
        val children = parent.getChildren();
        val child = nextSpecSpanNode();
        assertThrows(UnsupportedOperationException.class, () -> children.add(child));
    }

    @Test
    void setChildren() {
        val parent = nextSpecSpanNode();
        val child1 = nextSpecSpanNode();
        parent.setChildren(singletonList(child1));
        assertThat(child1.getParent(), equalTo(parent));
        assertThat(parent.getChildren(), contains(child1));

        val child2 = nextSpecSpanNode();
        parent.setChildren(singletonList(child2));
        assertThat(child1.getParent(), nullValue());
        assertThat(child2.getParent(), equalTo(parent));
        assertThat(parent.getChildren(), contains(child2));
    }

    @Test
    void addChild() {
        val child = nextSpecSpanNode();
        val parent1 = nextSpecSpanNode();
        parent1.addChild(child);
        assertThat(child.getParent(), equalTo(parent1));
        assertThat(parent1.getChildren(), contains(child));

        val parent2 = nextSpecSpanNode();
        parent2.addChild(child);
        assertThat(child.getParent(), equalTo(parent2));
        assertThat(parent1.getChildren(), empty());
        assertThat(parent2.getChildren(), contains(child));

        val otherChild = nextSpecSpanNode();
        parent2.addChild(otherChild);
        assertThat(parent2.getChildren(), contains(child, otherChild));

        parent2.addChild(child);
        assertThat(parent2.getChildren(), contains(otherChild, child));
    }

    @Test
    void addChildAfter() {
        val parent = nextSpecSpanNode();
        val child1 = nextSpecSpanNode(it -> it.setParent(parent));
        val child2 = nextSpecSpanNode(it -> it.setParent(parent));

        val childToAdd = nextSpecSpanNode();
        assertThrows(IllegalArgumentException.class, () -> parent.addChildAfter(childToAdd, childToAdd));

        val detachedNode = nextSpecSpanNode();
        assertThrows(IllegalArgumentException.class, () -> parent.addChildAfter(childToAdd, detachedNode));

        parent.addChildAfter(childToAdd, child1);
        assertThat(parent.getChildren(), contains(
            child1,
            childToAdd,
            child2
        ));

        parent.addChildAfter(childToAdd, child2);
        assertThat(parent.getChildren(), contains(
            child1,
            child2,
            childToAdd
        ));
    }

    @Test
    void addChildBefore() {
        val parent = nextSpecSpanNode();
        val child1 = nextSpecSpanNode(it -> it.setParent(parent));
        val child2 = nextSpecSpanNode(it -> it.setParent(parent));

        val childToAdd = nextSpecSpanNode();
        assertThrows(IllegalArgumentException.class, () -> parent.addChildBefore(childToAdd, childToAdd));

        val detachedNode = nextSpecSpanNode();
        assertThrows(IllegalArgumentException.class, () -> parent.addChildBefore(childToAdd, detachedNode));

        parent.addChildBefore(childToAdd, child1);
        assertThat(parent.getChildren(), contains(
            childToAdd,
            child1,
            child2
        ));

        parent.addChildBefore(childToAdd, child2);
        assertThat(parent.getChildren(), contains(
            child1,
            childToAdd,
            child2
        ));
    }

    @Test
    void removeChild() {
        val parent = nextSpecSpanNode();
        val child1 = nextSpecSpanNode();
        parent.addChild(child1);
        val child2 = nextSpecSpanNode();
        parent.addChild(child2);

        parent.removeChild(child1);
        assertThat(child1.getParent(), nullValue());
        assertThat(parent.getChildren(), contains(child2));
    }

    @Test
    void getPrevious() {
        val child = nextSpecSpanNode();
        assertThat(child.getPrevious(), nullValue());

        val parent = nextSpecSpanNode();
        parent.addChild(child);
        assertThat(child.getPrevious(), nullValue());

        val otherChild = nextSpecSpanNode();
        parent.addChild(otherChild);
        assertThat(child.getPrevious(), nullValue());

        parent.removeChild(child);
        parent.addChild(child);
        assertThat(child.getPrevious(), equalTo(otherChild));

        child.setParent(null);
        assertThat(child.getPrevious(), nullValue());
    }

    @Test
    void getNext() {
        val child = nextSpecSpanNode();
        assertThat(child.getNext(), nullValue());

        val parent = nextSpecSpanNode();
        parent.addChild(child);
        assertThat(child.getNext(), nullValue());

        val otherChild = nextSpecSpanNode();
        parent.addChild(otherChild);
        assertThat(child.getNext(), equalTo(otherChild));

        parent.removeChild(child);
        parent.addChild(child);
        assertThat(child.getNext(), nullValue());

        child.setParent(null);
        assertThat(child.getNext(), nullValue());
    }

    @Test
    void sortChildren() {
        val parent = nextSpecSpanNode();

        val child2 = nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(2)));
        child2.setParent(parent);

        val child1 = nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(1)));
        child1.setParent(parent);
        val child12 = nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(12)));
        child12.setParent(child1);
        val child11 = nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(11)));
        child11.setParent(child1);

        parent.sortChildren();

        assertThat(parent.getChildren(), contains(child1, child2));
        assertThat(child1.getChildren(), contains(child11, child12));
    }

    @Test
    void append() {
        val parent = nextSpecSpanNode();

        val node = nextSpecSpanNode(it -> {
            it.setParent(parent);
            it.setName(null);
        });
        val child = nextSpecSpanNode(it -> {
            it.setParent(node);
            it.setStartedAt(ofEpochSecond(20));
        });

        val nodeToAppend = nextSpecSpanNode(it -> {
            it.setParent(parent);
            it.setName("name");
            it.setKind(SERVER);
            it.setAsync(true);
            it.setServiceName("local");
            it.setRemoteServiceName("remote");
            it.setStartedAt(ofEpochSecond(1));
            it.setDescription("description");
            it.putTag("tag", "value");
        });
        val childToAppend = nextSpecSpanNode(it -> {
            it.setParent(nodeToAppend);
            it.setStartedAt(ofEpochSecond(10));
        });

        node.append(nodeToAppend);

        assertThat(node.getChildren(), contains(child, childToAppend));
        assertThat(node.getName(), equalTo("name"));
        assertThat(node.getKind(), equalTo(SERVER));
        assertThat(node.isAsync(), equalTo(true));
        assertThat(node.getServiceName(), equalTo("local"));
        assertThat(node.getRemoteServiceName(), equalTo("remote"));
        assertThat(node.getStartedAt(), equalTo(ofEpochSecond(1)));
        assertThat(node.getDescription(), equalTo("description"));
        assertThat(node.getTags(), hasEntry("tag", "value"));

        assertThat(nodeToAppend.getParent(), nullValue());
        assertThat(nodeToAppend.getChildren(), empty());
    }


    @Nested
    class Visit {

        @Test
        void full() {
            val node = nextSpecSpanNode();
            val node1 = nextSpecSpanNode(it -> it.setParent(node));
            val node11 = nextSpecSpanNode(it -> it.setParent(node1));
            val node111 = nextSpecSpanNode(it -> it.setParent(node11));
            val node112 = nextSpecSpanNode(it -> it.setParent(node11));
            val node2 = nextSpecSpanNode(it -> it.setParent(node));

            val methodInvocations = visitAndCollectInvocations(node);
            assertThat(methodInvocations, contains(
                new VisitMethodInvocation("visit", node),
                new VisitMethodInvocation("visit", node1),
                new VisitMethodInvocation("visit", node11),
                new VisitMethodInvocation("visit", node111),
                new VisitMethodInvocation("postVisit", node111),
                new VisitMethodInvocation("visit", node112),
                new VisitMethodInvocation("postVisit", node112),
                new VisitMethodInvocation("postVisit", node11),
                new VisitMethodInvocation("postVisit", node1),
                new VisitMethodInvocation("visit", node2),
                new VisitMethodInvocation("postVisit", node2),
                new VisitMethodInvocation("postVisit", node)
            ));
        }

        @Test
        void filter_node() {
            val root = nextSpecSpanNode();
            val parent = nextSpecSpanNode(it -> it.setParent(root));
            val child = nextSpecSpanNode(it -> it.setParent(parent));

            val methodInvocations = visitAndCollectInvocations(
                root,
                node -> !node.equals(parent)
            );
            assertThat(methodInvocations, contains(
                new VisitMethodInvocation("visit", root),
                new VisitMethodInvocation("postVisit", root)
            ));
            assertThat(methodInvocations, not(hasItems(
                new VisitMethodInvocation("visit", parent),
                new VisitMethodInvocation("postVisit", parent)
            )));
            assertThat(methodInvocations, not(hasItems(
                new VisitMethodInvocation("visit", child),
                new VisitMethodInvocation("postVisit", child)
            )));
        }


        private List<VisitMethodInvocation> visitAndCollectInvocations(SpecSpanNode node) {
            return visitAndCollectInvocations(node, __ -> true);
        }

        private List<VisitMethodInvocation> visitAndCollectInvocations(
            SpecSpanNode node,
            Predicate<SpecSpanNode> predicate
        ) {
            List<VisitMethodInvocation> invocations = new ArrayList<>();
            node.visit(new SpecSpanNodeVisitor() {
                @Override
                public boolean filterNode(SpecSpanNode node) {
                    return predicate.test(node);
                }

                @Override
                public void visit(SpecSpanNode node) {
                    invocations.add(new VisitMethodInvocation("visit", node));
                }

                @Override
                public void postVisit(SpecSpanNode node) {
                    invocations.add(new VisitMethodInvocation("postVisit", node));
                }
            });
            return invocations;
        }

        @Value
        private class VisitMethodInvocation {
            String methodName;
            SpecSpanNode node;
        }

    }

    @Test
    void deserialization() {
        val parent = new SpecSpanNode();
        parent.setName("1");
        parent.setKind(PRODUCER);
        val child = new SpecSpanNode();
        child.setName("2");
        child.setKind(CONSUMER);
        child.setParent(parent);

        val deserialized = readJsonResource("spec-span-node.json", SpecSpanNode.class);
        deserialized.visit(new SpecSpanNodeVisitor() {
            @Override
            public void visit(SpecSpanNode node) {
                node.getTags().clear();
            }
        });

        assertThat(
            deserialized,
            equalTo(parent)
        );
    }

}
