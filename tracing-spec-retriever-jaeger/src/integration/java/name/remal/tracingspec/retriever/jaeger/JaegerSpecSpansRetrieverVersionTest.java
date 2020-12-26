package name.remal.tracingspec.retriever.jaeger;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.testcontainers.images.PullPolicy.ageBased;
import static utils.test.awaitility.AwaitilityUtils.await;

import io.jaegertracing.internal.JaegerTracer;
import io.jaegertracing.internal.reporters.RemoteReporter;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.thrift.internal.senders.HttpSender;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.test.container.JaegerAllInOneContainer;

@SuppressWarnings({"java:S1450", "java:S109"})
class JaegerSpecSpansRetrieverVersionTest {

    private static final String SERVICE_NAME = "service-name";

    private final JaegerAllInOneContainer jaegerContainer = new JaegerAllInOneContainer()
        .withImagePullPolicy(ageBased(Duration.ofHours(1)));

    private JaegerTracer tracer;

    private JaegerSpecSpansRetriever retriever;

    @BeforeEach
    void beforeEach() {
        jaegerContainer.start();

        val endpoint = format("http://localhost:%d/api/traces", jaegerContainer.getCollectorThriftPort());
        val sender = new HttpSender.Builder(endpoint)
            .build();
        val reporter = new RemoteReporter.Builder()
            .withSender(sender)
            .withFlushInterval(1)
            .build();
        tracer = new JaegerTracer.Builder(SERVICE_NAME)
            .withSampler(new ConstSampler(true))
            .withReporter(reporter)
            .build();

        val retrieverProperties = new JaegerSpecSpansRetrieverProperties();
        retrieverProperties.setHost("localhost");
        retrieverProperties.setPort(jaegerContainer.getQueryPort());
        retriever = new JaegerSpecSpansRetriever(retrieverProperties);
    }

    @AfterEach
    void afterEach() {
        tracer.close();
        jaegerContainer.stop();
    }


    @Test
    void test() {
        val rootSpan = tracer.buildSpan("root").start();
        val childSpan = tracer.buildSpan("child").asChildOf(rootSpan)
            .withTag("spec.description", "some text")
            .withTag("spec.async", true)
            .start();
        childSpan.finish();
        rootSpan.finish();


        val traceId = rootSpan.context().getTraceId();
        List<SpecSpan> specSpans = new ArrayList<>();
        await().until(
            () -> {
                specSpans.clear();
                specSpans.addAll(retriever.retrieveSpecSpansForTrace(traceId));
                return specSpans;
            },
            hasSize(2)
        );

        specSpans.forEach(specSpan -> {
            specSpan.getTags().clear();
            specSpan.getAnnotations().clear();
        });


        val expectedRootSpecSpan = new SpecSpan(rootSpan.context().toSpanId());
        expectedRootSpecSpan.setName("root");
        expectedRootSpecSpan.setServiceName(SERVICE_NAME);
        expectedRootSpecSpan.setStartedAt(Instant.ofEpochSecond(
            0,
            NANOSECONDS.convert(rootSpan.getStart(), MICROSECONDS)
        ));

        val expectedChildSpecSpan = new SpecSpan(childSpan.context().toSpanId());
        expectedChildSpecSpan.setParentSpanId(rootSpan.context().toSpanId());
        expectedChildSpecSpan.setName("child");
        expectedChildSpecSpan.setAsync(true);
        expectedChildSpecSpan.setServiceName(SERVICE_NAME);
        expectedChildSpecSpan.setStartedAt(Instant.ofEpochSecond(
            0,
            NANOSECONDS.convert(childSpan.getStart(), MICROSECONDS)
        ));
        expectedChildSpecSpan.setDescription("some text");

        assertThat(specSpans, containsInAnyOrder(
            expectedRootSpecSpan,
            expectedChildSpecSpan
        ));
    }

}
