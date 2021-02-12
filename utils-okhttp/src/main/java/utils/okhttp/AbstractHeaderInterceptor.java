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
