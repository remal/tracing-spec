package utils.okhttp;

import static okhttp3.Protocol.HTTP_1_1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.val;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

class ErroneousResponseInterceptorTest {

    private final ErroneousResponseInterceptor interceptor = new ErroneousResponseInterceptor();

    private final Request request = new Request.Builder().url("http://localhost/").build();

    private final Chain chain = mock(Chain.class);

    {
        when(chain.request()).thenReturn(request);
    }

    @Test
    void exception_is_thrown() throws Throwable {
        when(chain.proceed(request)).thenThrow(AssertionError.class);
        assertThrows(ErroneousResponseException.class, () -> interceptor.intercept(chain));
    }

    @Test
    void successful_response() throws Throwable {
        val response = new Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(200)
            .message("successful")
            .build();
        when(chain.proceed(request)).thenReturn(response);
        assertThat(interceptor.intercept(chain), equalTo(response));
    }

    @Test
    void erroneous_response() throws Throwable {
        val response = new Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(404)
            .message("erroneous")
            .build();
        when(chain.proceed(request)).thenReturn(response);
        assertThrows(ErroneousResponseException.class, () -> interceptor.intercept(chain));
    }

}
