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

import java.io.IOException;
import lombok.val;
import okhttp3.Interceptor;
import okhttp3.Response;

abstract class AbstractHeaderInterceptor implements Interceptor {

    protected abstract Object getValue();


    private final String header;

    protected AbstractHeaderInterceptor(String header) {
        this.header = header;
    }

    @Override
    public final Response intercept(Chain chain) throws IOException {
        val request = chain.request();
        if (request.header(header) != null) {
            return chain.proceed(request);
        }

        return chain.proceed(request.newBuilder()
            .header(header, getValue().toString())
            .build()
        );
    }

}
