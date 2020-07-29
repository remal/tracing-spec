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

import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import org.junit.jupiter.api.Test;

class SpecSpansFiltersTest {

    @Test
    void specSpansWithName() throws Throwable {
        val filter = SpecSpansFilters.specSpansWithName();
        assertThat(
            filter.test(SpecSpan.builder().spanId("0")
                .name("string")
                .build()
            ),
            equalTo(true)
        );
        assertThat(
            filter.test(SpecSpan.builder().spanId("0")
                .build()
            ),
            equalTo(false)
        );
    }

    @Test
    void specSpansWithServiceName() throws Throwable {
        val filter = SpecSpansFilters.specSpansWithServiceName();
        assertThat(
            filter.test(SpecSpan.builder().spanId("0")
                .serviceName("string")
                .build()
            ),
            equalTo(true)
        );
        assertThat(
            filter.test(SpecSpan.builder().spanId("0")
                .build()
            ),
            equalTo(false)
        );
    }

    @Test
    void specSpansWithDescription() throws Throwable {
        val filter = SpecSpansFilters.specSpansWithDescription();
        assertThat(
            filter.test(SpecSpan.builder().spanId("0")
                .description("string")
                .build()
            ),
            equalTo(true)
        );
        assertThat(
            filter.test(SpecSpan.builder().spanId("0")
                .build()
            ),
            equalTo(false)
        );
    }

}
