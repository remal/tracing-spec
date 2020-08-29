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

import static java.time.Instant.ofEpochSecond;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class SpecSpanAnnotationTest {

    @Test
    void compareTo() {
        assertThat(new SpecSpanAnnotation("").compareTo(new SpecSpanAnnotation("")), equalTo(0));
        assertThat(new SpecSpanAnnotation(ofEpochSecond(1), "").compareTo(new SpecSpanAnnotation("")), equalTo(-1));
        assertThat(new SpecSpanAnnotation("").compareTo(new SpecSpanAnnotation(ofEpochSecond(1), "")), equalTo(1));

        assertThat(
            new SpecSpanAnnotation(ofEpochSecond(1), "").compareTo(new SpecSpanAnnotation(ofEpochSecond(1), "")),
            equalTo(0)
        );
        assertThat(
            new SpecSpanAnnotation(ofEpochSecond(2), "").compareTo(new SpecSpanAnnotation(ofEpochSecond(1), "")),
            equalTo(1)
        );
        assertThat(
            new SpecSpanAnnotation(ofEpochSecond(1), "").compareTo(new SpecSpanAnnotation(ofEpochSecond(2), "")),
            equalTo(-1)
        );
    }

}
