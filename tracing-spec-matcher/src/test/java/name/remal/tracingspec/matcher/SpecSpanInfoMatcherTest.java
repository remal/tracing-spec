package name.remal.tracingspec.matcher;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.COMMENTS;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.UNICODE_CASE;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;
import static java.util.regex.Pattern.UNIX_LINES;
import static name.remal.tracingspec.model.SpecSpanKind.CLIENT;
import static name.remal.tracingspec.model.SpecSpanKind.SERVER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpan;

import com.google.common.collect.ImmutableMap;
import java.util.function.BiConsumer;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SpecSpanInfoMatcherTest {

    @Test
    void null_value() {
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
            })).matches(null),
            equalTo(false)
        );
    }

    @Test
    void default_value() {
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
            })).matches(nextSpecSpan(it -> {
            })),
            equalTo(true)
        );
    }

    @Test
    void async() {
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
            })).matches(nextSpecSpan(it -> {
                it.setAsync(true);
            })),
            equalTo(false)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.setAsync(true);
            })).matches(nextSpecSpan(it -> {
            })),
            equalTo(false)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.setAsync(true);
            })).matches(nextSpecSpan(it -> {
                it.setAsync(true);
            })),
            equalTo(true)
        );
    }

    @Test
    void kind() {
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
            })).matches(nextSpecSpan(it -> {
                it.setKind(CLIENT);
            })),
            equalTo(true)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.setKind(CLIENT);
            })).matches(nextSpecSpan(it -> {
            })),
            equalTo(false)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.setKind(CLIENT);
            })).matches(nextSpecSpan(it -> {
                it.setKind(CLIENT);
            })),
            equalTo(true)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.setKind(CLIENT);
            })).matches(nextSpecSpan(it -> {
                it.setKind(SERVER);
            })),
            equalTo(false)
        );
    }

    @Test
    void name() {
        stringField(SpecSpanInfo::setName);
    }

    @Test
    void serviceName() {
        stringField(SpecSpanInfo::setServiceName);
    }

    @Test
    void remoteServiceName() {
        stringField(SpecSpanInfo::setRemoteServiceName);
    }

    @Test
    void description() {
        stringField(SpecSpanInfo::setDescription);
    }

    private static void stringField(BiConsumer<SpecSpanInfo<?>, String> setter) {
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
            })).matches(nextSpecSpan(it -> {
                setter.accept(it, "value");
            })),
            equalTo(true)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                setter.accept(it, "value");
            })).matches(nextSpecSpan(it -> {
            })),
            equalTo(false)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                setter.accept(it, "value");
            })).matches(nextSpecSpan(it -> {
                setter.accept(it, "value");
            })),
            equalTo(true)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                setter.accept(it, "value");
            })).matches(nextSpecSpan(it -> {
                setter.accept(it, "other");
            })),
            equalTo(false)
        );

        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                setter.accept(it, "/v.+e/");
            })).matches(nextSpecSpan(it -> {
            })),
            equalTo(false)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                setter.accept(it, "/v.+e/");
            })).matches(nextSpecSpan(it -> {
                setter.accept(it, "value");
            })),
            equalTo(true)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                setter.accept(it, "/v.+e/");
            })).matches(nextSpecSpan(it -> {
                setter.accept(it, "other");
            })),
            equalTo(false)
        );
    }

    @Test
    void tags() {
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
            })).matches(nextSpecSpan(it -> {
                it.putTag("key", "value");
            })),
            equalTo(true)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.putTag("key", "value");
            })).matches(nextSpecSpan(it -> {
            })),
            equalTo(false)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.putTag("key", "value");
            })).matches(nextSpecSpan(it -> {
                it.putTag("key", "value");
            })),
            equalTo(true)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.putTag("key", "value");
            })).matches(nextSpecSpan(it -> {
                it.putTag("key", "other");
            })),
            equalTo(false)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.putTag("key", "value");
            })).matches(nextSpecSpan(it -> {
                it.putTag("other", "value");
            })),
            equalTo(false)
        );

        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.putTag("/k.+y/", "/v.+e/");
            })).matches(nextSpecSpan(it -> {
            })),
            equalTo(false)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.putTag("/k.+y/", "/v.+e/");
            })).matches(nextSpecSpan(it -> {
                it.putTag("key", "value");
            })),
            equalTo(true)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.putTag("/k.+y/", "/v.+e/");
            })).matches(nextSpecSpan(it -> {
                it.putTag("key", "other");
            })),
            equalTo(false)
        );
        assertThat(
            new SpecSpanInfoMatcher(nextSpecSpan(it -> {
                it.putTag("/k.+y/", "/v.+e/");
            })).matches(nextSpecSpan(it -> {
                it.putTag("other", "value");
            })),
            equalTo(false)
        );
    }

    @Nested
    class ParsePattern {

        @Test
        void null_string() {
            assertThat(SpecSpanInfoMatcher.parsePattern(null), nullValue());
        }

        @Test
        void not_a_regex() {
            assertThat(SpecSpanInfoMatcher.parsePattern("/123"), nullValue());
            assertThat(SpecSpanInfoMatcher.parsePattern("123/"), nullValue());
        }

        @Test
        void empty_regex() {
            val pattern = SpecSpanInfoMatcher.parsePattern("//");
            assertThat(pattern, notNullValue());
            assertThat(pattern.pattern(), equalTo(""));
            assertThat(pattern.flags(), equalTo(UNICODE_CASE | UNICODE_CHARACTER_CLASS));
        }

        @Nested
        class Flags {

            @Test
            void flag_d() {
                val pattern = SpecSpanInfoMatcher.parsePattern("//d");
                assertThat(pattern, notNullValue());
                assertThat(pattern.flags(), equalTo(UNIX_LINES | UNICODE_CASE | UNICODE_CHARACTER_CLASS));
            }

            @Test
            void flag_i() {
                val pattern = SpecSpanInfoMatcher.parsePattern("//i");
                assertThat(pattern, notNullValue());
                assertThat(pattern.flags(), equalTo(CASE_INSENSITIVE | UNICODE_CASE | UNICODE_CHARACTER_CLASS));
            }

            @Test
            void flag_x() {
                val pattern = SpecSpanInfoMatcher.parsePattern("//x");
                assertThat(pattern, notNullValue());
                assertThat(pattern.flags(), equalTo(COMMENTS | UNICODE_CASE | UNICODE_CHARACTER_CLASS));
            }

            @Test
            void flag_m() {
                val pattern = SpecSpanInfoMatcher.parsePattern("//m");
                assertThat(pattern, notNullValue());
                assertThat(pattern.flags(), equalTo(MULTILINE | UNICODE_CASE | UNICODE_CHARACTER_CLASS));
            }

            @Test
            void flag_s() {
                val pattern = SpecSpanInfoMatcher.parsePattern("//s");
                assertThat(pattern, notNullValue());
                assertThat(pattern.flags(), equalTo(DOTALL | UNICODE_CASE | UNICODE_CHARACTER_CLASS));
            }

            @Test
            void multiple() {
                val pattern = SpecSpanInfoMatcher.parsePattern("//dixmsdixms");
                assertThat(pattern, notNullValue());
                assertThat(pattern.flags(), equalTo(
                    UNIX_LINES
                        | CASE_INSENSITIVE
                        | COMMENTS
                        | MULTILINE
                        | DOTALL
                        | UNICODE_CASE
                        | UNICODE_CHARACTER_CLASS
                ));
            }

        }

        @Test
        void simple_regex() {
            val pattern = SpecSpanInfoMatcher.parsePattern("/123/");
            assertThat(pattern, notNullValue());
            assertThat(pattern.pattern(), equalTo("123"));
        }

        @Test
        void regex_with_slashes() {
            val pattern = SpecSpanInfoMatcher.parsePattern("/1\\/2\\/3/");
            assertThat(pattern, notNullValue());
            assertThat(pattern.pattern(), equalTo("1/2/3"));
        }

        @Test
        void regex_with_slashes_and_backslashes_before() {
            val pattern = SpecSpanInfoMatcher.parsePattern("/1\\\\\\/2\\\\\\\\\\/3/");
            assertThat(pattern, notNullValue());
            assertThat(pattern.pattern(), equalTo("1\\\\/2\\\\\\\\/3"));
        }

    }

    @Nested
    class ToPattern {

        @Test
        void empty() {
            val pattern = SpecSpanInfoMatcher.toPattern("");
            assertThat(pattern, notNullValue());
            assertThat(pattern.pattern(), equalTo(""));
            assertThat(pattern.flags(), equalTo(UNICODE_CASE | UNICODE_CHARACTER_CLASS));
        }

        @Test
        void regex() {
            val pattern = SpecSpanInfoMatcher.toPattern("/abc/i");
            assertThat(pattern, notNullValue());
            assertThat(pattern.pattern(), equalTo("abc"));
            assertThat(pattern.flags(), equalTo(CASE_INSENSITIVE | UNICODE_CASE | UNICODE_CHARACTER_CLASS));
        }

        @Test
        void simple_string() {
            val pattern = SpecSpanInfoMatcher.toPattern("abc");
            assertThat(pattern, notNullValue());
            assertThat(pattern.pattern(), equalTo("abc"));
            assertThat(pattern.flags(), equalTo(UNICODE_CASE | UNICODE_CHARACTER_CLASS));
        }

        @Test
        void escape_special_chars() {
            char[] charsToEscape = new char[]{
                '\\',
                '.',
                '[',
                ']',
                '(',
                ')',
                '?',
                '*',
                '+',
                '{',
                '}',
                '^',
                '$',
                };
            for (val charToEscape : charsToEscape) {
                val assertDescr = "Char " + charToEscape;
                val pattern = SpecSpanInfoMatcher.toPattern("a" + charToEscape + "b");
                assertThat(assertDescr, pattern, notNullValue());
                assertThat(assertDescr, pattern.pattern(), equalTo("a\\" + charToEscape + "b"));
                assertThat(assertDescr, pattern.flags(), equalTo(UNICODE_CASE | UNICODE_CHARACTER_CLASS));
            }
        }

        @Test
        void escape_whitespace_chars() {
            val charsToEscape = ImmutableMap.of(
                '\n', "\\n",
                '\r', "\\r",
                '\t', "\\t"
            );
            for (val entry : charsToEscape.entrySet()) {
                val charToEscape = entry.getKey();
                val escapedChar = entry.getValue();

                val assertDescr = "Char " + ((int) charToEscape);
                val pattern = SpecSpanInfoMatcher.toPattern("a" + charToEscape + "b");
                assertThat(assertDescr, pattern, notNullValue());
                assertThat(assertDescr, pattern.pattern(), equalTo("a" + escapedChar + "b"));
                assertThat(assertDescr, pattern.flags(), equalTo(UNICODE_CASE | UNICODE_CHARACTER_CLASS));
            }
        }

    }

}
