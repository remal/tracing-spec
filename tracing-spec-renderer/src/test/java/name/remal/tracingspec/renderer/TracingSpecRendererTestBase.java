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

import static com.google.common.io.ByteStreams.toByteArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.ImmutableSpecSpan.SpecSpanBuilder;
import name.remal.tracingspec.model.SpecSpan;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"java:S5786", "java:S2699"})
public abstract class TracingSpecRendererTestBase<Result, Renderer extends TracingSpecRenderer<Result>> {

    protected final Renderer renderer;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    protected TracingSpecRendererTestBase() {
        val typeToken = TypeToken.of(this.getClass());
        val testType = typeToken.getSupertype(TracingSpecRendererTestBase.class).getType();
        if (testType instanceof ParameterizedType) {
            val parameterizedTestType = (ParameterizedType) testType;
            val rendererType = parameterizedTestType.getActualTypeArguments()[1];
            Class<?> rendererClass = TypeToken.of(rendererType).getRawType();
            val ctor = rendererClass.getConstructor();
            this.renderer = (Renderer) ctor.newInstance();
        } else {
            throw new IllegalStateException(this.getClass() + " isn't parameterized");
        }
    }


    protected abstract Result normalizeResult(Result result);


    @Test
    final void empty_spans_list() {
        List<SpecSpan> specSpans = emptyList();
        assertThrows(IllegalArgumentException.class, () -> renderer.renderTracingSpec(specSpans));
    }


    @Test
    final void all_spans_were_filtered_out() {
        renderer.setSpecSpansFilter(span -> false);
        List<SpecSpan> specSpans = singletonList(nextSpecSpanBuilder().build());
        assertThrows(IllegalStateException.class, () -> renderer.renderTracingSpec(specSpans));
    }


    @Test
    final void all_spans_without_service_name() {
        List<SpecSpan> specSpans = asList(
            nextSpecSpanBuilder().build(),
            nextSpecSpanBuilder().build()
        );
        assertDoesNotThrow(() -> renderer.renderTracingSpec(specSpans));
    }


    @Test
    final void all_spans_with_service_name() {
        List<SpecSpan> specSpans = asList(
            nextSpecSpanBuilder()
                .serviceName("service A")
                .build(),
            nextSpecSpanBuilder()
                .serviceName("service B")
                .build()
        );
        assertDoesNotThrow(() -> renderer.renderTracingSpec(specSpans));
    }


    @Test
    final void some_spans_have_service_name_and_some_not() {
        List<SpecSpan> specSpans = asList(
            nextSpecSpanBuilder()
                .serviceName("service A")
                .build(),
            nextSpecSpanBuilder()
                .build()
        );
        assertThrows(IllegalStateException.class, () -> renderer.renderTracingSpec(specSpans));
    }


    protected abstract void one_simple_span(Result result);

    @Test
    final void one_simple_span() {
        Result result = renderer.renderTracingSpec(singletonList(
            nextSpecSpanBuilder()
                .name("name")
                .serviceName("service")
                .build()
        ));
        one_simple_span(normalizeResult(result));
    }


    protected abstract void one_simple_span_with_description(Result result);

    @Test
    final void one_simple_span_with_description() {
        Result result = renderer.renderTracingSpec(singletonList(
            nextSpecSpanBuilder()
                .name("name")
                .serviceName("service")
                .description("description")
                .build()
        ));
        one_simple_span_with_description(normalizeResult(result));
    }

    @Test
    final void one_simple_span_with_service_name_transformation() {
        renderer.setServiceNameTransformer(String::trim);
        Result result = renderer.renderTracingSpec(singletonList(
            nextSpecSpanBuilder()
                .name("name")
                .serviceName("  service  ")
                .build()
        ));
        one_simple_span(normalizeResult(result));
    }


    protected abstract void two_parents(Result result);

    @Test
    final void two_parents() {
        val parent1 = nextSpecSpanBuilder()
            .name("parent1")
            .serviceName("service")
            .build();
        val parent2 = nextSpecSpanBuilder()
            .name("parent2")
            .serviceName("service")
            .build();
        Result result = renderer.renderTracingSpec(asList(
            parent1,
            parent2
        ));
        two_parents(normalizeResult(result));
    }


    protected abstract void parent_child(Result result);

    @Test
    final void parent_child() {
        val parent = nextSpecSpanBuilder()
            .name("parent")
            .serviceName("service")
            .build();
        val child = nextSpecSpanBuilder()
            .parentSpanId(parent.getSpanId())
            .name("child")
            .serviceName("service")
            .build();
        Result result = renderer.renderTracingSpec(asList(
            parent,
            child
        ));
        parent_child(normalizeResult(result));
    }


    protected abstract void parent_children(Result result);

    @Test
    final void parent_children() {
        val parent = nextSpecSpanBuilder()
            .name("parent")
            .serviceName("service")
            .build();
        val child1 = nextSpecSpanBuilder()
            .parentSpanId(parent.getSpanId())
            .name("child1")
            .serviceName("service")
            .build();
        val child2 = nextSpecSpanBuilder()
            .parentSpanId(parent.getSpanId())
            .name("child2")
            .serviceName("service")
            .build();
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
        val root = nextSpecSpanBuilder()
            .name("root")
            .serviceName("root")
            .build();
        val parent = nextSpecSpanBuilder()
            .parentSpanId(root.getSpanId())
            .name("parent")
            .serviceName("parent")
            .build();
        val child = nextSpecSpanBuilder()
            .parentSpanId(parent.getSpanId())
            .name("child")
            .serviceName("child")
            .build();
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
        val span = nextSpecSpanBuilder()
            .async(true)
            .name("name")
            .serviceName("service")
            .build();
        Result result = renderer.renderTracingSpec(singletonList(
            span
        ));
        async_one_span(normalizeResult(result));
    }


    protected abstract void async_children(Result result);

    @Test
    final void async_children() {
        val parent = nextSpecSpanBuilder()
            .name("root")
            .serviceName("service A")
            .build();
        val inner = nextSpecSpanBuilder()
            .async(true)
            .parentSpanId(parent.getSpanId())
            .name("inner")
            .serviceName("service A")
            .build();
        val child = nextSpecSpanBuilder()
            .async(true)
            .parentSpanId(parent.getSpanId())
            .name("child")
            .serviceName("service B")
            .build();
        Result result = renderer.renderTracingSpec(asList(
            parent,
            inner,
            child
        ));
        async_children(normalizeResult(result));
    }


    protected static SpecSpanBuilder nextSpecSpanBuilder() {
        return SpecSpan.builder().spanId(nextSpanId());
    }

    private static final AtomicLong SPAN_IDS = new AtomicLong(0);

    protected static String nextSpanId() {
        return SPAN_IDS.incrementAndGet() + "";
    }


    @SneakyThrows
    protected byte[] readBinaryResource(@Language("file-reference") String resourceName) {
        val loaderClass = this.getClass();
        val resourceUrl = loaderClass.getResource(resourceName);
        if (resourceUrl == null) {
            throw new IllegalArgumentException(loaderClass + ": resource can't be found: " + resourceName);
        }

        final byte[] bytesContent;
        try (val in = resourceUrl.openStream()) {
            bytesContent = toByteArray(in);
        }

        return bytesContent;
    }

    protected String readTextResource(@Language("file-reference") String resourceName) {
        val bytesContent = readBinaryResource(resourceName);
        return new String(bytesContent, UTF_8);
    }

}
