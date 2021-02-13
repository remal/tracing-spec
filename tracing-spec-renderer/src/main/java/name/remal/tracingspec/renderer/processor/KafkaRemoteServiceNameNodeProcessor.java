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
