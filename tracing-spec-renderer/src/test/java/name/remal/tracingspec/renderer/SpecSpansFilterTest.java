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

package name.remal.tracingspec.renderer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class SpecSpansFilterTest {

    private static final SpecSpan span = SpecSpan.builder().spanId("0").build();

    private static final SpecSpansFilter trueFilter = span -> true;
    private static final SpecSpansFilter falseFilter = span -> false;

    @Test
    void and() throws Throwable {
        assertThat(
            trueFilter.and(trueFilter).test(span),
            equalTo(true)
        );
        assertThat(
            trueFilter.and(falseFilter).test(span),
            equalTo(false)
        );
        assertThat(
            falseFilter.and(trueFilter).test(span),
            equalTo(false)
        );
        assertThat(
            falseFilter.and(falseFilter).test(span),
            equalTo(false)
        );
    }

    @Test
    void or() throws Throwable {
        assertThat(
            trueFilter.or(trueFilter).test(span),
            equalTo(true)
        );
        assertThat(
            trueFilter.or(falseFilter).test(span),
            equalTo(true)
        );
        assertThat(
            falseFilter.or(trueFilter).test(span),
            equalTo(true)
        );
        assertThat(
            falseFilter.or(falseFilter).test(span),
            equalTo(false)
        );
    }

    @Test
    void negate() throws Throwable {
        assertThat(
            trueFilter.negate().test(span),
            equalTo(false)
        );
        assertThat(
            falseFilter.negate().test(span),
            equalTo(true)
        );
    }

}
