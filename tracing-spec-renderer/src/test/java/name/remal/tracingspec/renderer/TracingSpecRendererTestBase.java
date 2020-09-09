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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.test.json.ObjectMapperProvider.readJsonResource;
import static utils.test.reflection.ReflectionTestUtils.getParameterizedTypeArgumentClass;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpan;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"java:S5786", "java:S2699"})
public abstract class TracingSpecRendererTestBase<Result, Renderer extends TracingSpecRenderer<Result>> {

    protected final Renderer renderer;

    @SneakyThrows
    protected TracingSpecRendererTestBase() {
        Class<Renderer> rendererClass = getParameterizedTypeArgumentClass(
            getClass(),
            TracingSpecRendererTestBase.class,
            1
        );
        this.renderer = rendererClass.getConstructor().newInstance();

        this.renderer.addTagToDisplay("displayableTag");
    }


    protected abstract Result normalizeResult(Result result);

    protected abstract String getExpectedResourceName(@Language("file-reference") String resourceName);

    protected abstract Result loadExpectedResult(@Language("file-reference") String expectedResourceName)
        throws Throwable;

    @Test
    final void one_sync() throws Throwable {
        resourceTest("one-sync.json5");
    }

    @Test
    final void one_sync_local_remote() throws Throwable {
        resourceTest("one-sync-local-remote.json5");
    }

    @Test
    final void one_sync_same_local_remote() throws Throwable {
        resourceTest("one-sync-same-local-remote.json5");
    }

    @Test
    final void one_async() throws Throwable {
        resourceTest("one-async.json5");
    }

    @Test
    final void one_async_local_remote() throws Throwable {
        resourceTest("one-async-local-remote.json5");
    }

    @Test
    final void one_async_same_local_remote() throws Throwable {
        resourceTest("one-async-same-local-remote.json5");
    }

    @Test
    final void parent_self_child() throws Throwable {
        resourceTest("parent-self-child.json5");
    }

    @Test
    final void parent_child_sync_sync() throws Throwable {
        resourceTest("parent-child-sync-sync.json5");
    }

    @Test
    final void parent_child_sync_async() throws Throwable {
        resourceTest("parent-child-sync-async.json5");
    }

    @Test
    final void parent_child_async_sync() throws Throwable {
        resourceTest("parent-child-async-sync.json5");
    }

    @Test
    final void parent_child_async_async() throws Throwable {
        resourceTest("parent-child-async-async.json5");
    }

    @Test
    final void children_async_sync_async() throws Throwable {
        resourceTest("children-async-sync-async.json5");
    }

    @Test
    final void parent_without_remote_children_with_remote() throws Throwable {
        resourceTest("parent-without-remote-children-with-remote.json5");
    }

    @Test
    final void parent_with_own_remote_children_with_remote() throws Throwable {
        resourceTest("parent-with-own-remote-children-with-remote.json5");
    }

    @Test
    final void parent_with_own_remote_child_without_remote() throws Throwable {
        resourceTest("parent-with-own-remote-child-without-remote.json5");
    }

    @Test
    final void consumer_root_with_remote() throws Throwable {
        resourceTest("consumer-root-with-remote.json5");
    }

    @Test
    final void consumer_child_with_remote() throws Throwable {
        resourceTest("consumer-child-with-remote.json5");
    }

    @Test
    final void consumer_parent_with_remote() throws Throwable {
        resourceTest("consumer-parent-with-remote.json5");
    }

    @Test
    final void description() throws Throwable {
        resourceTest("description.json5");
    }

    @Test
    final void displayable_tag() throws Throwable {
        resourceTest("displayable-tag.json5");
    }

    @Test
    final void hidden_spans() throws Throwable {
        resourceTest("hidden-spans.json5");
    }

    private void resourceTest(@Language("file-reference") String resourceName) throws Throwable {
        val resourceLoaderClass = TracingSpecRendererTestBase.class;

        final String resourceNamePrefix;
        {
            val name = resourceLoaderClass.getName();
            resourceNamePrefix = name.substring(0, name.lastIndexOf('.') + 1).replace('.', '/');
        }

        if (!resourceName.contains("/")) {
            resourceName = resourceNamePrefix + resourceName;
        }

        val specSpans = readJsonResource(
            resourceLoaderClass,
            resourceName,
            new TypeReference<List<SpecSpan>>() { }
        );
        val result = renderer.renderTracingSpec(specSpans);
        val normalizedResult = normalizeResult(result);

        val expectedResourceName = getExpectedResourceName(resourceName);
        val expectedResult = loadExpectedResult(expectedResourceName);
        val normalizedExpectedResult = normalizeResult(expectedResult);

        assertThat(normalizedResult, equalTo(normalizedExpectedResult));
    }


    @Nested
    class Preconditions {

        @Test
        final void empty_spans_list() {
            List<SpecSpan> specSpans = emptyList();
            assertThrows(IllegalArgumentException.class, () -> renderer.renderTracingSpec(specSpans));
        }

        @Test
        final void span_without_service_name() {
            List<SpecSpan> specSpans = singletonList(nextSpecSpan());
            assertThrows(IllegalStateException.class, () -> renderer.renderTracingSpec(specSpans));
        }

    }

}
