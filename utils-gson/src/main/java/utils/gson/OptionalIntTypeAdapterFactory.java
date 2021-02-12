package utils.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.gradle_plugins.api.AutoService;

@AutoService
public class OptionalIntTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (typeToken.getRawType() == OptionalInt.class) {
            return (TypeAdapter) new OptionalIntTypeAdapter();
        }
        return null;
    }

    private static class OptionalIntTypeAdapter extends TypeAdapter<OptionalInt> {

        @Override
        public OptionalInt read(JsonReader in) throws IOException {
            val peek = in.peek();
            if (peek != JsonToken.NULL) {
                return OptionalInt.of(in.nextInt());
            }

            in.nextNull();
            return OptionalInt.empty();
        }

        @Override
        public void write(JsonWriter out, @Nullable OptionalInt value) throws IOException {
            if (value != null && value.isPresent()) {
                out.value(value.getAsInt());
            } else {
                out.nullValue();
            }
        }

    }

}
