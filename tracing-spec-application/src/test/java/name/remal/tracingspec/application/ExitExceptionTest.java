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
