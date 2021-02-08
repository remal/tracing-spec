package utils.gson;

import static java.lang.String.format;
import static name.remal.gradle_plugins.api.BuildTimeConstants.getClassName;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.api.AutoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@AutoService
public class CaseInsensitiveEnumTypeAdapterFactory implements TypeAdapterFactory {

    private static final Logger logger = LogManager.getLogger(CaseInsensitiveEnumTypeAdapterFactory.class);

    @Override
    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<?> rawType = typeToken.getRawType();
        if (Enum.class.isAssignableFrom(rawType) && rawType != Enum.class) {
            if (!rawType.isEnum()) {
                rawType = rawType.getSuperclass();
            }
            return (TypeAdapter) new CaseInsensitiveEnumTypeAdapter((Class) rawType);
        }
        return null;
    }

    @RequiredArgsConstructor
    private static class CaseInsensitiveEnumTypeAdapter extends TypeAdapter<Enum<?>> {

        private final Map<String, Enum<?>> nameToConstant = new LinkedHashMap<>();
        private final Map<String, Enum<?>> alternateNameToConstant = new LinkedHashMap<>();

        private final Map<Enum<?>, String> constantToName = new LinkedHashMap<>();

        @SneakyThrows
        @SuppressWarnings("java:S134")
        public CaseInsensitiveEnumTypeAdapter(Class<? extends Enum<?>> enumClass) {
            val constants = enumClass.getEnumConstants();
            for (val constant : constants) {
                val constantName = constant.name();
                val serializedName = getSerializedName(constant);
                if (serializedName != null) {
                    val name = getSerializedName(serializedName);
                    {
                        val prevConstant = nameToConstant.put(name, constant);
                        if (prevConstant != null) {
                            throw new AssertionError(format(
                                "Name '%s' bound to several enum constants: %s and %s",
                                name,
                                prevConstant,
                                constant
                            ));
                        }
                    }
                    for (val alternateName : getSerializedAlternateNames(serializedName)) {
                        val prevConstant = alternateNameToConstant.put(alternateName, constant);
                        if (prevConstant != null) {
                            throw new AssertionError(format(
                                "Alternate name '%s' bound to several enum constants: %s and %s",
                                name,
                                prevConstant,
                                constant
                            ));
                        }
                    }
                    constantToName.put(constant, name);

                } else {
                    nameToConstant.put(constantName, constant);
                    constantToName.put(constant, constantName);
                }
            }
        }


        private static final String SERIALIZED_NAME_CLASS_NAME = getClassName(SerializedName.class);

        @Nullable
        @SneakyThrows
        private static Annotation getSerializedName(Enum<?> constant) {
            val field = constant.getDeclaringClass().getField(constant.name());
            for (val annotation : field.getAnnotations()) {
                val annotationClass = annotation.annotationType();
                val annotationClassName = annotationClass.getName();
                if (annotationClassName.equalsIgnoreCase(SERIALIZED_NAME_CLASS_NAME)
                    || annotationClassName.endsWith('.' + SERIALIZED_NAME_CLASS_NAME)
                ) {
                    return annotation;
                }
            }
            return null;
        }

        @SneakyThrows
        private static String getSerializedName(Annotation annotation) {
            val annotationClass = annotation.annotationType();
            val method = annotationClass.getMethod("value");
            return (String) method.invoke(annotation);
        }

        private static String[] getSerializedAlternateNames(Annotation annotation) {
            try {
                val annotationClass = annotation.annotationType();
                val method = annotationClass.getMethod("alternate");
                return (String[]) method.invoke(annotation);

            } catch (Throwable e) {
                logger.warn(e.toString(), e);
                return new String[0];
            }
        }


        @Override
        @Nullable
        public Enum<?> read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            val string = in.nextString();

            Enum<?> value;
            value = nameToConstant.get(string);
            if (value != null) {
                return value;
            }

            value = alternateNameToConstant.get(string);
            if (value != null) {
                return value;
            }

            for (Entry<String, Enum<?>> entry : nameToConstant.entrySet()) {
                if (string.equalsIgnoreCase(entry.getKey())) {
                    return entry.getValue();
                }
            }

            for (Entry<String, Enum<?>> entry : alternateNameToConstant.entrySet()) {
                if (string.equalsIgnoreCase(entry.getKey())) {
                    return entry.getValue();
                }
            }

            return null;
        }

        @Override
        public void write(JsonWriter out, @Nullable Enum<?> value) throws IOException {
            if (value != null) {
                val name = constantToName.get(value);
                if (name == null) {
                    throw new AssertionError("Unknown enum constant: " + value);
                }
                out.value(name);

            } else {
                out.nullValue();
            }
        }

    }

}
