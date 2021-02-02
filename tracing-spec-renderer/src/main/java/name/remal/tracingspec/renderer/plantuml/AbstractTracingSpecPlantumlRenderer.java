package name.remal.tracingspec.renderer.plantuml;

import static java.lang.Character.isWhitespace;
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.sort;

import javax.annotation.Nullable;
import lombok.val;
import name.remal.tracingspec.renderer.AbstractStringTracingSpecRenderer;

public abstract class AbstractTracingSpecPlantumlRenderer extends AbstractStringTracingSpecRenderer {

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

    static {
        sort(CHARACTERS_TO_ESCAPE_WITH_CODEPOINT);
    }

    protected static String escapeString(@Nullable String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }

        val sb = new StringBuilder();
        for (int pos = 0; pos < string.length(); ++pos) {
            val ch = string.charAt(pos);
            if (ch == '\n') {
                sb.append("\\n");
            } else if (ch == '\r') {
                sb.append("\\r");
            } else if (ch == '\t') {
                sb.append("\\t");

            } else if (binarySearch(CHARACTERS_TO_ESCAPE_WITH_CODEPOINT, ch) >= 0) {
                sb.append("&#").append((int) ch).append(';');

            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }


    protected static String quoteString(@Nullable String string) {
        if (string == null || string.isEmpty()) {
            return "\" \"";
        }

        val escapedString = escapeString(string);
        if (escapedString.equals(string) && !shouldBeQuoted(escapedString)) {
            return string;
        } else {
            return '"' + escapedString + '"';
        }
    }

    private static boolean shouldBeQuoted(String string) {
        for (int pos = 0; pos < string.length(); ++pos) {
            val ch = string.charAt(pos);
            if (ch == ':'
                || isWhitespace(ch)
            ) {
                return true;
            }
        }
        return false;
    }

}
