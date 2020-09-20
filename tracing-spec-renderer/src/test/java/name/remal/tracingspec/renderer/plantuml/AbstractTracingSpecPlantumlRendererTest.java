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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AbstractTracingSpecPlantumlRendererTest {

    private static final char[] CHARACTERS_TO_ESCAPE_WITH_CODEPOINT = new char[]{
        '"',
        '<',
        '*',
        '/',
        '-',
        '_',
        '~',
        '=',
        '#',
        '|',
        '&',
        };

    @Nested
    class EscapeString {

        @Test
        void empty() {
            assertThat(AbstractTracingSpecPlantumlRenderer.escapeString(""), equalTo(""));
        }

        @Test
        void clean() {
            assertThat(
                AbstractTracingSpecPlantumlRenderer.escapeString("abc"),
                equalTo("abc")
            );
        }

        @Test
        void should_be_escaped_by_slash() {
            assertThat(
                AbstractTracingSpecPlantumlRenderer.escapeString("1\n2\r3\t4"),
                equalTo("1\\n2\\r3\\t4")
            );
        }

        @Test
        void should_be_escaped_by_codepoint() {
            for (val ch : CHARACTERS_TO_ESCAPE_WITH_CODEPOINT) {
                assertThat(
                    "Character: '" + ch + '\'',
                    AbstractTracingSpecPlantumlRenderer.escapeString(ch + ""),
                    equalTo("&#" + ((int) ch) + ';')
                );
            }
        }

    }

    @Nested
    class QuoteString {

        @Test
        void empty() {
            assertThat(AbstractTracingSpecPlantumlRenderer.quoteString(""), equalTo("\" \""));
        }

        @Test
        void clean() {
            assertThat(
                AbstractTracingSpecPlantumlRenderer.quoteString("abc"),
                equalTo("abc")
            );
        }

        @Test
        void should_be_escaped_by_slash() {
            assertThat(
                AbstractTracingSpecPlantumlRenderer.quoteString("1\n2\r3\t4"),
                equalTo("\"1\\n2\\r3\\t4\"")
            );
        }

        @Test
        void should_be_escaped_by_codepoint() {
            for (val ch : CHARACTERS_TO_ESCAPE_WITH_CODEPOINT) {
                assertThat(
                    "Character: '" + ch + '\'',
                    AbstractTracingSpecPlantumlRenderer.quoteString(ch + ""),
                    equalTo("\"&#" + ((int) ch) + ";\"")
                );
            }
        }

        @Test
        void should_be_quoted_by_char() {
            for (val ch : new char[]{':', ' '}) {
                assertThat(
                    "Character: '" + ch + '\'',
                    AbstractTracingSpecPlantumlRenderer.quoteString(ch + ""),
                    equalTo("\"" + ch + "\"")
                );
            }
        }

    }

}
