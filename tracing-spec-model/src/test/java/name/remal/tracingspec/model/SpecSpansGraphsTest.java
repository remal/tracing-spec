package name.remal.tracingspec.model;

import static java.time.Instant.ofEpochSecond;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static name.remal.tracingspec.model.SpecSpanConverter.SPEC_SPAN_CONVERTER;
import static name.remal.tracingspec.model.SpecSpanKind.CLIENT;
import static name.remal.tracingspec.model.SpecSpanKind.CONSUMER;
import static name.remal.tracingspec.model.SpecSpanKind.PRODUCER;
import static name.remal.tracingspec.model.SpecSpanKind.SERVER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.tracing.SpanIdGenerator.nextSpanId;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpan;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SpecSpansGraphsTest {

    @Nested
    @DisplayName("createSpecSpansGraph: Iterable<SpecSpan>")
    class CreateSpecSpansGraphIterableSpecSpans {

        @Test
        void empty() {
            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(emptyList()),
                equalTo(new SpecSpansGraph())
            );
        }

        @Test
        void one() {
            val span1 = nextSpecSpan("name");

            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(singletonList(span1)),
                equalTo(new SpecSpansGraph()
                    .addRoot(SPEC_SPAN_CONVERTER.toNode(span1))
                )
            );
        }

        @Test
        void hierarchy() {
            val root = nextSpecSpan("root");
            val parent = nextSpecSpan("parent", span -> span.setParentSpanId(root.getSpanId()));
            val child1 = nextSpecSpan("child1", span -> span.setParentSpanId(parent.getSpanId()));
            val child2 = nextSpecSpan("child2", span -> span.setParentSpanId(parent.getSpanId()));

            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(asList(
                    root,
                    parent,
                    child1,
                    child2
                )),
                equalTo(new SpecSpansGraph()
                    .addRoot(SPEC_SPAN_CONVERTER.toNode(root)
                        .addChild(SPEC_SPAN_CONVERTER.toNode(parent)
                            .addChild(SPEC_SPAN_CONVERTER.toNode(child1))
                            .addChild(SPEC_SPAN_CONVERTER.toNode(child2))
                        )
                    )
                )
            );
        }

        @Test
        void invalid_parentSpanId() {
            val root = nextSpecSpan("root");
            val child = nextSpecSpan("child", span -> span.setParentSpanId(root.getSpanId() + " invalid"));

            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(asList(
                    root,
                    child
                )),
                equalTo(new SpecSpansGraph()
                    .addRoot(SPEC_SPAN_CONVERTER.toNode(root))
                )
            );
        }

        @Test
        void shared_spans_client_server() {
            val root = nextSpecSpan("root");

            val sharedSpanId = nextSpanId();
            val client = new SpecSpan(sharedSpanId);
            client.setParentSpanId(root.getSpanId());
            client.setKind(CLIENT);
            val server = new SpecSpan(sharedSpanId);
            server.setParentSpanId(root.getSpanId());
            server.setKind(SERVER);

            val child = nextSpecSpan("child", span -> span.setParentSpanId(sharedSpanId));

            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(asList(
                    root,
                    child,
                    server,
                    client
                )),
                equalTo(new SpecSpansGraph()
                    .addRoot(SPEC_SPAN_CONVERTER.toNode(root)
                        .addChild(SPEC_SPAN_CONVERTER.toNode(client)
                            .addChild(SPEC_SPAN_CONVERTER.toNode(server)
                                .addChild(SPEC_SPAN_CONVERTER.toNode(child))
                            )
                        )
                    )
                )
            );
        }

        @Test
        void shared_spans_producer_consumer() {
            val root = nextSpecSpan("root");

            val sharedSpanId = nextSpanId();
            val producer = new SpecSpan(sharedSpanId);
            producer.setParentSpanId(root.getSpanId());
            producer.setKind(PRODUCER);
            val consumer = new SpecSpan(sharedSpanId);
            consumer.setParentSpanId(root.getSpanId());
            consumer.setKind(CONSUMER);

            val child = nextSpecSpan("child", span -> span.setParentSpanId(sharedSpanId));

            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(asList(
                    root,
                    child,
                    consumer,
                    producer
                )),
                equalTo(new SpecSpansGraph()
                    .addRoot(SPEC_SPAN_CONVERTER.toNode(root)
                        .addChild(SPEC_SPAN_CONVERTER.toNode(producer)
                            .addChild(SPEC_SPAN_CONVERTER.toNode(consumer)
                                .addChild(SPEC_SPAN_CONVERTER.toNode(child))
                            )
                        )
                    )
                )
            );
        }

        @Test
        void shared_spans_sorted_by_startedAt() {
            val root = nextSpecSpan("root");

            val sharedSpanId = nextSpanId();
            val shared1 = new SpecSpan(sharedSpanId);
            shared1.setParentSpanId(root.getSpanId());
            shared1.setStartedAt(ofEpochSecond(1));
            val shared2 = new SpecSpan(sharedSpanId);
            shared2.setParentSpanId(root.getSpanId());
            shared2.setStartedAt(ofEpochSecond(2));

            val child = nextSpecSpan("child", span -> span.setParentSpanId(sharedSpanId));

            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(asList(
                    root,
                    child,
                    shared2,
                    shared1
                )),
                equalTo(new SpecSpansGraph()
                    .addRoot(SPEC_SPAN_CONVERTER.toNode(root)
                        .addChild(SPEC_SPAN_CONVERTER.toNode(shared1)
                            .addChild(SPEC_SPAN_CONVERTER.toNode(shared2)
                                .addChild(SPEC_SPAN_CONVERTER.toNode(child))
                            )
                        )
                    )
                )
            );
        }

        @Test
        void shared_span_with_the_same_serviceName() {
            val root = nextSpecSpan("root");

            val sharedSpanId = nextSpanId();
            val shared1 = new SpecSpan(sharedSpanId);
            shared1.setParentSpanId(root.getSpanId());
            shared1.setStartedAt(ofEpochSecond(1));
            shared1.setServiceName("1");
            val shared2 = new SpecSpan(sharedSpanId);
            shared2.setParentSpanId(root.getSpanId());
            shared2.setStartedAt(ofEpochSecond(2));
            shared2.setServiceName("2");

            val child = nextSpecSpan("child", span -> {
                span.setParentSpanId(sharedSpanId);
                span.setServiceName("1");
            });

            assertThat(
                SpecSpansGraphs.createSpecSpansGraph(asList(
                    root,
                    child,
                    shared2,
                    shared1
                )),
                equalTo(new SpecSpansGraph()
                    .addRoot(SPEC_SPAN_CONVERTER.toNode(root)
                        .addChild(SPEC_SPAN_CONVERTER.toNode(shared1)
                            .addChild(SPEC_SPAN_CONVERTER.toNode(shared2))
                            .addChild(SPEC_SPAN_CONVERTER.toNode(child))
                        )
                    )
                )
            );
        }

    }

}
