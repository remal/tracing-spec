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
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;
import org.junit.jupiter.api.Test;

class ServiceNameTransformersTest {

    @Test
    void removeServiceNamePrefixes() throws Throwable {
        val transformer = ServiceNameTransformers.removeServiceNamePrefixes("a", "b");
        assertThat(transformer.transform("ababcab"), equalTo("cab"));
    }

    @Test
    void removeServiceNamePrefixes_empty_element() {
        assertThrows(IllegalArgumentException.class, () ->
            ServiceNameTransformers.removeServiceNamePrefixes("")
        );
        assertThrows(IllegalArgumentException.class, () ->
            ServiceNameTransformers.removeServiceNamePrefixes((String) null)
        );
    }


    @Test
    void removeServiceNameSuffixes() throws Throwable {
        val transformer = ServiceNameTransformers.removeServiceNameSuffixes("a", "b");
        assertThat(transformer.transform("abcabab"), equalTo("abc"));
    }

    @Test
    void removeServiceNameSuffixes_empty_element() {
        assertThrows(IllegalArgumentException.class, () ->
            ServiceNameTransformers.removeServiceNameSuffixes("")
        );
        assertThrows(IllegalArgumentException.class, () ->
            ServiceNameTransformers.removeServiceNameSuffixes((String) null)
        );
    }

}
