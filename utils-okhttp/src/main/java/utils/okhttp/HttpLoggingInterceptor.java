package utils.okhttp;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static okhttp3.internal.http.HttpHeaders.hasBody;
import static org.apache.commons.lang3.StringUtils.repeat;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.GzipSource;
import org.apache.logging.log4j.LogManager;

public class HttpLoggingInterceptor implements Interceptor {

    private final Consumer<CharSequence> logger;
    private final BiConsumer<CharSequence, Throwable> exceptionLogger;
    private final LongSupplier nanoTimeSupplier;

    public HttpLoggingInterceptor() {
        val loggerImpl = LogManager.getLogger(HttpLoggingInterceptor.class);
        this.logger = loggerImpl::debug;
        this.exceptionLogger = loggerImpl::debug;
        this.nanoTimeSupplier = System::nanoTime;
    }

    HttpLoggingInterceptor(
        Consumer<CharSequence> logger,
        BiConsumer<CharSequence, Throwable> exceptionLogger,
        LongSupplier nanoTimeSupplier
    ) {
        this.logger = logger;
        this.exceptionLogger = exceptionLogger;
        this.nanoTimeSupplier = nanoTimeSupplier;
    }

    @Override
    @SneakyThrows
    public Response intercept(Chain chain) {
        val messageBuilder = new StringBuilder();

        appendRequest(chain, messageBuilder);

        long startNanos = nanoTimeSupplier.getAsLong();
        Response response;
        final long tookMillis;
        try {
            response = chain.proceed(chain.request());

        } catch (Throwable e) {
            tookMillis = NANOSECONDS.toMillis(nanoTimeSupplier.getAsLong() - startNanos);
            messageBuilder.append("\n\n<-- HTTP FAILED (took ").append(tookMillis).append(" ms): ").append(e);
            exceptionLogger.accept(messageBuilder, e);
            throw e;
        }

        tookMillis = NANOSECONDS.toMillis(nanoTimeSupplier.getAsLong() - startNanos);
        appendResponse(response, tookMillis, messageBuilder);

        logger.accept(messageBuilder);

        return response;
    }

    @SneakyThrows
    @SuppressWarnings("java:S3776")
    private static void appendRequest(Chain chain, StringBuilder messageBuilder) {
        val request = chain.request();

        messageBuilder.append("--> ").append(request.method());

        HttpUrl url = request.url();
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

                if (OkhttpUtils.isPlainText(buffer)) {
                    appendPlainTextRequestBody(requestBody, buffer, messageBuilder);

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

    @SneakyThrows
    private static void appendPlainTextRequestBody(
        RequestBody requestBody,
        Buffer buffer,
        StringBuilder messageBuilder
    ) {
        if (requestBody.contentLength() <= 0) {
            return;
        }

        val charset = Optional.ofNullable(requestBody.contentType())
            .map(MediaType::charset)
            .orElse(UTF_8);

        messageBuilder
            .append("\n\n")
            .append(buffer.readString(charset));
    }

    @SneakyThrows
    @SuppressWarnings({"java:S3776", "java:S2095"})
    private static void appendResponse(Response response, long tookMillis, StringBuilder messageBuilder) {
        messageBuilder.append("\n\n<-- ").append(response.code());

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
            Buffer buffer = source.getBuffer();

            Long gzippedLength = null;
            if ("gzip".equalsIgnoreCase(response.header("Content-Encoding"))) {
                gzippedLength = buffer.size();
                try (val gzippedResponseBody = new GzipSource(buffer.clone())) {
                    buffer = new Buffer();
                    buffer.writeAll(gzippedResponseBody);
                }
            }

            if (OkhttpUtils.isPlainText(buffer)) {
                appendPlainTextResponseBody(responseBody, buffer, messageBuilder);

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

    @SneakyThrows
    private static void appendPlainTextResponseBody(
        ResponseBody responseBody,
        Buffer buffer,
        StringBuilder messageBuilder
    ) {
        if (responseBody.contentLength() <= 0) {
            return;
        }

        val charset = Optional.ofNullable(responseBody.contentType())
            .map(MediaType::charset)
            .orElse(UTF_8);

        messageBuilder
            .append("\n\n")
            .append(buffer.snapshot().string(charset));
    }

    private static final Pattern AUTH_WITH_SCHEME = Pattern.compile(
        "^(?:Basic|Bearer|Digest|HOBA|Mutual|Negotiate|OAuth|SCRAM-SHA-\\d+|vapid) ",
        CASE_INSENSITIVE
    );

    private static void appendHeaders(Headers headers, StringBuilder messageBuilder) {
        appendHeaders(headers, messageBuilder, header -> true);
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

            String value = headers.value(headersIndex);

            if ("Authorization".equalsIgnoreCase(name)) {
                val matcher = AUTH_WITH_SCHEME.matcher(value);
                if (matcher.find()) {
                    value = value.substring(0, matcher.end()) + repeat('*', value.length() - matcher.end());
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
