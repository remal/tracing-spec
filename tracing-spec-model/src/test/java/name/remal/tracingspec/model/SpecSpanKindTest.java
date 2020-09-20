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
