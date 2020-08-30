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
import static name.remal.tracingspec.model.SpecSpanConverter.SPEC_SPAN_CONVERTER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpan;

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
                equalTo(new SpecSpansGraph())
            );
        }

        @Test
        void one() {
            val span1 = nextSpecSpan("name");

            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(singletonList(span1)),
                equalTo(new SpecSpansGraph()
                    .addRoot(SPEC_SPAN_CONVERTER.toNode(span1))
                )
            );
        }

        @Test
        void hierarchy() {
            val root = nextSpecSpan("root");
            val parent = nextSpecSpan("parent", span -> span.setParentSpanId(root.getSpanId()));
            val child1 = nextSpecSpan("child1", span -> span.setParentSpanId(parent.getSpanId()));
            val child2 = nextSpecSpan("child2", span -> span.setParentSpanId(parent.getSpanId()));

            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(asList(
                    root,
                    parent,
                    child1,
                    child2
                )),
                equalTo(new SpecSpansGraph()
                    .addRoot(SPEC_SPAN_CONVERTER.toNode(root)
                        .addChild(SPEC_SPAN_CONVERTER.toNode(parent)
                            .addChild(SPEC_SPAN_CONVERTER.toNode(child1))
                            .addChild(SPEC_SPAN_CONVERTER.toNode(child2))
                        )
                    )
                )
            );
        }

    }

}
