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

package name.remal.tracingspec.renderer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.test.reflection.ReflectionTestUtils.getParameterizedTypeArgumentClass;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpan;

import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"java:S5786", "java:S2699"})
public abstract class TracingSpecRendererTestBase<Result, Renderer extends TracingSpecRenderer<Result>> {

    protected final Renderer renderer;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    protected TracingSpecRendererTestBase() {
        Class<?> rendererClass = getParameterizedTypeArgumentClass(getClass(), TracingSpecRendererTestBase.class, 1);
        this.renderer = (Renderer) rendererClass.getConstructor().newInstance();
    }


    protected abstract Result normalizeResult(Result result);


    @Test
    final void empty_spans_list() {
        List<SpecSpan> specSpans = emptyList();
        assertThrows(IllegalArgumentException.class, () -> renderer.renderTracingSpec(specSpans));
    }


    @Test
    final void all_spans_without_service_name() {
        List<SpecSpan> specSpans = asList(
            nextSpecSpan(),
            nextSpecSpan()
        );
        assertDoesNotThrow(() -> renderer.renderTracingSpec(specSpans));
    }


    @Test
    final void all_spans_with_service_name() {
        List<SpecSpan> specSpans = asList(
            nextSpecSpan(span -> span.setServiceName("service A")),
            nextSpecSpan(span -> span.setServiceName("service B"))
        );
        assertDoesNotThrow(() -> renderer.renderTracingSpec(specSpans));
    }


    @Test
    final void some_spans_have_service_name_and_some_not() {
        List<SpecSpan> specSpans = asList(
            nextSpecSpan(span -> span.setServiceName("service A")),
            nextSpecSpan()
        );
        assertThrows(IllegalStateException.class, () -> renderer.renderTracingSpec(specSpans));
    }


    protected abstract void one_simple_span(Result result);

    @Test
    final void one_simple_span() {
        Result result = renderer.renderTracingSpec(singletonList(
            nextSpecSpan("name", span -> span.setServiceName("service"))
        ));
        one_simple_span(normalizeResult(result));
    }


    protected abstract void one_simple_span_with_description(Result result);

    @Test
    final void one_simple_span_with_description() {
        Result result = renderer.renderTracingSpec(singletonList(
            nextSpecSpan("name", span -> {
                span.setServiceName("service");
                span.setDescription("description");
            })
        ));
        one_simple_span_with_description(normalizeResult(result));
    }


    protected abstract void two_parents(Result result);

    @Test
    final void two_parents() {
        val parent1 = nextSpecSpan("parent1", span -> span.setServiceName("service"));
        val parent2 = nextSpecSpan("parent2", span -> span.setServiceName("service"));
        Result result = renderer.renderTracingSpec(asList(
            parent1,
            parent2
        ));
        two_parents(normalizeResult(result));
    }


    protected abstract void parent_child(Result result);

    @Test
    final void parent_child() {
        val parent = nextSpecSpan("parent", span -> span.setServiceName("service"));
        val child = nextSpecSpan("child", span -> {
            span.setParentSpanId(parent.getSpanId());
            span.setServiceName("service");
        });
        Result result = renderer.renderTracingSpec(asList(
            parent,
            child
        ));
        parent_child(normalizeResult(result));
    }


    protected abstract void parent_children(Result result);

    @Test
    final void parent_children() {
        val parent = nextSpecSpan("parent", span -> span.setServiceName("service"));
        val child1 = nextSpecSpan("child1", span -> {
            span.setParentSpanId(parent.getSpanId());
            span.setServiceName("service");
        });
        val child2 = nextSpecSpan("child2", span -> {
            span.setParentSpanId(parent.getSpanId());
            span.setServiceName("service");
        });
        Result result = renderer.renderTracingSpec(asList(
            parent,
            child1,
            child2
        ));
        parent_children(normalizeResult(result));
    }


    protected abstract void root_parent_child(Result result);

    @Test
    final void root_parent_child() {
        val root = nextSpecSpan("root", span -> span.setServiceName("root"));
        val parent = nextSpecSpan("parent", span -> {
            span.setParentSpanId(root.getSpanId());
            span.setServiceName("parent");
        });
        val child = nextSpecSpan("child", span -> {
            span.setParentSpanId(parent.getSpanId());
            span.setServiceName("child");
        });
        Result result = renderer.renderTracingSpec(asList(
            root,
            parent,
            child
        ));
        root_parent_child(normalizeResult(result));
    }


    protected abstract void async_one_span(Result result);

    @Test
    final void async_one_span() {
        val span = nextSpecSpan("name", it -> {
            it.setAsync(true);
            it.setServiceName("service");
        });
        Result result = renderer.renderTracingSpec(singletonList(
            span
        ));
        async_one_span(normalizeResult(result));
    }


    protected abstract void async_children(Result result);

    @Test
    final void async_children() {
        val parent = nextSpecSpan("inner", span -> span.setServiceName("service A"));
        val inner = nextSpecSpan("root", span -> {
            span.setParentSpanId(parent.getSpanId());
            span.setAsync(true);
            span.setServiceName("service A");
        });
        val child = nextSpecSpan("child", span -> {
            span.setParentSpanId(parent.getSpanId());
            span.setAsync(true);
            span.setServiceName("service B");
        });
        Result result = renderer.renderTracingSpec(asList(
            parent,
            inner,
            child
        ));
        async_children(normalizeResult(result));
    }

}
