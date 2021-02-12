package utils.test.tracing;

import static utils.test.tracing.SpanIdGenerator.nextSpanId;

import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpanNode;

public interface SpecSpanGenerator {

    static SpecSpan nextSpecSpan() {
        return new SpecSpan(nextSpanId());
    }

    @SneakyThrows
    static SpecSpan nextSpecSpan(Configurer<SpecSpan> configurer) {
        val result = nextSpecSpan();
        configurer.configure(result);
        return result;
    }

    static SpecSpan nextSpecSpan(String name) {
        return nextSpecSpan(span -> span.setName(name));
    }

    static SpecSpan nextSpecSpan(String name, Configurer<SpecSpan> configurer) {
        return nextSpecSpan(configurer.combineWith(span -> span.setName(name)));
    }


    static SpecSpanNode nextSpecSpanNode() {
        val result = new SpecSpanNode();
        result.setName(nextSpanId());
        return result;
    }

    @SneakyThrows
    static SpecSpanNode nextSpecSpanNode(Configurer<SpecSpanNode> configurer) {
        val result = nextSpecSpanNode();
        configurer.configure(result);
        return result;
    }

    static SpecSpanNode nextSpecSpanNode(String name) {
        return nextSpecSpanNode(node -> node.setName(name));
    }

    static SpecSpanNode nextSpecSpanNode(String name, Configurer<SpecSpanNode> configurer) {
        return nextSpecSpanNode(configurer.combineWith(node -> node.setName(name)));
    }


    @FunctionalInterface
    interface Configurer<T> {

        void configure(T object) throws Throwable;

        default Configurer<T> combineWith(Configurer<T> configurer) {
            return object -> {
                configure(object);
                configurer.configure(object);
            };
        }

    }

}
