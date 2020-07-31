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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.json.ObjectMapperProvider.readJsonResource;
import static utils.test.json.ObjectMapperProvider.writeJsonString;
import static utils.test.tracing.SpecSpanGraphNodeGenerator.nextSpecSpansGraphNodeBuilder;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Value;
import lombok.val;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SpecSpansGraphTest {

    @Test
    void visit() {
        val graph = SpecSpansGraph.builder()
            .addRoot(
                nextSpecSpansGraphNodeBuilder()
                    .name("root")
                    .addChild(
                        nextSpecSpansGraphNodeBuilder()
                            .name("parent")
                            .addChild(nextSpecSpansGraphNodeBuilder()
                                .name("child 1")
                                .build()
                            )
                            .addChild(nextSpecSpansGraphNodeBuilder()
                                .name("child 2")
                                .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();
        val root = graph.getRoots().get(0);
        val parent = root.getChildren().get(0);
        val child1 = parent.getChildren().get(0);
        val child2 = parent.getChildren().get(1);

        @Value
        class MethodInvocation {
            String methodName;
            List<Object> parameters;
        }

        List<MethodInvocation> invocations = new ArrayList<>();
        SpecSpansGraphVisitor visitor = new SpecSpansGraphVisitor() {
            @Override
            public void visitNode(SpecSpansGraphNode node, @Nullable SpecSpansGraphNode parentNode) {
                invocations.add(new MethodInvocation("visitNode", asList(node, parentNode)));
            }

            @Override
            public void postVisitNode(SpecSpansGraphNode node, @Nullable SpecSpansGraphNode parentNode) {
                invocations.add(new MethodInvocation("postVisitNode", asList(node, parentNode)));
            }
        };

        graph.visit(visitor);

        assertThat(invocations, equalTo(asList(
            new MethodInvocation("visitNode", asList(root, null)),
            new MethodInvocation("visitNode", asList(parent, root)),
            new MethodInvocation("visitNode", asList(child1, parent)),
            new MethodInvocation("postVisitNode", asList(child1, parent)),
            new MethodInvocation("visitNode", asList(child2, parent)),
            new MethodInvocation("postVisitNode", asList(child2, parent)),
            new MethodInvocation("postVisitNode", asList(parent, root)),
            new MethodInvocation("postVisitNode", asList(root, null))
        )));
    }

    @Nested
    class Json {

        @Test
        void empty() {
            match(
                "graph-empty.json",
                SpecSpansGraph.builder()
                    .build()
            );
        }

        @Test
        void roots_only() {
            match(
                "graph-roots-only.json",
                SpecSpansGraph.builder()
                    .addRoot(SpecSpansGraphNode.builder()
                        .spanId("1")
                        .name("name A")
                        .build()
                    )
                    .addRoot(SpecSpansGraphNode.builder()
                        .spanId("2")
                        .name("name B")
                        .build()
                    )
                    .build()
            );
        }

        @Test
        void with_children() {
            match(
                "graph-with-children.json",
                SpecSpansGraph.builder()
                    .addRoot(SpecSpansGraphNode.builder()
                        .spanId("1")
                        .name("root")
                        .addChild(SpecSpansGraphNode.builder()
                            .spanId("2")
                            .name("parent")
                            .addChild(SpecSpansGraphNode.builder()
                                .spanId("3")
                                .name("child")
                                .build()
                            )
                            .build()
                        )
                        .build()
                    )
                    .build()
            );
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
