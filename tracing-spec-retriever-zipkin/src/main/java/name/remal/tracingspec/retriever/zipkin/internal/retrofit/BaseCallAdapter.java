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

import static name.remal.tracingspec.retriever.zipkin.internal.okhttp.OkhttpUtils.isPlaintext;

import java.io.IOException;
import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.api.ExcludeFromCodeCoverage;
import org.jetbrains.annotations.ApiStatus.Internal;
import retrofit2.Call;
import retrofit2.CallAdapter;

@Internal
@ExcludeFromCodeCoverage
@RequiredArgsConstructor
abstract class BaseCallAdapter implements CallAdapter<Object, Object> {

    private final Type responseType;

    @Override
    public Type responseType() {
        return responseType;
    }


    protected static <T> retrofit2.Response<T> executeCall(Call<T> call) {
        try {
            return call.execute();
        } catch (IOException e) {
            throw new RetrofitCallException(e);
        }
    }

    @SneakyThrows
    protected static RetrofitCallException createStatusException(Call<?> call, retrofit2.Response<?> response) {
        val sb = new StringBuilder();
        sb.append(call.request().method()).append(' ').append(call.request().url());
        sb.append(": HTTP status ").append(response.code());

        if (!response.message().isEmpty()) {
            sb.append(' ').append(response.message());
        }

        val errorBody = response.errorBody();
        if (errorBody != null) {
            if (isPlaintext(errorBody)) {
                sb.append(":\n").append(errorBody.string());
            } else {
                sb.append(":\n").append("[binary ").append(errorBody.contentLength()).append("-byte body]");
            }
        }

        throw new RetrofitCallException(sb.toString());
    }

}
