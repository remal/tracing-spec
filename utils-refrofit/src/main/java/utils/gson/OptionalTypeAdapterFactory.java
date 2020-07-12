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
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
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
