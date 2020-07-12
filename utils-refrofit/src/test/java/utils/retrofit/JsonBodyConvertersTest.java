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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;

class JsonBodyConvertersTest {

    private final JsonBodyConverters converters = new JsonBodyConverters();

    private final Retrofit retrofit = mock(Retrofit.class, invocation -> {
        throw new UnsupportedOperationException();
    });

    @Test
    void requestBodyConverter() throws IOException {
        val converter = converters.requestBodyConverter(int.class, new Annotation[0], new Annotation[0], retrofit);
        assertThat(converter, notNullValue());

        val requestBody = converter.convert(ImmutableMap.of("field", "value"));
        assertThat(requestBody, notNullValue());
        assertThat(requestBody.contentType(), equalTo(MediaType.get("application/json; charset=UTF-8")));

        val buffer = new Buffer();
        requestBody.writeTo(buffer);
        assertThat(buffer.readString(UTF_8), equalTo("{\"field\":\"value\"}"));
    }

    @Test
    void responseBodyConverter() throws IOException {
        val type = new TypeToken<Map<String, String>>() { }.getType();
        val converter = converters.responseBodyConverter(type, new Annotation[0], retrofit);
        assertThat(converter, notNullValue());

        val response = converter.convert(ResponseBody.create(null, "{\"field\":\"value\"}"));
        assertThat(response, notNullValue());
        assertThat(response, equalTo(ImmutableMap.of("field", "value")));
    }

}
