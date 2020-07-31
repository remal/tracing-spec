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
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanBuilder;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SpecSpansGraphsTest {

    @Nested
    @DisplayName("createSpecSpansGraph: Iterable<SpecSpan>")
    class CreateSpecSpansGraphIterableSpecSpans {

        @Test
        void empty() {
            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(emptyList()),
                equalTo(SpecSpansGraph.builder()
                    .build()
                )
            );
        }

        @Test
        void one() {
            val span1 = nextSpecSpanBuilder()
                .name("name")
                .build();

            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(singletonList(span1)),
                equalTo(SpecSpansGraph.builder()
                    .addRoot(SpecSpansGraphNode.builder()
                        .spanId(span1.getSpanId())
                        .name(span1.getName())
                        .build()
                    )
                    .build()
                )
            );
        }

        @Test
        void hierarchy() {
            val root = nextSpecSpanBuilder()
                .name("root")
                .build();
            val parent = nextSpecSpanBuilder()
                .parentSpanId(root.getSpanId())
                .name("parent")
                .build();
            val child1 = nextSpecSpanBuilder()
                .parentSpanId(parent.getSpanId())
                .name("child A")
                .build();
            val child2 = nextSpecSpanBuilder()
                .parentSpanId(parent.getSpanId())
                .name("child B")
                .build();

            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(asList(
                    root,
                    parent,
                    child1,
                    child2
                )),
                equalTo(SpecSpansGraph.builder()
                    .addRoot(SpecSpansGraphNode.builder()
                        .spanId(root.getSpanId())
                        .name(root.getName())
                        .addChild(SpecSpansGraphNode.builder()
                            .spanId(parent.getSpanId())
                            .name(parent.getName())
                            .addChild(SpecSpansGraphNode.builder()
                                .spanId(child1.getSpanId())
                                .name(child1.getName())
                                .build()
                            )
                            .addChild(SpecSpansGraphNode.builder()
                                .spanId(child2.getSpanId())
                                .name(child2.getName())
                                .build()
                            )
                            .build()
                        )
                        .build()
                    )
                    .build()
                )
            );
        }

    }

}
