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

import static okhttp3.Protocol.HTTP_1_1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import lombok.val;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Response;

class OptionalCallAdapterTest {

    private final OptionalCallAdapter adapter = new OptionalCallAdapter(int.class);

    private final Request request = new Request.Builder().url("http://localhost/").build();

    @SuppressWarnings("unchecked")
    private final Call<Object> call = mock(Call.class);

    {
        when(call.request()).thenReturn(request);
    }

    @Test
    void successful_no_raw_body() throws IOException {
        val response = Response.success(null, new okhttp3.Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(200)
            .message("successful")
            .build()
        );
        when(call.execute()).thenReturn(response);

        assertThat(adapter.adapt(call), equalTo(Optional.empty()));
    }

    @Test
    void successful_content_length_equals_to_0() throws IOException {
        val response = Response.success(null, new okhttp3.Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(200)
            .message("successful")
            .body(ResponseBody.create(null, ""))
            .build()
        );
        when(call.execute()).thenReturn(response);

        assertThat(adapter.adapt(call), equalTo(Optional.empty()));
    }

    @Test
    void successful_204() throws IOException {
        val response = Response.success(null, new okhttp3.Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(204)
            .message("successful")
            .body(ResponseBody.create(null, "123"))
            .build()
        );
        when(call.execute()).thenReturn(response);

        assertThat(adapter.adapt(call), equalTo(Optional.empty()));
    }

    @Test
    void successful_null_body() throws IOException {
        val response = Response.success(null, new okhttp3.Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(200)
            .message("successful")
            .body(ResponseBody.create(null, "123"))
            .build()
        );
        when(call.execute()).thenReturn(response);

        assertThat(adapter.adapt(call), equalTo(Optional.empty()));
    }

    @Test
    void successful_with_body() throws IOException {
        val response = Response.success((Object) Optional.of("text"), new okhttp3.Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(200)
            .message("successful")
            .body(ResponseBody.create(null, "123"))
            .build()
        );
        when(call.execute()).thenReturn(response);

        assertThat(adapter.adapt(call), equalTo(Optional.of("text")));
    }

    @Test
    void erroneous_404() throws IOException {
        val response = Response.error(404, ResponseBody.create(null, ""));
        when(call.execute()).thenReturn(response);

        assertThat(adapter.adapt(call), equalTo(Optional.empty()));
    }

    @Test
    void erroneous() throws IOException {
        val response = Response.error(500, ResponseBody.create(null, ""));
        when(call.execute()).thenReturn(response);

        assertThrows(RetrofitCallException.class, () -> adapter.adapt(call));
    }

}
