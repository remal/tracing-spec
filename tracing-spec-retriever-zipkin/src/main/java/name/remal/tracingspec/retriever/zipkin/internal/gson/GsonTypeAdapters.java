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

package name.remal.tracingspec.retriever.zipkin.internal.gson;

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
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.gradle_plugins.api.AutoService;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@SuppressWarnings({"unchecked", "rawtypes", "java:S2789"})
abstract class GsonTypeAdapters {

    @AutoService
    public static class OptionalTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        @Nullable
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
    }

    @RequiredArgsConstructor
    private static class OptionalTypeAdapter extends TypeAdapter<Optional<?>> {

        private final TypeAdapter valueAdapter;

        @Override
        public Optional<?> read(JsonReader in) throws IOException {
            val peek = in.peek();
            if (peek != JsonToken.NULL) {
                return Optional.ofNullable(valueAdapter.read(in));
            }
            return Optional.empty();
        }

        @Override
        public void write(JsonWriter out, @Nullable Optional<?> value) throws IOException {
            if (value != null && value.isPresent()) {
                valueAdapter.write(out, value.get());
            } else {
                out.nullValue();
            }
        }
    }


    @AutoService
    public static class OptionalDoubleTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        @Nullable
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            if (typeToken.getRawType() == OptionalDouble.class) {
                return (TypeAdapter) new OptionalDoubleTypeAdapter();
            }
            return null;
        }
    }

    private static class OptionalDoubleTypeAdapter extends TypeAdapter<OptionalDouble> {

        @Override
        public OptionalDouble read(JsonReader in) throws IOException {
            val peek = in.peek();
            if (peek != JsonToken.NULL) {
                return OptionalDouble.of(in.nextDouble());
            }
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


    @AutoService
    public static class OptionalIntTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        @Nullable
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            if (typeToken.getRawType() == OptionalInt.class) {
                return (TypeAdapter) new OptionalIntTypeAdapter();
            }
            return null;
        }
    }

    private static class OptionalIntTypeAdapter extends TypeAdapter<OptionalInt> {

        @Override
        public OptionalInt read(JsonReader in) throws IOException {
            val peek = in.peek();
            if (peek != JsonToken.NULL) {
                return OptionalInt.of(in.nextInt());
            }
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


    @AutoService
    public static class OptionalLongTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        @Nullable
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            if (typeToken.getRawType() == OptionalLong.class) {
                return (TypeAdapter) new OptionalLongTypeAdapter();
            }
            return null;
        }
    }

    private static class OptionalLongTypeAdapter extends TypeAdapter<OptionalLong> {

        @Override
        public OptionalLong read(JsonReader in) throws IOException {
            val peek = in.peek();
            if (peek != JsonToken.NULL) {
                return OptionalLong.of(in.nextLong());
            }
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


    private GsonTypeAdapters() {
    }

}
