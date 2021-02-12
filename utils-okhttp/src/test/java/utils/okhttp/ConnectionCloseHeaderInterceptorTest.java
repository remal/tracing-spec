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
