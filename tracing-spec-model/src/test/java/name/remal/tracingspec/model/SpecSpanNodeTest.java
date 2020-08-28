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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.test.json.ObjectMapperProvider.readJsonResource;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import java.util.ArrayList;
import java.util.List;
import lombok.Value;
import lombok.val;
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
    void compareTo() {
        assertThat(
            nextSpecSpanNode().compareTo(nextSpecSpanNode()),
            equalTo(0)
        );
        assertThat(
            nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(1))).compareTo(nextSpecSpanNode()),
            equalTo(-1)
        );
        assertThat(
            nextSpecSpanNode().compareTo(nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(1)))),
            equalTo(1)
        );
        assertThat(
            nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(2)))
                .compareTo(nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(1)))),
            equalTo(1)
        );
        assertThat(
            nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(1)))
                .compareTo(nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(2)))),
            equalTo(-1)
        );
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
    void visit() {
        val parent = nextSpecSpanNode();

        val child1 = nextSpecSpanNode();
        parent.addChild(child1);

        val child11 = nextSpecSpanNode();
        child1.addChild(child11);

        val child111 = nextSpecSpanNode();
        child11.addChild(child111);

        val child112 = nextSpecSpanNode();
        child11.addChild(child112);

        val child2 = nextSpecSpanNode();
        parent.addChild(child2);


        @Value
        class MethodInvocation {
            String methodName;
            SpecSpanNode node;
        }

        List<MethodInvocation> methodInvocations = new ArrayList<>();
        parent.visit(new SpecSpanNodeVisitor() {
            @Override
            public void visit(SpecSpanNode node) {
                methodInvocations.add(new MethodInvocation("visit", node));
            }

            @Override
            public void postVisit(SpecSpanNode node) {
                methodInvocations.add(new MethodInvocation("postVisit", node));
            }
        });

        assertThat(methodInvocations, contains(
            new MethodInvocation("visit", parent),
            new MethodInvocation("visit", child1),
            new MethodInvocation("visit", child11),
            new MethodInvocation("visit", child111),
            new MethodInvocation("postVisit", child111),
            new MethodInvocation("visit", child112),
            new MethodInvocation("postVisit", child112),
            new MethodInvocation("postVisit", child11),
            new MethodInvocation("postVisit", child1),
            new MethodInvocation("visit", child2),
            new MethodInvocation("postVisit", child2),
            new MethodInvocation("postVisit", parent)
        ));
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
