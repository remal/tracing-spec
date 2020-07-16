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

import static okhttp3.Protocol.HTTP_1_1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.val;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

class ErroneousResponseInterceptorTest {

    private final ErroneousResponseInterceptor interceptor = new ErroneousResponseInterceptor();

    private final Request request = new Request.Builder().url("http://localhost/").build();

    private final Chain chain = mock(Chain.class);

    {
        when(chain.request()).thenReturn(request);
    }

    @Test
    void exception_is_thrown() throws Throwable {
        when(chain.proceed(request)).thenThrow(AssertionError.class);
        assertThrows(ErroneousResponseException.class, () -> interceptor.intercept(chain));
    }

    @Test
    void successful_response() throws Throwable {
        val response = new Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(200)
            .message("successful")
            .build();
        when(chain.proceed(request)).thenReturn(response);
        assertThat(interceptor.intercept(chain), equalTo(response));
    }

    @Test
    void erroneous_response() throws Throwable {
        val response = new Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(404)
            .message("erroneous")
            .build();
        when(chain.proceed(request)).thenReturn(response);
        assertThrows(ErroneousResponseException.class, () -> interceptor.intercept(chain));
    }

}
