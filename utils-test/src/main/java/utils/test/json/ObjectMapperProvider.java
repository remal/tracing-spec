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

package utils.test.json;

import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_JAVA_COMMENTS;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_MISSING_VALUES;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_TRAILING_COMMA;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES;
import static java.util.Objects.requireNonNull;
import static utils.test.resource.Resources.getResourceUrl;
import static utils.test.whocalled.WhoCalled.getCallerClass;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import org.intellij.lang.annotations.Language;

public abstract class ObjectMapperProvider {

    private static final JsonFactory JSON_FACTORY = new JsonFactoryBuilder()
        .enable(ALLOW_JAVA_COMMENTS)
        .enable(ALLOW_SINGLE_QUOTES)
        .enable(ALLOW_UNQUOTED_FIELD_NAMES)
        .enable(ALLOW_MISSING_VALUES)
        .enable(ALLOW_TRAILING_COMMA)
        .build();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(JSON_FACTORY)
        .findAndRegisterModules();

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }


    @SneakyThrows
    public static String writeJsonString(@Nullable Object object) {
        return getObjectMapper().writeValueAsString(object);
    }


    @SneakyThrows
    public static <T> T readJsonString(@Language("JSON") String json, Class<T> type) {
        val result = getObjectMapper().readValue(json, type);
        return requireNonNull(result, "result");
    }

    @SneakyThrows
    public static <T> T readJsonString(@Language("JSON") String json, TypeReference<T> typeReference) {
        val result = getObjectMapper().readValue(json, typeReference);
        return requireNonNull(result, "result");
    }


    @SneakyThrows
    public static <T> T readJsonResource(
        @Language("file-reference") String resourceName,
        Class<T> type
    ) {
        val loaderClass = getCallerClass(1);
        return readJsonResource(loaderClass, resourceName, type);
    }

    @SneakyThrows
    public static <T> T readJsonResource(
        Class<?> loaderClass,
        @Language("file-reference") String resourceName,
        Class<T> type
    ) {
        val resourceUrl = getResourceUrl(loaderClass, resourceName);
        val result = getObjectMapper().readValue(resourceUrl, type);
        return requireNonNull(result, "result");
    }

    @SneakyThrows
    public static <T> T readJsonResource(
        @Language("file-reference") String resourceName,
        TypeReference<T> typeReference
    ) {
        val loaderClass = getCallerClass(1);
        return readJsonResource(loaderClass, resourceName, typeReference);
    }

    @SneakyThrows
    public static <T> T readJsonResource(
        Class<?> loaderClass,
        @Language("file-reference") String resourceName,
        TypeReference<T> typeReference
    ) {
        val resourceUrl = getResourceUrl(loaderClass, resourceName);
        val result = getObjectMapper().readValue(resourceUrl, typeReference);
        return requireNonNull(result, "result");
    }


    private ObjectMapperProvider() {
    }

}
