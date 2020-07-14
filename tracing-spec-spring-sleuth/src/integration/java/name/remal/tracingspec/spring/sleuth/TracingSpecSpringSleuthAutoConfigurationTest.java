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

package name.remal.tracingspec.spring.sleuth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.stereotype.Component;
import test.sleuth.TestSpanHandler;

@SpringBootTest
@SpringBootApplication
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
class TracingSpecSpringSleuthAutoConfigurationTest {

    private final TracedLogic tracedLogic;

    private final TestSpanHandler testSpanHandler;

    @Test
    @SuppressWarnings({"UnusedAssignment", "java:S881"})
    void test() {
        int executionsCounter = 0;

        tracedLogic.syncExecute();
        {
            val span = testSpanHandler.get(executionsCounter++);
            assertThat(span, notNullValue());
            assertThat(span.name(), equalTo("inner-sync-logic"));
            assertThat(span.tag("spec.description"), equalTo("1.2"));
            assertThat(span.tag("spec.is-async"), nullValue());
        }
        {
            val span = testSpanHandler.get(executionsCounter++);
            assertThat(span, notNullValue());
            assertThat(span.name(), equalTo("sync-logic"));
            assertThat(span.tag("spec.description"), equalTo("1.1"));
            assertThat(span.tag("spec.is-async"), nullValue());
        }


        tracedLogic.asyncExecute();
        {
            val span = testSpanHandler.get(executionsCounter++);
            assertThat(span, notNullValue());
            assertThat(span.name(), equalTo("inner-async-logic"));
            assertThat(span.tag("spec.description"), equalTo("2.2"));
            assertThat(span.tag("spec.is-async"), equalTo("true"));
        }
        {
            val span = testSpanHandler.get(executionsCounter++);
            assertThat(span, notNullValue());
            assertThat(span.name(), equalTo("async-logic"));
            assertThat(span.tag("spec.description"), equalTo("2.1"));
            assertThat(span.tag("spec.is-async"), equalTo("true"));
        }
    }


    @Component
    @RequiredArgsConstructor
    public static class TracedLogic {

        private final TracedLogicInner inner;

        @NewSpan(name = "sync-logic")
        @SpecSpan(description = "1.1")
        public synchronized void syncExecute() {
            inner.syncExecute();
        }

        @NewSpan(name = "async-logic")
        @SpecSpan(description = "2.1", isAsync = true)
        public synchronized void asyncExecute() {
            inner.asyncExecute();
        }

    }

    @Component
    @RequiredArgsConstructor
    public static class TracedLogicInner {

        @NewSpan(name = "inner-sync-logic")
        @SpecSpan(description = "1.2")
        public synchronized void syncExecute() {
            // do nothing
        }

        @NewSpan(name = "inner-async-logic")
        @SpecSpan(description = "2.2", isAsync = true)
        public synchronized void asyncExecute() {
            // do nothing
        }

    }

}
