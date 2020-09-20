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

package name.remal.tracingspec.renderer.jackson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.resource.Resources.readTextResource;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.junit.jupiter.api.Test;

class YamlTracingSpecRendererTest {

    final YamlTracingSpecRenderer renderer = new YamlTracingSpecRenderer();

    @Test
    void renderTracingSpec() {
        val parent = nextSpecSpanNode(it -> {
            it.setName("parent");
            it.setServiceName("service");
        });
        val child = nextSpecSpanNode(it -> {
            it.setParent(parent);
            it.setName("child");
            it.setServiceName("service");
        });
        val graph = new SpecSpansGraph()
            .addRoot(parent);

        val result = renderer.renderTracingSpec(graph);
        val normalizedResult = result.trim();

        val expectedResult = readTextResource("expected.yml");
        val normalizedExpectedResult = expectedResult.trim();

        assertThat(normalizedResult, equalTo(normalizedExpectedResult));
    }

}
