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

package name.remal.tracingspec.matcher;

import static java.util.Collections.unmodifiableMap;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.COMMENTS;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.MULTILINE;
import static java.util.regex.Pattern.UNICODE_CASE;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;
import static java.util.regex.Pattern.UNIX_LINES;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanInfo;
import name.remal.tracingspec.model.SpecSpanKind;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.VisibleForTesting;

@Experimental
@ToString
@EqualsAndHashCode
public class SpecSpanInfoMatcher {

    private final boolean async;

    @Nullable
    private final SpecSpanKind kind;

    @Nullable
    private final String name;
    @Nullable
    private final Pattern namePattern;

    @Nullable
    private final String serviceName;
    @Nullable
    private final Pattern serviceNamePattern;

    @Nullable
    private final String remoteServiceName;
    @Nullable
    private final Pattern remoteServiceNamePattern;

    @Nullable
    private final String description;
    @Nullable
    private final Pattern descriptionPattern;

    private final Map<Pattern, Pattern> tagPatterns = new HashMap<>();

    public SpecSpanInfoMatcher(SpecSpanInfo<?> patternInfo) {
        async = patternInfo.isAsync();

        kind = patternInfo.getKind();

        namePattern = parsePattern(patternInfo.getName());
        name = namePattern == null ? patternInfo.getName() : null;

        serviceNamePattern = parsePattern(patternInfo.getServiceName());
        serviceName = serviceNamePattern == null ? patternInfo.getServiceName() : null;

        remoteServiceNamePattern = parsePattern(patternInfo.getRemoteServiceName());
        remoteServiceName = remoteServiceNamePattern == null ? patternInfo.getRemoteServiceName() : null;

        descriptionPattern = parsePattern(patternInfo.getDescription());
        description = descriptionPattern == null ? patternInfo.getDescription() : null;

        patternInfo.getTags().forEach((key, value) -> {
            val keyPattern = toPattern(key);
            val valuePattern = toPattern(value);
            tagPatterns.put(keyPattern, valuePattern);
        });
    }

    @Contract("null -> false")
    @SuppressWarnings("java:S3776")
    public boolean matches(@Nullable SpecSpanInfo<?> info) {
        if (info == null) {
            return false;
        }
        if (async != info.isAsync()) {
            return false;
        }
        if (kind != null && kind != info.getKind()) {
            return false;
        }
        if (name != null && !name.equals(info.getName())) {
            return false;
        }
        if (namePattern != null && notMatches(info.getName(), namePattern)) {
            return false;
        }
        if (serviceName != null && !serviceName.equals(info.getServiceName())) {
            return false;
        }
        if (serviceNamePattern != null && notMatches(info.getServiceName(), serviceNamePattern)) {
            return false;
        }
        if (remoteServiceName != null && !remoteServiceName.equals(info.getRemoteServiceName())) {
            return false;
        }
        if (remoteServiceNamePattern != null && notMatches(info.getRemoteServiceName(), remoteServiceNamePattern)) {
            return false;
        }
        if (description != null && !description.equals(info.getDescription())) {
            return false;
        }
        if (descriptionPattern != null && notMatches(info.getDescription(), descriptionPattern)) {
            return false;
        }
        if (!tagPatterns.isEmpty()) {
            for (val entry : tagPatterns.entrySet()) {
                val keyPattern = entry.getKey();
                val valuePattern = entry.getValue();
                val hasNotMatchingEntry = info.getTags().entrySet().stream().noneMatch(infoEntry -> {
                    val infoKey = infoEntry.getKey();
                    val infoValue = infoEntry.getValue();
                    if (infoKey == null || infoValue == null) {
                        return false;
                    }
                    return keyPattern.matcher(infoKey).matches()
                        && valuePattern.matcher(infoValue).matches();
                });
                if (hasNotMatchingEntry) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean notMatches(
        @Nullable String value,
        Pattern expectedPattern
    ) {
        return value == null || !expectedPattern.matcher(value).matches();
    }


    private static final Pattern IS_REGEX_PATTERN = Pattern.compile("/(.*)/([a-zA-Z]*)");

    private static final int DEFAULT_FLAGS = UNICODE_CASE | UNICODE_CHARACTER_CLASS;

    private static final Map<Character, Integer> PATTERN_FLAG_CHARS;

    static {
        Map<Character, Integer> patternFlagChars = new HashMap<>();
        patternFlagChars.put('d', UNIX_LINES);
        patternFlagChars.put('i', CASE_INSENSITIVE);
        patternFlagChars.put('x', COMMENTS);
        patternFlagChars.put('m', MULTILINE);
        patternFlagChars.put('s', DOTALL);
        PATTERN_FLAG_CHARS = unmodifiableMap(patternFlagChars);
    }

    private static final Pattern SLASH_UNESCAPER = Pattern.compile("((?:^|[^/])(?:\\\\\\\\)*)\\\\/");

    @Nullable
    @Contract("null -> null")
    @VisibleForTesting
    static Pattern parsePattern(@Nullable String string) {
        if (string == null || string.length() <= 1) {
            return null;
        }

        val matcher = IS_REGEX_PATTERN.matcher(string);
        if (!matcher.matches()) {
            return null;
        }

        String pattern = matcher.group(1);
        pattern = SLASH_UNESCAPER.matcher(pattern).replaceAll("$1/");

        int flags = DEFAULT_FLAGS;
        val flagsString = matcher.group(2);
        if (!flagsString.isEmpty()) {
            for (val entry : PATTERN_FLAG_CHARS.entrySet()) {
                if (flagsString.indexOf(entry.getKey()) >= 0) {
                    flags |= entry.getValue();
                }
            }
        }

        return Pattern.compile(pattern, flags);
    }

    @VisibleForTesting
    static Pattern toPattern(String string) {
        Pattern pattern = parsePattern(string);
        if (pattern != null) {
            return pattern;
        }

        val patternString = string
            .replace("\\", "\\\\")
            .replace(".", "\\.")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("?", "\\?")
            .replace("*", "\\*")
            .replace("+", "\\+")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace("^", "\\^")
            .replace("$", "\\$")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
        return Pattern.compile(patternString, DEFAULT_FLAGS);
    }

}
