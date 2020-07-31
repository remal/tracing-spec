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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.test.tracing.SpecSpanGraphNodeGenerator.nextSpecSpansGraphNodeBuilder;

import lombok.val;
import org.junit.jupiter.api.Test;

class SpecSpansGraphUtilsTest {

    private static final SpecSpansGraphNode PARENT = nextSpecSpansGraphNodeBuilder()
        .addChild(nextSpecSpansGraphNodeBuilder()
            .name("child 1")
            .build()
        )
        .addChild(nextSpecSpansGraphNodeBuilder()
            .name("child 2")
            .build()
        )
        .build();

    private static final SpecSpansGraphNode CHILD_1 = PARENT.getChildren().get(0);
    private static final SpecSpansGraphNode CHILD_2 = PARENT.getChildren().get(1);

    @Test
    void getPreviousNodeFor_root() {
        assertThat(
            SpecSpansGraphUtils.getPreviousNodeFor(PARENT, null),
            nullValue()
        );
    }

    @Test
    void getNextNodeFor_root() {
        assertThat(
            SpecSpansGraphUtils.getNextNodeFor(PARENT, null),
            nullValue()
        );
    }

    @Test
    void getPreviousNodeFor() {
        assertThat(
            SpecSpansGraphUtils.getPreviousNodeFor(CHILD_1, PARENT),
            nullValue()
        );
        assertThat(
            SpecSpansGraphUtils.getPreviousNodeFor(CHILD_2, PARENT),
            equalTo(CHILD_1)
        );
    }

    @Test
    void getNextNodeFor() {
        assertThat(
            SpecSpansGraphUtils.getNextNodeFor(CHILD_1, PARENT),
            equalTo(CHILD_2)
        );
        assertThat(
            SpecSpansGraphUtils.getNextNodeFor(CHILD_2, PARENT),
            nullValue()
        );
    }

    @Test
    void getPreviousNodeFor_invalid_parent() {
        val invalidParent = nextSpecSpansGraphNodeBuilder().build();
        assertThrows(IllegalStateException.class, () -> SpecSpansGraphUtils.getPreviousNodeFor(PARENT, invalidParent));
    }

    @Test
    void getNextNodeFor_invalid_parent() {
        val invalidParent = nextSpecSpansGraphNodeBuilder().build();
        assertThrows(IllegalStateException.class, () -> SpecSpansGraphUtils.getNextNodeFor(PARENT, invalidParent));
    }

}
