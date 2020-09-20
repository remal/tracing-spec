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

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static picocli.CommandLine.defaultFactory;

import lombok.Data;
import org.junit.jupiter.api.Test;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

class TracingSpecApplicationRunnerTest {

    final TestCommand testCommand = new TestCommand();

    final TracingSpecApplicationRunner runner = new TracingSpecApplicationRunner(
        defaultFactory(),
        singletonList(testCommand)
    );

    @Test
    void test_command() {
        runner.run("test", "value");
        assertThat(testCommand.value, equalTo("value"));
        assertThat(testCommand.executed, equalTo(true));
    }


    @Command(name = "test")
    @Data
    private static class TestCommand implements CommandLineCommand {

        @Parameters(index = "0")
        String value;

        boolean executed;

        @Override
        public void run() {
            executed = true;
        }
    }

}
