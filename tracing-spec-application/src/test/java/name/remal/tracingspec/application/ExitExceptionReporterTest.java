package name.remal.tracingspec.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

class ExitExceptionReporterTest {

    private final ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);

    private final ExitExceptionReporter reporter = new ExitExceptionReporter(context);

    @Test
    void exit_exception() {
        val exception = new ExitException(0);
        assertThat(reporter.reportException(exception), equalTo(true));
    }

    @Test
    void other_exception() {
        val exception = new RuntimeException();
        assertThat(reporter.reportException(exception), equalTo(false));
    }

}
