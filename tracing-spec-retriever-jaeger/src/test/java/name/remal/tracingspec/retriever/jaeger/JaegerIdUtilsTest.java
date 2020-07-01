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

package name.remal.tracingspec.retriever.jaeger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S109")
class JaegerIdUtilsTest {

    @Test
    void encodeJaegerId() {
        assertThat(JaegerIdUtils.encodeJaegerId("01ff09"), equalTo(new byte[]{1, -1, 9}));
    }

    @Test
    void encodeJaegerId_upper_case() {
        assertThat(JaegerIdUtils.encodeJaegerId("01FF09"), equalTo(new byte[]{1, -1, 9}));
    }

    @Test
    void encodeJaegerId_odd_length() {
        assertThat(JaegerIdUtils.encodeJaegerId("1ff09"), equalTo(new byte[]{1, -1, 9}));
    }


    @Test
    void decodeJaegerTraceId() {
        assertThat(JaegerIdUtils.decodeJaegerTraceId(new byte[]{1, -1, 9}), equalTo("000000000001ff09"));
    }

    @Test
    void decodeJaegerTraceId_redundant_leading_zero_bytes() {
        assertThat(
            JaegerIdUtils.decodeJaegerTraceId(new byte[]{
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 9
            }),
            equalTo("000000000001ff09")
        );
    }


    @Test
    void decodeJaegerSpanId() {
        assertThat(JaegerIdUtils.decodeJaegerSpanId(new byte[]{1, -1, 9}), equalTo("1ff09"));
    }

    @Test
    void decodeJaegerSpanId_extra_short() {
        assertThat(JaegerIdUtils.decodeJaegerSpanId(new byte[]{}), equalTo(""));
        assertThat(JaegerIdUtils.decodeJaegerSpanId(new byte[]{1}), equalTo("1"));
    }

}
