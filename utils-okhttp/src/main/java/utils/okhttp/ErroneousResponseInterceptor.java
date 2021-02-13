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
        sb.append(": ").append(response.code());

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
