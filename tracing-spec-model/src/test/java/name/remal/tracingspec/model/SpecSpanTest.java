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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SpecSpanTest {

    @Test
    void can_be_built_with_spanId_only() {
        val builder = SpecSpan.builder()
            .spanId("0");
        assertDoesNotThrow(builder::build);
    }

    @Test
    void async_is_false_by_default() {
        val specSpan = SpecSpan.builder().spanId("0")
            .build();
        assertThat(specSpan.isAsync(), equalTo(false));
    }

    @Nested
    class Validation {

        @Test
        void spanId_cannot_be_empty() {
            val builder = SpecSpan.builder()
                .spanId("");
            assertThrows(IllegalStateException.class, builder::build);
        }

        @Test
        void parentSpanId_cannot_be_empty() {
            val builder = SpecSpan.builder().spanId("0")
                .parentSpanId("");
            assertThrows(IllegalStateException.class, builder::build);
        }

        @Test
        void name_cannot_be_empty() {
            val builder = SpecSpan.builder().spanId("0")
                .name("");
            assertThrows(IllegalStateException.class, builder::build);
        }

        @Test
        void serviceName_cannot_be_empty() {
            val builder = SpecSpan.builder().spanId("0")
                .serviceName("");
            assertThrows(IllegalStateException.class, builder::build);
        }

        @Test
        void description_cannot_be_empty() {
            val builder = SpecSpan.builder().spanId("0")
                .description("");
            assertThrows(IllegalStateException.class, builder::build);
        }

    }

}
