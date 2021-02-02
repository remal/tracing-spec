package name.remal.tracingspec.renderer.processor;

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

}
