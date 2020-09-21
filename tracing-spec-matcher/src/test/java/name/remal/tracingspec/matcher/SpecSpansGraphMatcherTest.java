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

package name.remal.tracingspec.matcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.junit.jupiter.api.Test;

class SpecSpansGraphMatcherTest {

    @Test
    void empty_pattern_graph() {
        val patternGraph = new SpecSpansGraph();
        assertThrows(IllegalArgumentException.class, () -> new SpecSpansGraphMatcher(patternGraph));
    }

    @Test
    void null_value() {
        val patternGraph = new SpecSpansGraph()
            .addRoot(nextSpecSpanNode());
        assertThat(
            new SpecSpansGraphMatcher(patternGraph).matches(null),
            equalTo(false)
        );
    }

    @Test
    void success() {
        val patternGraph = new SpecSpansGraph()
            .addRoot(nextSpecSpanNode(root -> {
                root.setName(null);
                root.addChild(nextSpecSpanNode(child -> {
                    child.setName("child");
                }));
            }));

        val graph = new SpecSpansGraph()
            .addRoot(nextSpecSpanNode(root -> {
                root.setName("parent");
                root.addChild(nextSpecSpanNode(child -> {
                    child.setName("child");
                }));
            }));

        assertThat(
            new SpecSpansGraphMatcher(patternGraph).matches(graph),
            equalTo(true)
        );
    }

    @Test
    void order_insensitive() {
        val patternGraph = new SpecSpansGraph()
            .addRoot(nextSpecSpanNode(root -> root.setName("1")))
            .addRoot(nextSpecSpanNode(root -> root.setName("2")));

        val sameOrderGraph = new SpecSpansGraph()
            .addRoot(nextSpecSpanNode(root -> root.setName("1")))
            .addRoot(nextSpecSpanNode(root -> root.setName("2")));
        assertThat(
            new SpecSpansGraphMatcher(patternGraph).matches(sameOrderGraph),
            equalTo(true)
        );

        val differentOrderGraph = new SpecSpansGraph()
            .addRoot(nextSpecSpanNode(root -> root.setName("2")))
            .addRoot(nextSpecSpanNode(root -> root.setName("1")));
        assertThat(
            new SpecSpansGraphMatcher(patternGraph).matches(differentOrderGraph),
            equalTo(true)
        );
    }

    @Test
    void different_roots_count() {
        val patternGraph = new SpecSpansGraph()
            .addRoot(nextSpecSpanNode())
            .addRoot(nextSpecSpanNode());

        val graph = new SpecSpansGraph()
            .addRoot(nextSpecSpanNode());

        assertThat(
            new SpecSpansGraphMatcher(patternGraph).matches(graph),
            equalTo(false)
        );
    }

}
