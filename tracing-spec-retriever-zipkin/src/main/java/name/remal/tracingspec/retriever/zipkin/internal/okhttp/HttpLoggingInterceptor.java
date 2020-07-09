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

package name.remal.tracingspec.retriever.zipkin.internal.okhttp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static name.remal.tracingspec.retriever.zipkin.internal.okhttp.OkhttpUtils.isPlaintext;
import static okhttp3.internal.http.HttpHeaders.hasBody;
import static org.apache.commons.lang3.StringUtils.repeat;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import name.remal.gradle_plugins.api.ExcludeFromCodeCoverage;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okio.Buffer;
import okio.GzipSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@ExcludeFromCodeCoverage
public class HttpLoggingInterceptor implements Interceptor {

    private static final Level LOG_LEVEL = Level.DEBUG;

    private static final Logger logger = LogManager.getLogger(HttpLoggingInterceptor.class);


    @Override
    @SneakyThrows
    @SuppressWarnings("java:S3776")
    public Response intercept(Chain chain) {
        val messageBuilder = new StringBuilder();

        val request = chain.request();
        {
            messageBuilder.append("--> ").append(request.method());

            var url = request.url();
            if (!url.password().isEmpty()) {
                url = url.newBuilder()
                    .encodedPassword(repeat('*', url.password().length()))
                    .build();
            }
            messageBuilder.append(' ').append(url);

            Optional.ofNullable(chain.connection()).ifPresent(connection ->
                messageBuilder.append(" (").append(connection.protocol()).append(')')
            );

            appendHeaders(request.headers(), messageBuilder, header ->
                !"Content-Type".equalsIgnoreCase(header)
                    && !"Content-Length".equalsIgnoreCase(header)
            );

            val requestBody = request.body();
            if (requestBody != null) {
                Optional.ofNullable(requestBody.contentType()).ifPresent(contentType ->
                    messageBuilder.append("\nContent-Type: ").append(contentType)
                );
                if (requestBody.contentLength() >= 0) {
                    messageBuilder.append("\nContent-Length: ").append(requestBody.contentLength());
                }

                if (bodyHasUnknownEncoding(request.headers())) {
                    messageBuilder.append("\n--> END ").append(request.method())
                        .append(" (encoded body omitted)");
                } else if (requestBody.isDuplex()) {
                    messageBuilder.append("\n--> END ").append(request.method())
                        .append(" (duplex request body omitted)");
                } else {
                    val buffer = new Buffer();
                    requestBody.writeTo(buffer);

                    if (isPlaintext(buffer)) {
                        val charset = Optional.ofNullable(requestBody.contentType())
                            .map(MediaType::charset)
                            .orElse(UTF_8);

                        if (requestBody.contentLength() > 0) {
                            messageBuilder
                                .append("\n\n")
                                .append(buffer.readString(charset));
                        }

                        messageBuilder
                            .append("\n--> END ")
                            .append(request.method())
                            .append(" (")
                            .append(requestBody.contentLength())
                            .append("-byte body)");

                    } else {
                        messageBuilder
                            .append("\n--> END ")
                            .append(request.method())
                            .append(" (binary ")
                            .append(requestBody.contentLength())
                            .append("-byte body omitted)");
                    }
                }
            } else {
                messageBuilder.append("\n--> END ").append(request.method());
            }
        }

        long startNanos = System.nanoTime();
        Response response;
        final long tookMillis;
        try {
            response = chain.proceed(request);

        } catch (Throwable e) {
            tookMillis = NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            messageBuilder.append("\n<-- HTTP FAILED (took ").append(tookMillis).append(" ms): ").append(e);
            logger.log(LOG_LEVEL, messageBuilder, e);
            throw e;
        }

        tookMillis = NANOSECONDS.toMillis(System.nanoTime() - startNanos);

        {
            messageBuilder.append("\n<-- ").append(response.code());

            val message = response.message();
            if (!message.isEmpty()) {
                messageBuilder.append(' ').append(message);
            }

            messageBuilder.append(" (took ").append(tookMillis).append(" ms)");

            appendHeaders(response.headers(), messageBuilder);

            val responseBody = response.body();
            if (responseBody == null || !hasBody(response)) {
                messageBuilder.append("\n<-- END HTTP");
            } else if (bodyHasUnknownEncoding(response.headers())) {
                messageBuilder.append("\n<-- END HTTP (encoded body omitted)");
            } else {
                val source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body
                var buffer = source.getBuffer();

                Long gzippedLength = null;
                if ("gzip".equalsIgnoreCase(response.header("Content-Encoding"))) {
                    gzippedLength = buffer.size();
                    try (val gzippedResponseBody = new GzipSource(buffer.clone())) {
                        buffer = new Buffer();
                        buffer.writeAll(gzippedResponseBody);
                    }
                }

                if (isPlaintext(buffer)) {
                    val charset = Optional.ofNullable(responseBody.contentType())
                        .map(MediaType::charset)
                        .orElse(UTF_8);

                    if (responseBody.contentLength() > 0) {
                        messageBuilder
                            .append("\n\n")
                            .append(buffer.snapshot().string(charset));
                    }

                    if (gzippedLength != null) {
                        messageBuilder
                            .append("\n<-- END HTTP (")
                            .append(buffer.size())
                            .append("-byte, ")
                            .append(gzippedLength)
                            .append("-gzipped-byte body)");
                    } else {
                        messageBuilder.append("\n<-- END HTTP (").append(buffer.size()).append("-byte body)");
                    }

                } else {
                    messageBuilder
                        .append("\n<-- END HTTP (binary ")
                        .append(buffer.size())
                        .append("-byte body omitted)");
                }
            }
        }

        logger.log(LOG_LEVEL, messageBuilder);

        return response;
    }

    private static final Pattern AUTH_WITH_SCHEME = Pattern.compile(
        "^(?:Basic|Bearer|Digest|HOBA|Mutual|Negotiate|OAuth|SCRAM-SHA-\\d+|vapid) ",
        CASE_INSENSITIVE
    );

    private static void appendHeaders(Headers headers, StringBuilder messageBuilder) {
        appendHeaders(headers, messageBuilder, __ -> true);
    }

    private static void appendHeaders(
        Headers headers,
        StringBuilder messageBuilder,
        Predicate<String> headerPredicate
    ) {
        for (int headersIndex = 0; headersIndex < headers.size(); ++headersIndex) {
            val name = headers.name(headersIndex);
            if (!headerPredicate.test(name)) {
                continue;
            }

            var value = headers.value(headersIndex);

            if ("Authorization".equalsIgnoreCase(name)) {
                val matcher = AUTH_WITH_SCHEME.matcher(value);
                if (matcher.find()) {
                    value = value.substring(0, matcher.end())
                        + repeat('*', value.length() - matcher.end());
                } else {
                    value = repeat('*', value.length());
                }
            }

            messageBuilder.append('\n').append(name).append(": ").append(value);
        }
    }

    private static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
            && !contentEncoding.equalsIgnoreCase("identity")
            && !contentEncoding.equalsIgnoreCase("gzip");
    }

}
