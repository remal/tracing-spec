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

package utils.retrofit;

import static utils.gson.GsonFactory.getGsonInstance;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
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

    @Nullable
    @Override
    public Converter<Object, RequestBody> requestBodyConverter(
        Type type,
        Annotation[] parameterAnnotations,
        Annotation[] methodAnnotations,
        Retrofit retrofit
    ) {
        return value -> {
            val json = getGsonInstance().toJson(value);
            return RequestBody.create(MEDIA_TYPE, json);
        };
    }

    @Nullable
    @Override
    public Converter<ResponseBody, Object> responseBodyConverter(
        Type type,
        Annotation[] annotations,
        Retrofit retrofit
    ) {
        return responseBody -> {
            val json = responseBody.string();
            return getGsonInstance().fromJson(json, type);
        };
    }

}
