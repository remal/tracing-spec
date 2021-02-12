package utils.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.OptionalDouble;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.gradle_plugins.api.AutoService;

@AutoService
public class OptionalDoubleTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (typeToken.getRawType() == OptionalDouble.class) {
            return (TypeAdapter) new OptionalDoubleTypeAdapter();
        }
        return null;
    }

    private static class OptionalDoubleTypeAdapter extends TypeAdapter<OptionalDouble> {

        @Override
        public OptionalDouble read(JsonReader in) throws IOException {
            val peek = in.peek();
            if (peek != JsonToken.NULL) {
                return OptionalDouble.of(in.nextDouble());
            }

            in.nextNull();
            return OptionalDouble.empty();
        }

        @Override
        public void write(JsonWriter out, @Nullable OptionalDouble value) throws IOException {
            if (value != null && value.isPresent()) {
                out.value(value.getAsDouble());
            } else {
                out.nullValue();
            }
        }

    }

}
