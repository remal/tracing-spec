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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import lombok.val;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ConnectionCloseHeaderInterceptorTest {

    private final Chain chain = mock(Chain.class);

    @Test
    void no_header_is_set() throws IOException {
        val request = new Request.Builder()
            .url("http://localhost/")
            .build();
        when(chain.request()).thenReturn(request);

        new ConnectionCloseHeaderInterceptor().intercept(chain);

        val requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(chain, times(1)).proceed(requestCaptor.capture());

        assertThat(requestCaptor.getValue().header("Connection"), equalTo("close"));
    }

    @Test
    void header_is_set() throws IOException {
        val request = new Request.Builder().url("http://localhost/")
            .header("connection", "keep-alive")
            .build();
        when(chain.request()).thenReturn(request);

        new ConnectionCloseHeaderInterceptor().intercept(chain);

        val requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(chain, times(1)).proceed(requestCaptor.capture());

        assertThat(requestCaptor.getValue().header("Connection"), equalTo("keep-alive"));
    }

}
