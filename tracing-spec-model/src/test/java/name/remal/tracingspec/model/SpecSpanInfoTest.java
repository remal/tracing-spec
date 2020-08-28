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

package name.remal.tracingspec.model;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static utils.test.reflection.ReflectionTestUtils.getParameterizedTypeArgumentClass;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpan;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

abstract class SpecSpanInfoTest<T extends SpecSpanInfo<T>> {

    private final T span;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    protected SpecSpanInfoTest() {
        val type = getParameterizedTypeArgumentClass(getClass(), SpecSpanInfoTest.class, 0);
        if (type == SpecSpan.class) {
            span = (T) nextSpecSpan();
        } else {
            span = (T) type.getConstructor().newInstance();
        }
    }


    @Test
    void isSync() {
        assertThat(
            span.isSync(),
            equalTo(true)
        );

        span.setAsync(true);
        assertThat(
            span.isSync(),
            equalTo(false)
        );

        span.setAsync(false);
        assertThat(
            span.isSync(),
            equalTo(true)
        );
    }

    @Test
    void putTag() {
        span.putTag("key", "value");
        assertThat(span.getTags(), equalTo(singletonMap("key", "value")));

        span.putTag("key", null);
        assertThat(span.getTags(), equalTo(emptyMap()));
    }

    @Test
    void removeTag() {
        span.getTags().put("key", "value");

        span.removeTag("key");
        assertThat(span.getTags(), equalTo(emptyMap()));
    }

    @Test
    void addAnnotation() {
        span.addAnnotation("value");
        assertThat(span.getAnnotations(), hasItem(new SpecSpanAnnotation("value")));

        span.addAnnotation("key", "value");
        assertThat(span.getAnnotations(), hasItem(new SpecSpanAnnotation("key", "value")));
    }

}
