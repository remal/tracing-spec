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
import static org.hamcrest.Matchers.nullValue;
import static utils.test.reflection.ReflectionTestUtils.getParameterizedTypeArgumentClass;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpan;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

abstract class SpecSpanInfoTest<T extends SpecSpanInfo<T>> {

    private final T instance;

    @SneakyThrows
    @SuppressWarnings("unchecked")
    protected SpecSpanInfoTest() {
        val type = getParameterizedTypeArgumentClass(getClass(), SpecSpanInfoTest.class, 0);
        if (type == SpecSpan.class) {
            instance = (T) nextSpecSpan();
        } else {
            instance = (T) type.getConstructor().newInstance();
        }
    }


    @Test
    void isAsync() {
        assertThat(
            instance.isAsync(),
            equalTo(false)
        );

        instance.setAsync(true);
        assertThat(
            instance.isAsync(),
            equalTo(true)
        );

        instance.setAsync(false);
        assertThat(
            instance.isAsync(),
            equalTo(false)
        );

        for (val kind : SpecSpanKind.values()) {
            instance.setKind(kind);
            assertThat(
                "Kind " + kind,
                instance.isAsync(),
                equalTo(kind.isAlwaysAsync())
            );
        }
    }

    @Test
    void isSync() {
        assertThat(
            instance.isSync(),
            equalTo(true)
        );

        instance.setAsync(true);
        assertThat(
            instance.isSync(),
            equalTo(false)
        );

        instance.setAsync(false);
        assertThat(
            instance.isSync(),
            equalTo(true)
        );

        for (val kind : SpecSpanKind.values()) {
            instance.setKind(kind);
            assertThat(
                "Kind " + kind,
                instance.isSync(),
                equalTo(!kind.isAlwaysAsync())
            );
        }
    }

    @Test
    void getTag() {
        instance.putTag("key", "value");
        assertThat(instance.getTag("key"), equalTo("value"));

        instance.putTag("key", null);
        assertThat(instance.getTag("key"), nullValue());
    }

    @Test
    void putTag() {
        instance.putTag("key", "value");
        assertThat(instance.getTags(), equalTo(singletonMap("key", "value")));

        instance.putTag("key", null);
        assertThat(instance.getTags(), equalTo(emptyMap()));

        instance.putTag("spec.kind", "server");
    }

    @Test
    void addAnnotation() {
        instance.addAnnotation("value");
        assertThat(instance.getAnnotations(), hasItem(new SpecSpanAnnotation("value")));

        instance.addAnnotation("key", "value");
        assertThat(instance.getAnnotations(), hasItem(new SpecSpanAnnotation("key", "value")));
    }

}
