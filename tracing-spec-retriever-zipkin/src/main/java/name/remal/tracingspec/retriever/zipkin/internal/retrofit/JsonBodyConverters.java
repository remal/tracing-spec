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

package name.remal.tracingspec.retriever.zipkin.internal.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ServiceLoader;
import javax.annotation.Nullable;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.ApiStatus.Internal;
import retrofit2.Converter;
import retrofit2.Retrofit;

@Internal
public class JsonBodyConverters extends Converter.Factory {

    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");

    private static final Gson GSON;

    static {
        val builder = new GsonBuilder()
            .disableHtmlEscaping();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(builder::registerTypeAdapterFactory);
        GSON = builder.create();
    }

    @Nullable
    @Override
    public Converter<?, RequestBody> requestBodyConverter(
        Type type,
        Annotation[] parameterAnnotations,
        Annotation[] methodAnnotations,
        Retrofit retrofit
    ) {
        return value -> {
            val json = GSON.toJson(value);
            return RequestBody.create(MEDIA_TYPE, json);
        };
    }

    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return responseBody -> {
            val json = responseBody.string();
            return GSON.fromJson(json, type);
        };
    }

}
