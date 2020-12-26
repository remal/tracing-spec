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
