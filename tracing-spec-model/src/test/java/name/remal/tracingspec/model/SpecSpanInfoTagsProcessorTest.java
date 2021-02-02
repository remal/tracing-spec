package name.remal.tracingspec.model;

import static name.remal.tracingspec.model.SpecSpanKind.SERVER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class SpecSpanInfoTagsProcessorTest {

    private final SpecSpanInfo<?> info = mock(SpecSpanInfo.class);

    @Test
    void hidden_1() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.hidden", "1");
        verify(info).setHidden(true);
    }

    @Test
    void hidden_true() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.hidden", "tRuE");
        verify(info).setHidden(true);
    }

    @Test
    void kind() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.kind", "server");
        verify(info).setKind(SERVER);
    }

    @Test
    void async_1() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.async", "1");
        verify(info).setAsync(true);
    }

    @Test
    void async_true() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.async", "tRuE");
        verify(info).setAsync(true);
    }

    @Test
    void serviceName() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.serviceName", "service name");
        verify(info).setServiceName("service name");
    }

    @Test
    void remoteServiceName() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.remoteServiceName", "remote service name");
        verify(info).setRemoteServiceName("remote service name");
    }

    @Test
    void description() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.description", "description");
        verify(info).setDescription("description");
    }

}
