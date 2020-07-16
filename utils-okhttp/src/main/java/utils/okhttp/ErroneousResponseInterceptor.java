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

package utils.okhttp;

import static java.lang.String.format;
import static utils.okhttp.OkhttpUtils.isPlainText;

import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Interceptor;
import okhttp3.Response;

public class ErroneousResponseInterceptor implements Interceptor {

    @Override
    @SneakyThrows
    public Response intercept(Chain chain) {
        val request = chain.request();

        final Response response;
        try {
            response = chain.proceed(request);
        } catch (Throwable exception) {
            throw new ErroneousResponseException(
                format("%s %s: %s", request.method(), request.url(), exception),
                exception
            );
        }

        if (response.code() >= 400) {
            throw createException(response);
        }

        return response;
    }

    @SneakyThrows
    private static Throwable createException(Response response) {
        val request = response.request();

        val sb = new StringBuilder();
        sb.append(request.method()).append(' ').append(request.url());
        sb.append(": HTTP status ").append(response.code());

        if (!response.message().isEmpty()) {
            sb.append(' ').append(response.message());
        }

        val responseBody = response.body();
        if (responseBody != null) {
            if (isPlainText(responseBody)) {
                val errorBodyString = responseBody.string();
                if (errorBodyString.isEmpty()) {
                    sb.append(":\n[empty body]");
                } else {
                    sb.append(":\n").append(errorBodyString);
                }
            } else {
                sb.append(":\n").append("[binary ").append(responseBody.contentLength()).append("-byte body]");
            }
        }

        return new ErroneousResponseException(sb.toString());
    }

}
