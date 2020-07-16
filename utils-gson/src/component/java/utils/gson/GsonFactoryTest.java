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

package utils.gson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.gson.GsonFactory.getGsonInstance;

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
