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

import static name.remal.tracingspec.model.SpecSpanKind.CONSUMER;
import static name.remal.tracingspec.model.SpecSpanKind.PRODUCER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import lombok.val;
import org.junit.jupiter.api.Test;

class KafkaRemoteServiceNameNodeProcessorTest {

    private final KafkaRemoteServiceNameNodeProcessor processor = new KafkaRemoteServiceNameNodeProcessor();

    @Test
    void positive_scenario_for_producer() {
        val node = nextSpecSpanNode(it -> {
            it.setKind(PRODUCER);
            it.putTag("kafka.topic", "topic");
        });
        processor.processNode(node);
        assertThat(node.getRemoteServiceName(), equalTo("kafka"));
    }

    @Test
    void positive_scenario_for_consumer() {
        val node = nextSpecSpanNode(it -> {
            it.setKind(CONSUMER);
            it.putTag("kafka.topic", "topic");
        });
        processor.processNode(node);
        assertThat(node.getRemoteServiceName(), equalTo("kafka"));
    }

    @Test
    void getOrder() {
        assertThat(processor.getOrder(), equalTo(0));
    }

}
