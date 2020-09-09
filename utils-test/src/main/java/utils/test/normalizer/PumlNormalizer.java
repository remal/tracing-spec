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

package utils.test.normalizer;

import static java.util.stream.Collectors.joining;

import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.val;

public abstract class PumlNormalizer {

    private static final Pattern NEW_LINES = Pattern.compile("[\n\r]+");

    public static String normalizePuml(String diagram) {
        val lines = NEW_LINES.split(diagram);
        return Stream.of(lines)
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .collect(joining("\n"));
    }


    private PumlNormalizer() {
    }

}