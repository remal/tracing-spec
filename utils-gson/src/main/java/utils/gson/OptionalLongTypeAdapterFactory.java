package utils.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.OptionalLong;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.gradle_plugins.api.AutoService;

@AutoService
public class OptionalLongTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (typeToken.getRawType() == OptionalLong.class) {
            return (TypeAdapter) new OptionalLongTypeAdapter();
        }
        return null;
    }

    private static class OptionalLongTypeAdapter extends TypeAdapter<OptionalLong> {

        @Override
        public OptionalLong read(JsonReader in) throws IOException {
            val peek = in.peek();
            if (peek != JsonToken.NULL) {
                return OptionalLong.of(in.nextLong());
            }

            in.nextNull();
            return OptionalLong.empty();
        }

        @Override
        public void write(JsonWriter out, @Nullable OptionalLong value) throws IOException {
            if (value != null && value.isPresent()) {
                out.value(value.getAsLong());
            } else {
                out.nullValue();
            }
        }

    }

}
