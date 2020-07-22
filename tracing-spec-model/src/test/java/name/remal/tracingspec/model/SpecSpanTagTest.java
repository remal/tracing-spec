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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import java.util.List;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;

class SpecSpanTagTest {

    @Test
    void tag_names_are_correct() {
        for (val specTag : SpecSpanTag.values()) {
            assertThat(specTag.name(), specTag.getTagName(), equalTo(
                "spec." + specTag.name().toLowerCase().replace('_', '-')
            ));
        }
    }


    @Test
    void description() {
        val builder = SpecSpan.builder().spanId("0");
        SpecSpanTag.DESCRIPTION.processTagsIntoBuilder(
            singletonMap(SpecSpanTag.DESCRIPTION.getTagName(), "some text"),
            builder
        );

        assertThat(
            builder.build(),
            hasProperty("description", equalTo(Optional.of("some text")))
        );
    }

    @Test
    void description_null() {
        val builder = SpecSpan.builder().spanId("0");
        SpecSpanTag.DESCRIPTION.processTagsIntoBuilder(
            singletonMap(SpecSpanTag.DESCRIPTION.getTagName(), null),
            builder
        );

        assertThat(
            builder.build(),
            hasProperty("description", equalTo(Optional.empty()))
        );
    }

    @Test
    void description_empty() {
        val builder = SpecSpan.builder().spanId("0");
        SpecSpanTag.DESCRIPTION.processTagsIntoBuilder(
            singletonMap(SpecSpanTag.DESCRIPTION.getTagName(), ""),
            builder
        );

        assertThat(
            builder.build(),
            hasProperty("description", equalTo(Optional.empty()))
        );
    }


    @Test
    void is_async() {
        for (val trueString : generateTrueStrings()) {
            val builder = SpecSpan.builder().spanId("0");
            SpecSpanTag.IS_ASYNC.processTagsIntoBuilder(
                singletonMap(SpecSpanTag.IS_ASYNC.getTagName(), trueString),
                builder
            );

            assertThat(
                "True string: \"" + trueString + "\"",
                builder.build(),
                hasProperty("async", equalTo(true))
            );
        }
    }

    @Test
    void is_async_null() {
        val builder = SpecSpan.builder().spanId("0");
        SpecSpanTag.IS_ASYNC.processTagsIntoBuilder(
            singletonMap(SpecSpanTag.IS_ASYNC.getTagName(), null),
            builder
        );

        assertThat(
            builder.build(),
            hasProperty("async", equalTo(false))
        );
    }

    @Test
    void is_async_empty() {
        val builder = SpecSpan.builder().spanId("0");
        SpecSpanTag.IS_ASYNC.processTagsIntoBuilder(
            singletonMap(SpecSpanTag.IS_ASYNC.getTagName(), ""),
            builder
        );

        assertThat(
            builder.build(),
            hasProperty("async", equalTo(false))
        );
    }


    private static List<String> generateTrueStrings() {
        return asList(
            "1",
            "true",
            "TRUE"
        );
    }

}
