package utils.test.awaitility;

import static utils.test.debug.TestDebug.AWAIT_TIMEOUT;

import java.time.Duration;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

public abstract class AwaitilityUtils {

    private static final Duration AWAIT_DELAY = Duration.ofSeconds(1);
    private static final Duration AWAIT_INTERVAL = Duration.ofSeconds(1);

    public static ConditionFactory await() {
        return Awaitility.await()
            .atMost(AWAIT_TIMEOUT)
            .pollDelay(AWAIT_DELAY)
            .pollInterval(AWAIT_INTERVAL)
            .ignoreExceptions()
            .pollInSameThread();
    }


    private AwaitilityUtils() {
    }

}
