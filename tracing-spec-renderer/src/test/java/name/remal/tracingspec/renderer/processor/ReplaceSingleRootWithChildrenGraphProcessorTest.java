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

package name.remal.tracingspec.renderer.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.junit.jupiter.api.Test;

class ReplaceSingleRootWithChildrenGraphProcessorTest {

    private final ReplaceSingleRootWithChildrenGraphProcessor processor
        = new ReplaceSingleRootWithChildrenGraphProcessor();

    @Test
    void positive_scenario() throws Throwable {
        val root = nextSpecSpanNode();
        val parent1 = nextSpecSpanNode(it -> it.setParent(root));
        val child1 = nextSpecSpanNode(it -> it.setParent(parent1));
        val parent2 = nextSpecSpanNode(it -> it.setParent(root));
        val graph = new SpecSpansGraph()
            .addRoot(root);

        processor.processGraph(graph);

        val expectedGraph = new SpecSpansGraph()
            .addRoot(parent1)
            .addRoot(parent2);
        assertThat(graph, equalTo(expectedGraph));
    }

}
