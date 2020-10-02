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

package name.remal.tracingspec.renderer.processor.script;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.junit.jupiter.api.Test;

class GroovyScriptEngineProcessorTest {

    @Test
    void graph() throws Throwable {
        val graph = new SpecSpansGraph().addRoot(nextSpecSpanNode());
        val processor = new GroovyScriptEngineProcessor("graph.removeRoot(graph.getRoots()[0])");
        processor.processGraph(graph);
        assertThat(graph.getRoots(), empty());
    }

    @Test
    void node() throws Throwable {
        val node = nextSpecSpanNode(it -> {
            it.setServiceName("empty");
        });
        val processor = new GroovyScriptEngineProcessor("node.serviceName = 'service'");
        processor.processNode(node);
        assertThat(node.getServiceName(), equalTo("service"));
    }

}
