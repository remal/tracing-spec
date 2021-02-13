package name.remal.tracingspec.renderer.processor;

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

}
