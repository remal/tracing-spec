package name.remal.tracingspec.model;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static utils.test.json.JsonUtils.readJsonString;
import static utils.test.json.JsonUtils.writeJsonString;

import lombok.val;
import org.junit.jupiter.api.Test;

class SpecSpanKindTest {

    @Test
    void parseSpecSpanKind() {
        assertThat("null", SpecSpanKind.parseSpecSpanKind(null), nullValue());
        for (val kind : SpecSpanKind.values()) {
            for (val string : asList(kind.name(), kind.name().toUpperCase(), kind.name().toLowerCase())) {
                assertThat(string, SpecSpanKind.parseSpecSpanKind(string), equalTo(kind));
            }
        }
    }

    @Test
    void deserialization() {
        for (val kind : SpecSpanKind.values()) {
            for (val string : asList(kind.name(), kind.name().toUpperCase(), kind.name().toLowerCase())) {
                val json = writeJsonString(string);
                assertThat(string, readJsonString(json, SpecSpanKind.class), equalTo(kind));
            }
        }
    }

}
