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
