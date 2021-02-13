package utils.test.debug;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;

import java.time.Duration;

public interface TestDebug {

    boolean IS_IN_DEBUG = getRuntimeMXBean().getInputArguments().toString().contains("jdwp");

    Duration AWAIT_TIMEOUT = IS_IN_DEBUG ? Duration.ofHours(1) : Duration.ofMinutes(1);

}
