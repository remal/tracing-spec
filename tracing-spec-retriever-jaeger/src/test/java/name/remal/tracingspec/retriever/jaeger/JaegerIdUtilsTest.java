package name.remal.tracingspec.retriever.jaeger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S109")
class JaegerIdUtilsTest {

    @Test
    void encodeJaegerId() {
        assertThat(JaegerIdUtils.encodeJaegerId("1ff09"), equalTo(new byte[]{0, 0, 0, 0, 0, 1, -1, 9}));
    }

    @Test
    void encodeJaegerId_upper_case() {
        assertThat(JaegerIdUtils.encodeJaegerId("1FF09"), equalTo(new byte[]{0, 0, 0, 0, 0, 1, -1, 9}));
    }

    @Test
    void encodeJaegerId_even_length() {
        assertThat(JaegerIdUtils.encodeJaegerId("01ff09"), equalTo(new byte[]{0, 0, 0, 0, 0, 1, -1, 9}));
    }

    @Test
    void encodeJaegerId_long() {
        assertThat(
            JaegerIdUtils.encodeJaegerId("1000000000001ff09"),
            equalTo(new byte[]{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, -1, 9})
        );
    }


    @Test
    void decodeJaegerId() {
        assertThat(JaegerIdUtils.decodeJaegerId(new byte[]{1, -1, 9}), equalTo("000000000001ff09"));
    }

    @Test
    void decodeJaegerId_long() {
        assertThat(
            JaegerIdUtils.decodeJaegerId(new byte[]{1, 0, 0, 0, 0, 0, 1, -1, 9}),
            equalTo("0000000000000001000000000001ff09")
        );
    }

}
