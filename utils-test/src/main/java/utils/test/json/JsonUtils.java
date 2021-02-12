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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import org.intellij.lang.annotations.Language;

public abstract class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder(
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


    private JsonUtils() {
    }

}
