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
import static java.util.Arrays.binarySearch;
import static java.util.Arrays.sort;

import java.util.Optional;
import lombok.val;
import name.remal.tracingspec.renderer.BaseTracingSpecRenderer;

public abstract class BaseTracingSpecPlantumlRenderer extends BaseTracingSpecRenderer<String> {

    private static final char[] CHARACTERS_TO_ESCAPE_WITH_CODEPOINT = new char[]{
        '"',
        '<',
        '*',
        '/',
        '-',
        '_',
        '~'
    };

    static {
        sort(CHARACTERS_TO_ESCAPE_WITH_CODEPOINT);
    }

    protected static String escapeString(String string) {
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
                sb.append("<U+").append(format("%04X", (int) ch)).append('>');

            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    protected static String escapeString(Optional<String> optionalString) {
        return escapeString(optionalString.orElse(""));
    }

    protected static String quoteString(String string) {
        if (string.isEmpty()) {
            return "\" \"";
        }

        val escapedString = escapeString(string);
        if (escapedString.equals(string)) {
            return string;
        } else {
            return '"' + escapedString + '"';
        }
    }

    protected static String quoteString(Optional<String> optionalString) {
        return quoteString(optionalString.orElse(""));
    }

}
