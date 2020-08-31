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

package name.remal.tracingspec.model;

import static name.remal.tracingspec.model.SpecSpanKind.CLIENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.test.json.ObjectMapperProvider.readJsonResource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.val;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

class SpecSpanTest extends SpecSpanInfoTest<SpecSpan> {

    @Test
    void spanId_cant_be_empty() {
        assertThrows(IllegalArgumentException.class, () -> new SpecSpan(""));
    }

    @Override
    protected Map<String, Pair<BiConsumer<SpecSpan, String>, Function<SpecSpan, String>>> getNullableStringProps() {
        val props = new LinkedHashMap<>(super.getNullableStringProps());
        props.put(
            "parentSpanId",
            ImmutablePair.of(SpecSpan::setParentSpanId, SpecSpan::getParentSpanId)
        );
        return props;
    }

    @Test
    void deserialization() {
        val expected = new SpecSpan("12345678");
        expected.setParentSpanId("87654321");
        expected.setName("name");
        expected.setKind(CLIENT);
        expected.setAsync(true);
        expected.setDescription("description");
        expected.addAnnotation(new SpecSpanAnnotation("annotation"));

        val deserialized = readJsonResource("spec-span.json", SpecSpan.class);
        deserialized.getTags().clear();

        assertThat(
            deserialized,
            equalTo(expected)
        );
    }

}
