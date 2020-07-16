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

import static utils.okhttp.OkhttpUtils.isPlainText;

import java.io.IOException;
import lombok.val;
import okhttp3.Interceptor;
import okhttp3.Response;

public class RequiredTextResponseBodyInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        val request = chain.request();
        val response = chain.proceed(request);

        val responseBody = response.body();
        val isBinaryResponse = responseBody != null && responseBody.contentLength() > 0 && !isPlainText(responseBody);
        if (responseBody == null || responseBody.contentLength() == 0 || isBinaryResponse) {
            val sb = new StringBuilder();
            sb.append(request.method()).append(' ').append(request.url()).append(": ");

            if (responseBody == null || responseBody.contentLength() == 0) {
                sb.append("No response body");
            } else {
                sb.append("Binary response body");
            }

            throw new ErroneousResponseException(sb.toString());
        }

        return response;
    }

}
