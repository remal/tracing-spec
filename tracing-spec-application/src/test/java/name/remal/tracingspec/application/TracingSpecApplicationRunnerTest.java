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
