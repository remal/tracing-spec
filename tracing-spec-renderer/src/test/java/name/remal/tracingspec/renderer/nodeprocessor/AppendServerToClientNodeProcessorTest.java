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

package name.remal.tracingspec.renderer.nodeprocessor;

import static name.remal.tracingspec.model.SpecSpanKind.CLIENT;
import static name.remal.tracingspec.model.SpecSpanKind.SERVER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import lombok.val;
import org.junit.jupiter.api.Test;

class AppendServerToClientNodeProcessorTest {

    private final AppendServerToClientNodeProcessor processor = new AppendServerToClientNodeProcessor();

    @Test
    void positive_scenario() {
        val clientNode = nextSpecSpanNode(it -> {
            it.setKind(CLIENT);
            it.setServiceName("local");
        });

        val serverNode = nextSpecSpanNode(it -> {
            it.setParent(clientNode);
            it.setKind(SERVER);
            it.setServiceName("remote");
        });

        val child = nextSpecSpanNode(it -> {
            it.setParent(serverNode);
        });

        processor.processNode(clientNode);

        assertThat(clientNode.getKind(), equalTo(CLIENT));
        assertThat(clientNode.getServiceName(), equalTo("local"));
        assertThat(clientNode.getRemoteServiceName(), equalTo("remote"));
        assertThat(clientNode.getChildren(), hasItem(child));
    }

    @Test
    void getOrder() {
        assertThat(processor.getOrder(), equalTo(0));
    }

}
