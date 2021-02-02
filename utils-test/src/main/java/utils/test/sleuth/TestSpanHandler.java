package utils.test.sleuth;

import static brave.handler.SpanHandler.Cause.FINISHED;
import static java.lang.String.format;

import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import java.util.ArrayList;
import java.util.List;

public class TestSpanHandler extends SpanHandler {

    private final List<MutableSpan> spans = new ArrayList<>();

    public synchronized MutableSpan get(int i) {
        if (i < 0) {
            throw new AssertionError("Span index can't be less than 0: " + i);
        } else if (i >= spans.size()) {
            throw new AssertionError(format(
                "Span can't be retrieved by the index %d, as only %d spans have been handled",
                i,
                spans.size()
            ));
        }
        return spans.get(i);
    }

    public synchronized List<MutableSpan> getSpans() {
        return spans;
    }

    public synchronized void clear() {
        spans.clear();
    }

    @Override
    public synchronized boolean end(TraceContext context, MutableSpan span, Cause cause) {
        if (cause == FINISHED) {
            spans.add(span);
        }
        return true;
    }

    @Override
    public synchronized String toString() {
        return TestSpanHandler.class.getSimpleName() + '[' + spans.size() + " spans]";
    }

}
