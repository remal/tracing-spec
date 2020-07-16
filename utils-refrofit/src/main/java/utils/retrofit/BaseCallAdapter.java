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

import static utils.okhttp.OkhttpUtils.isPlainText;

import java.io.IOException;
import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import retrofit2.Call;
import retrofit2.CallAdapter;

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
            if (isPlainText(errorBody)) {
                val errorBodyString = errorBody.string();
                if (errorBodyString.isEmpty()) {
                    sb.append(":\n[empty error body]");
                } else {
                    sb.append(":\n").append(errorBodyString);
                }
            } else {
                sb.append(":\n").append("[binary ").append(errorBody.contentLength()).append("-byte body]");
            }
        }

        throw new RetrofitCallException(sb.toString());
    }

}
