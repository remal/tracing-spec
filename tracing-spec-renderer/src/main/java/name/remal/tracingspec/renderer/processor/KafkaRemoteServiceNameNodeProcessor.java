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

import static name.remal.tracingspec.model.SpecSpanKind.CONSUMER;
import static name.remal.tracingspec.model.SpecSpanKind.PRODUCER;

import lombok.val;
import name.remal.tracingspec.model.SpecSpanNode;
import name.remal.tracingspec.renderer.SpecSpanNodeProcessor;

public class KafkaRemoteServiceNameNodeProcessor implements SpecSpanNodeProcessor {

    @Override
    public void processNode(SpecSpanNode node) {
        if (node.getKind() != PRODUCER && node.getKind() != CONSUMER) {
            return;
        }
        if (node.getRemoteServiceName() != null) {
            return;
        }

        val kafkaTopic = node.getTag("kafka.topic");
        if (kafkaTopic != null && !kafkaTopic.isEmpty()) {
            node.setRemoteServiceName("kafka");
        }
    }

}