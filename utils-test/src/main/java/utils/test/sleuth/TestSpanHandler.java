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

}
