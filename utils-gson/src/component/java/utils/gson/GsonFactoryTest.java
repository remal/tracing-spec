package utils.gson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static utils.gson.GsonFactory.getGsonInstance;

import com.google.gson.annotations.SerializedName;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S2789")
class GsonFactoryTest {

    @Test
    void case_insensitive_enum() {
        assertThat(
            getGsonInstance().fromJson("null", TestEnum.class),
            nullValue()
        );
        assertThat(
            getGsonInstance().fromJson("\"unknown\"", TestEnum.class),
            nullValue()
        );

        assertThat(
            getGsonInstance().fromJson("\"VALUE\"", TestEnum.class),
            equalTo(TestEnum.VALUE)
        );
        assertThat(
            getGsonInstance().fromJson("\"c\"", TestEnum.class),
            equalTo(TestEnum.COMPLEX)
        );
        assertThat(
            getGsonInstance().fromJson("\"com\"", TestEnum.class),
            equalTo(TestEnum.COMPLEX)
        );
        assertThat(
            getGsonInstance().fromJson("\"value\"", TestEnum.class),
            equalTo(TestEnum.COMPLEX)
        );
        assertThat(
            getGsonInstance().fromJson("\"C\"", TestEnum.class),
            equalTo(TestEnum.COMPLEX)
        );
        assertThat(
            getGsonInstance().fromJson("\"COM\"", TestEnum.class),
            equalTo(TestEnum.COMPLEX)
        );
    }

    private enum TestEnum {
        VALUE,
        @SerializedName(value = "c", alternate = {"com", "value"})
        COMPLEX,
    }


    @Test
    void java_util_Optional() {
        assertThat(
            getGsonInstance().fromJson("{\"field\":null}", OptionalField.class).field,
            equalTo(Optional.empty())
        );
        assertThat(
            getGsonInstance().fromJson("{\"field\":\"string\"}", OptionalField.class).field,
            equalTo(Optional.of("string"))
        );

        assertThat(
            getGsonInstance().toJson(new OptionalField(Optional.empty())),
            equalTo("{}")
        );
        assertThat(
            getGsonInstance().toJson(new OptionalField(Optional.of("string"))),
            equalTo("{\"field\":\"string\"}")
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class OptionalField {
        @Nullable
        public Optional<String> field;
    }


    @Test
    void java_util_OptionalDouble() {
        assertThat(
            getGsonInstance().fromJson("{\"field\":null}", OptionalDoubleField.class).field,
            equalTo(OptionalDouble.empty())
        );
        assertThat(
            getGsonInstance().fromJson("{\"field\":1.1}", OptionalDoubleField.class).field,
            equalTo(OptionalDouble.of(1.1))
        );

        assertThat(
            getGsonInstance().toJson(new OptionalDoubleField(OptionalDouble.empty())),
            equalTo("{}")
        );
        assertThat(
            getGsonInstance().toJson(new OptionalDoubleField(OptionalDouble.of(1.1))),
            equalTo("{\"field\":1.1}")
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class OptionalDoubleField {
        @Nullable
        public OptionalDouble field;
    }


    @Test
    void java_util_OptionalInt() {
        assertThat(
            getGsonInstance().fromJson("{\"field\":null}", OptionalIntField.class).field,
            equalTo(OptionalInt.empty())
        );
        assertThat(
            getGsonInstance().fromJson("{\"field\":1}", OptionalIntField.class).field,
            equalTo(OptionalInt.of(1))
        );

        assertThat(
            getGsonInstance().toJson(new OptionalIntField(OptionalInt.empty())),
            equalTo("{}")
        );
        assertThat(
            getGsonInstance().toJson(new OptionalIntField(OptionalInt.of(1))),
            equalTo("{\"field\":1}")
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class OptionalIntField {
        @Nullable
        public OptionalInt field;
    }


    @Test
    void java_util_OptionalLong() {
        assertThat(
            getGsonInstance().fromJson("{\"field\":null}", OptionalLongField.class).field,
            equalTo(OptionalLong.empty())
        );
        assertThat(
            getGsonInstance().fromJson("{\"field\":1}", OptionalLongField.class).field,
            equalTo(OptionalLong.of(1))
        );

        assertThat(
            getGsonInstance().toJson(new OptionalLongField(OptionalLong.empty())),
            equalTo("{}")
        );
        assertThat(
            getGsonInstance().toJson(new OptionalLongField(OptionalLong.of(1))),
            equalTo("{\"field\":1}")
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class OptionalLongField {
        @Nullable
        public OptionalLong field;
    }

}
