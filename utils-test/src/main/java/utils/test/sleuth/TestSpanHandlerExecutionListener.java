package utils.test.sleuth;

import static java.lang.System.nanoTime;
import static name.remal.gradle_plugins.api.BuildTimeConstants.getClassName;
import static org.springframework.util.ClassUtils.isPresent;

import brave.handler.SpanHandler;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.val;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class TestSpanHandlerExecutionListener implements TestExecutionListener {

    private static final boolean IS_ENABLED = isPresent(
        getClassName(SpanHandler.class),
        TestSpanHandlerExecutionListener.class.getClassLoader()
    );

    private static final long LOCK_TIMEOUT_NANOS = Duration.ofMinutes(15).toNanos();
    private static final long LOCK_SLEEP_MILLIS = Duration.ofMillis(50).toMillis();

    private final AtomicBoolean isLocked = new AtomicBoolean();

    @Override
    @SuppressWarnings({"java:S2276", "java:S2925", "BusyWait"})
    public synchronized void beforeTestExecution(TestContext testContext) throws Exception {
        if (isEnabled(testContext)) {
            long startNanos = nanoTime();
            while (!isLocked.compareAndSet(false, true)) {
                long currentNanos = nanoTime();
                if (currentNanos - startNanos > LOCK_TIMEOUT_NANOS) {
                    throw new RuntimeException("Lock can't be acquired in " + LOCK_TIMEOUT_NANOS + " nanoseconds");
                }
                Thread.sleep(LOCK_SLEEP_MILLIS);
            }
        }
    }

    @Override
    public synchronized void afterTestExecution(TestContext testContext) {
        if (isEnabled(testContext)) {
            isLocked.set(false);
        }
    }


    private static boolean isEnabled(TestContext testContext) {
        if (IS_ENABLED) {
            return isTestSpanHandlerPresent(testContext);
        }
        return false;
    }

    private static boolean isTestSpanHandlerPresent(TestContext testContext) {
        val beanNames = testContext.getApplicationContext()
            .getBeanNamesForType(TestSpanHandler.class);
        return beanNames.length != 0;
    }

}
