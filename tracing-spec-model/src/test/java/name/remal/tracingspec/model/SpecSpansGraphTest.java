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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.test.json.ObjectMapperProvider.readJsonResource;
import static utils.test.json.ObjectMapperProvider.writeJsonString;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import java.util.ArrayList;
import java.util.List;
import lombok.val;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SpecSpansGraphTest {

    private final SpecSpansGraph graph = new SpecSpansGraph();

    @Test
    @SuppressWarnings("ConstantConditions")
    void getRoots() {
        val roots = graph.getRoots();
        val root = nextSpecSpanNode();
        assertThrows(UnsupportedOperationException.class, () -> roots.add(root));
    }

    @Test
    void setRoots() {
        val root1 = nextSpecSpanNode();
        graph.setRoots(singletonList(root1));
        assertThat(graph.getRoots(), contains(root1));

        val root2 = nextSpecSpanNode();
        graph.setRoots(singletonList(root2));
        assertThat(graph.getRoots(), contains(root2));
    }

    @Test
    void addRoot() {
        val root = nextSpecSpanNode();
        val rootInitialParent = nextSpecSpanNode();
        root.setParent(rootInitialParent);

        graph.addRoot(root);
        assertThat(graph.getRoots(), contains(root));
        assertThat(root.getParent(), nullValue());

        val otherRoot = nextSpecSpanNode();
        graph.addRoot(otherRoot);
        assertThat(graph.getRoots(), contains(root, otherRoot));

        graph.addRoot(root);
        assertThat(graph.getRoots(), contains(otherRoot, root));
    }

    @Test
    void removeRoot() {
        val root1 = nextSpecSpanNode();
        graph.addRoot(root1);
        val root2 = nextSpecSpanNode();
        graph.addRoot(root2);

        graph.removeRoot(root1);
        assertThat(graph.getRoots(), contains(root2));
    }

    @Test
    void sortChildren() {
        val root2 = nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(2)));
        graph.addRoot(root2);

        val root1 = nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(1)));
        graph.addRoot(root1);

        val child12 = nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(12)));
        child12.setParent(root1);

        val child11 = nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(11)));
        child11.setParent(root1);
        val child112 = nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(112)));
        child112.setParent(child11);
        val child111 = nextSpecSpanNode(node -> node.setStartedAt(ofEpochSecond(111)));
        child111.setParent(child11);

        graph.sort();

        assertThat(graph.getRoots(), contains(root1, root2));
        assertThat(root1.getChildren(), contains(child11, child12));
        assertThat(child11.getChildren(), contains(child111, child112));
    }

    @Test
    void visit() {
        val root1 = nextSpecSpanNode();
        graph.addRoot(root1);

        val child1 = nextSpecSpanNode();
        root1.addChild(child1);

        val root2 = nextSpecSpanNode();
        graph.addRoot(root2);

        List<SpecSpanNode> visitedNodes = new ArrayList<>();
        graph.visit(new SpecSpanNodeVisitor() {
            @Override
            public void visit(SpecSpanNode node) {
                visitedNodes.add(node);
            }
        });

        assertThat(visitedNodes, contains(root1, child1, root2));
    }


    @Nested
    class Json {

        @Test
        void empty() {
            val graph = new SpecSpansGraph();
            match("graph-empty.json", graph);
        }

        @Test
        void roots_only() {
            val graph = new SpecSpansGraph()
                .addRoot(nextSpecSpanNode("name A"))
                .addRoot(nextSpecSpanNode("name B"));
            match("graph-roots-only.json", graph);
        }

        @Test
        void with_children() {
            val graph = new SpecSpansGraph();
            graph.addRoot(nextSpecSpanNode("root", root ->
                root.addChild(nextSpecSpanNode("parent", parent ->
                    parent.addChild(nextSpecSpanNode("child")))
                )
            ));
            match("graph-with-children.json", graph);
        }

        private void match(
            @Language("file-reference") String jsonResourceName,
            SpecSpansGraph graph
        ) {
            assertThat(
                writeJsonString(graph),
                equalTo(writeJsonString(
                    readJsonResource(jsonResourceName, Object.class)
                ))
            );

            assertThat(
                readJsonResource(jsonResourceName, SpecSpansGraph.class),
                equalTo(graph)
            );
        }

    }

}
