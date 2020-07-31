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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.tracing.SpecSpanGraphNodeGenerator.nextSpecSpansGraphNodeBuilder;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class SpecSpansGraphNodeTest {

    @Test
    void compareTo_no_startedAt_no_startedAt() {
        assertThat(
            nextSpecSpansGraphNodeBuilder()
                .build()
                .compareTo(
                    nextSpecSpansGraphNodeBuilder()
                        .build()
                ),
            equalTo(0)
        );
    }

    @Test
    void compareTo_startedAt_startedAt() {
        assertThat(
            nextSpecSpansGraphNodeBuilder()
                .startedAt(Instant.ofEpochSecond(1))
                .build()
                .compareTo(
                    nextSpecSpansGraphNodeBuilder()
                        .startedAt(Instant.ofEpochSecond(1))
                        .build()
                ),
            equalTo(0)
        );
        assertThat(
            nextSpecSpansGraphNodeBuilder()
                .startedAt(Instant.ofEpochSecond(1))
                .build()
                .compareTo(
                    nextSpecSpansGraphNodeBuilder()
                        .startedAt(Instant.ofEpochSecond(2))
                        .build()
                ),
            equalTo(-1)
        );
        assertThat(
            nextSpecSpansGraphNodeBuilder()
                .startedAt(Instant.ofEpochSecond(2))
                .build()
                .compareTo(
                    nextSpecSpansGraphNodeBuilder()
                        .startedAt(Instant.ofEpochSecond(1))
                        .build()
                ),
            equalTo(1)
        );
    }

    @Test
    void compareTo_no_startedAt_startedAt() {
        assertThat(
            nextSpecSpansGraphNodeBuilder()
                .build()
                .compareTo(
                    nextSpecSpansGraphNodeBuilder()
                        .startedAt(Instant.ofEpochSecond(1))
                        .build()
                ),
            equalTo(1)
        );
    }

    @Test
    void compareTo_startedAt_no_startedAt() {
        assertThat(
            nextSpecSpansGraphNodeBuilder()
                .startedAt(Instant.ofEpochSecond(1))
                .build()
                .compareTo(
                    nextSpecSpansGraphNodeBuilder()
                        .build()
                ),
            equalTo(-1)
        );
    }

}
