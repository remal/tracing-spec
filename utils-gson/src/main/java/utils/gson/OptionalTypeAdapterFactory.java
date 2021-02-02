package utils.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.api.AutoService;

@AutoService
public class OptionalTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (typeToken.getRawType() != Optional.class) {
            return null;
        }
        final Type valueType;
        val type = typeToken.getType();
        if (type instanceof ParameterizedType) {
            val parameterizedType = (ParameterizedType) type;
            valueType = parameterizedType.getActualTypeArguments()[0];
        } else {
            valueType = Object.class;
        }
        val valueAdapter = gson.getAdapter(TypeToken.get(valueType));
        return (TypeAdapter) new OptionalTypeAdapter(valueAdapter);
    }

    @RequiredArgsConstructor
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static class OptionalTypeAdapter extends TypeAdapter<Optional<?>> {

        private final TypeAdapter valueAdapter;

        @Override
        public Optional<?> read(JsonReader in) throws IOException {
            val peek = in.peek();
            if (peek != JsonToken.NULL) {
                return Optional.ofNullable(valueAdapter.read(in));
            }

            in.skipValue();
            return Optional.empty();
        }

        @Override
        @SuppressWarnings("java:S2789")
        public void write(JsonWriter out, @Nullable Optional<?> value) throws IOException {
            if (value != null && value.isPresent()) {
                valueAdapter.write(out, value.get());
            } else {
                out.nullValue();
            }
        }
    }

}
