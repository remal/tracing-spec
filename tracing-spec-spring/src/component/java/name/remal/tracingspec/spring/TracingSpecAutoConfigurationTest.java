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

package name.remal.tracingspec.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.stereotype.Component;
import utils.test.sleuth.TestSpanHandler;

@SpringBootTest
@SpringBootApplication
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
class TracingSpecAutoConfigurationTest {

    private final TracedLogic tracedLogic;

    private final TestSpanHandler testSpanHandler;

    @Test
    @SuppressWarnings({"UnusedAssignment", "java:S881", "java:S1854", "java:S1199"})
    void test() {
        int executionsCounter = 0;

        tracedLogic.swagger2();
        {
            val span = testSpanHandler.get(executionsCounter++);
            assertThat(span, notNullValue());
            assertThat(span.name(), equalTo("inner-swagger2"));
            assertThat(span.tag("spec.description"), equalTo("1.2"));
        }
        {
            val span = testSpanHandler.get(executionsCounter++);
            assertThat(span, notNullValue());
            assertThat(span.name(), equalTo("swagger2"));
            assertThat(span.tag("spec.description"), equalTo("1.1"));
        }


        tracedLogic.swagger3();
        {
            val span = testSpanHandler.get(executionsCounter++);
            assertThat(span, notNullValue());
            assertThat(span.name(), equalTo("inner-swagger3"));
            assertThat(span.tag("spec.description"), equalTo("2.2"));
        }
        {
            val span = testSpanHandler.get(executionsCounter++);
            assertThat(span, notNullValue());
            assertThat(span.name(), equalTo("swagger3"));
            assertThat(span.tag("spec.description"), equalTo("2.1"));
        }
    }


    @Component
    @RequiredArgsConstructor
    public static class TracedLogic {

        private final TracedLogicInner inner;

        @NewSpan(name = "swagger2")
        @ApiOperation("1.1")
        public void swagger2() {
            inner.swagger2();
        }

        @NewSpan(name = "swagger3")
        @Operation(summary = "2.1")
        public void swagger3() {
            inner.swagger3();
        }

    }

    @Component
    @RequiredArgsConstructor
    public static class TracedLogicInner {

        @NewSpan(name = "inner-swagger2")
        @ApiOperation("1.2")
        public void swagger2() {
            // do nothing
        }

        @NewSpan(name = "inner-swagger3")
        @Operation(summary = "2.2")
        public void swagger3() {
            // do nothing
        }

    }

}
