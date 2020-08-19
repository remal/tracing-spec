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

package name.remal.tracingspec.model.internal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;
import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.val;
import org.junit.jupiter.api.Test;

class JacksonTracingSpecBuilderSettingsModuleTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .enable(SORT_PROPERTIES_ALPHABETICALLY);

    @Test
    void properties_inclusion() throws Throwable {
        val serialized = OBJECT_MAPPER.writeValueAsString(new PropertiesInclusionClass());
        assertThat(serialized, equalTo("{\"boolValue\":false,\"stringValue\":\"value\"}"));
    }

    private static class PropertiesInclusionClass {
        @Nullable
        public String nullField = null;

        public Optional<String> emptyValue = Optional.empty();

        public Optional<String> stringValue = Optional.of("value");

        public boolean boolValue = false;

        @JsonInclude(NON_DEFAULT)
        public boolean boolNonDefaultValue = false;
    }


    @Test
    void builder_lombok() throws Throwable {
        val deserialized = OBJECT_MAPPER.readValue("{}", LombokWithBuilderClass.class);
        assertThat(deserialized, equalTo(LombokWithBuilderClass.builder().build()));
    }

    @lombok.Value
    @lombok.Builder
    private static class LombokWithBuilderClass {
        @lombok.Builder.Default
        String field = "default value";
    }


    @Test
    void builder_immutables_immutable_interface() throws Throwable {
        val deserialized = OBJECT_MAPPER.readValue("{}", ImmutableWithBuilderClass.class);
        assertThat(deserialized, equalTo(ImmutableImmutableWithBuilderClass.builder().build()));
    }

    @Test
    void builder_immutables_immutable_implementation() throws Throwable {
        val deserialized = OBJECT_MAPPER.readValue("{}", ImmutableImmutableWithBuilderClass.class);
        assertThat(deserialized, equalTo(ImmutableImmutableWithBuilderClass.builder().build()));
    }

    @org.immutables.value.Value.Immutable
    public interface ImmutableWithBuilderClass {
        @org.immutables.value.Value.Default
        default String getField() {
            return "default value";
        }
    }

}
