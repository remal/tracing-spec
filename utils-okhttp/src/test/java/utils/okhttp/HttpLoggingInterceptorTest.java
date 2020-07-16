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

import static java.util.Collections.singletonList;
import static okhttp3.Protocol.HTTP_1_1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;
import lombok.val;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;

class HttpLoggingInterceptorTest {

    private final List<String> loggedMessages = new ArrayList<>();
    private final List<ExceptionMessage> loggedExceptionMessages = new ArrayList<>();

    private final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(
        message -> loggedMessages.add(message.toString()),
        (message, exception) -> loggedExceptionMessages.add(new ExceptionMessage(message.toString(), exception)),
        () -> 0
    );

    private final Chain chain = mock(Chain.class);


    @Test
    void successful_request() throws IOException {
        val request = new Request.Builder()
            .url("http://localhost/")
            .header("Connection", "close")
            .post(RequestBody.create(null, "request"))
            .build();
        when(chain.request()).thenReturn(request);

        val response = new Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(200)
            .message("successful")
            .header("ETag", "etag")
            .body(ResponseBody.create(null, "response"))
            .build();
        when(chain.proceed(request)).thenReturn(response);

        interceptor.intercept(chain);

        assertThat(loggedMessages, equalTo(singletonList(String.join(
            "\n",
            "--> POST http://localhost/",
            "Connection: close",
            "Content-Length: 7",
            "",
            "request",
            "--> END POST (7-byte body)",
            "",
            "<-- 200 successful (took 0 ms)",
            "ETag: etag",
            "",
            "response",
            "<-- END HTTP (8-byte body)"
        ))));

        assertThat(loggedExceptionMessages, empty());
    }

    @Test
    void erroneous_request() throws IOException {
        val request = new Request.Builder()
            .url("http://localhost/")
            .header("Connection", "close")
            .post(RequestBody.create(null, "request"))
            .build();
        when(chain.request()).thenReturn(request);

        val exception = new IllegalStateException("exception");
        when(chain.proceed(request)).thenThrow(exception);

        try {
            interceptor.intercept(chain);
            fail(exception.getClass().getSimpleName() + " exception expected");
        } catch (Throwable e) {
            if (e == exception) {
                // OK
            } else {
                fail("Incorrect exception thrown: " + e);
            }
        }

        assertThat(loggedMessages, empty());

        assertThat(loggedExceptionMessages, equalTo(singletonList(
            new ExceptionMessage(
                String.join(
                    "\n",
                    "--> POST http://localhost/",
                    "Connection: close",
                    "Content-Length: 7",
                    "",
                    "request",
                    "--> END POST (7-byte body)",
                    "",
                    "<-- HTTP FAILED (took 0 ms): " + exception.toString()
                ),
                exception
            )
        )));
    }

    @Test
    void password_in_url_are_redacted() throws IOException {
        val request = new Request.Builder()
            .url("http://user:password@localhost/")
            .build();
        when(chain.request()).thenReturn(request);

        val response = new Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(204)
            .message("No Content")
            .build();
        when(chain.proceed(request)).thenReturn(response);

        interceptor.intercept(chain);

        assertThat(loggedMessages, equalTo(singletonList(String.join(
            "\n",
            "--> GET http://user:********@localhost/",
            "--> END GET",
            "",
            "<-- 204 No Content (took 0 ms)",
            "<-- END HTTP"
        ))));

        assertThat(loggedExceptionMessages, empty());
    }

    @Test
    void authorization_header_is_redacted() throws IOException {
        val request = new Request.Builder()
            .url("http://localhost/")
            .header("authorization", "bearer 1234")
            .build();
        when(chain.request()).thenReturn(request);

        val response = new Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(204)
            .message("No Content")
            .build();
        when(chain.proceed(request)).thenReturn(response);

        interceptor.intercept(chain);

        assertThat(loggedMessages, equalTo(singletonList(String.join(
            "\n",
            "--> GET http://localhost/",
            "authorization: bearer ****",
            "--> END GET",
            "",
            "<-- 204 No Content (took 0 ms)",
            "<-- END HTTP"
        ))));

        assertThat(loggedExceptionMessages, empty());
    }


    @Value
    private static class ExceptionMessage {
        String message;
        Throwable exception;
    }

}
