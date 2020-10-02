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

package name.remal.tracingspec.application;

import static name.remal.tracingspec.application.ExitException.findExitException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExitExceptionTest {

    @Nested
    class FindExitException {

        @Test
        void simple() {
            val exception = new ExitException(0);
            assertThat(findExitException(exception), sameInstance(exception));
        }

        @Test
        void cause() {
            val exception = new ExitException(0);
            val root = new RuntimeException(exception);
            assertThat(findExitException(root), sameInstance(exception));
        }

        @Test
        void suppressed() {
            val exception = new ExitException(0);
            val root = new RuntimeException();
            root.addSuppressed(exception);
            assertThat(findExitException(root), sameInstance(exception));
        }

    }

}
