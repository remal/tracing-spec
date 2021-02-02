package name.remal.tracingspec.model;

import static name.remal.tracingspec.model.SpecSpanKind.CLIENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.test.json.JsonUtils.readJsonResource;

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
