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

package name.remal.tracingspec.application;

import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_JAVA_COMMENTS;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_MISSING_VALUES;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_TRAILING_COMMA;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.val;
import org.intellij.lang.annotations.Language;

abstract class SerializationUtils {

    private static final YAMLMapper YAML_MAPPER = YAMLMapper.builder(
        YAMLFactory.builder()
            .build()
    )
        .disable(WRITE_DOC_START_MARKER)
        .enable(MINIMIZE_QUOTES)
        .findAndAddModules()
        .build();

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder(
        JsonFactory.builder()
            .enable(ALLOW_JAVA_COMMENTS)
            .enable(ALLOW_SINGLE_QUOTES)
            .enable(ALLOW_UNQUOTED_FIELD_NAMES)
            .enable(ALLOW_MISSING_VALUES)
            .enable(ALLOW_TRAILING_COMMA)
            .build()
    )
        .findAndAddModules()
        .build();

    @SneakyThrows
    public static <T> T readYamlOrJson(Class<T> type, Path path) {
        val url = path.toUri().toURL();
        try {
            return YAML_MAPPER.readValue(url, type);

        } catch (JsonParseException yamlException) {
            try {
                return JSON_MAPPER.readValue(url, type);

            } catch (Throwable jsonException) {
                jsonException.addSuppressed(yamlException);
                throw jsonException;
            }
        }
    }

    @Language("YAML")
    @SneakyThrows
    public static String writeYamlToString(Object object) {
        return YAML_MAPPER.writeValueAsString(object);
    }

    @Language("JSON")
    @SneakyThrows
    public static String writeJsonToString(Object object) {
        return JSON_MAPPER.writeValueAsString(object);
    }


    private SerializationUtils() {
    }

}
