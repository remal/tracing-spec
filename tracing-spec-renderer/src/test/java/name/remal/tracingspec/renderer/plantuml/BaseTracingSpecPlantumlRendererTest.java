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

package name.remal.tracingspec.renderer.plantuml;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BaseTracingSpecPlantumlRendererTest {

    private static final char[] CHARACTERS_TO_ESCAPE_WITH_CODEPOINT = new char[]{
        '"',
        '<',
        '*',
        '/',
        '-',
        '_',
        '~'
    };

    @Nested
    class EscapeString {

        @Test
        void empty() {
            assertThat(BaseTracingSpecPlantumlRenderer.escapeString(""), equalTo(""));
        }

        @Test
        void clean() {
            assertThat(
                BaseTracingSpecPlantumlRenderer.escapeString("abc"),
                equalTo("abc")
            );
        }

        @Test
        void should_be_escaped_by_slash() {
            assertThat(
                BaseTracingSpecPlantumlRenderer.escapeString("1\n2\r3\t4"),
                equalTo("1\\n2\\r3\\t4")
            );
        }

        @Test
        void should_be_escaped_by_codepoint() {
            for (val ch : CHARACTERS_TO_ESCAPE_WITH_CODEPOINT) {
                assertThat(
                    "Character: '" + ch + '\'',
                    BaseTracingSpecPlantumlRenderer.escapeString(ch + ""),
                    equalTo("<U+" + format("%04X", (int) ch) + '>')
                );
            }
        }

    }

    @Nested
    class QuoteString {

        @Test
        void empty() {
            assertThat(BaseTracingSpecPlantumlRenderer.quoteString(""), equalTo("\" \""));
        }

        @Test
        void clean() {
            assertThat(
                BaseTracingSpecPlantumlRenderer.quoteString("abc"),
                equalTo("abc")
            );
        }

        @Test
        void should_be_escaped_by_slash() {
            assertThat(
                BaseTracingSpecPlantumlRenderer.quoteString("1\n2\r3\t4"),
                equalTo("\"1\\n2\\r3\\t4\"")
            );
        }

        @Test
        void should_be_escaped_by_codepoint() {
            for (val ch : CHARACTERS_TO_ESCAPE_WITH_CODEPOINT) {
                assertThat(
                    "Character: '" + ch + '\'',
                    BaseTracingSpecPlantumlRenderer.quoteString(ch + ""),
                    equalTo("\"<U+" + format("%04X", (int) ch) + ">\"")
                );
            }
        }

    }

}
