package utils.test.container;

import static java.lang.String.format;
import static org.testcontainers.containers.wait.strategy.WaitAllStrategy.Mode.WITH_INDIVIDUAL_TIMEOUTS_ONLY;

import java.time.Duration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

public class ZipkinContainer extends GenericContainer<ZipkinContainer>
    implements WithZipkinCollectorUrl {

    public static final String IMAGE = System.getProperty("zipkin-image", "openzipkin/zipkin-slim");
    public static final String DEFAULT_TAG = System.getProperty("zipkin-image-tag", "latest");

    public static final int ZIPKIN_PORT = 9411;

    public ZipkinContainer() {
        this(IMAGE + ":" + DEFAULT_TAG);
    }

    public ZipkinContainer(String dockerImageName) {
        super(dockerImageName);
    }

    @Override
    @SuppressWarnings("java:S109")
    protected void configure() {
        withEnv("LOGGING_LEVEL_ROOT", "INFO");

        withExposedPorts(
            ZIPKIN_PORT
        );

        waitingFor(new WaitAllStrategy(WITH_INDIVIDUAL_TIMEOUTS_ONLY)
            .withStrategy(new HttpWaitStrategy()
                .forPort(ZIPKIN_PORT)
                .forStatusCodeMatching(status -> 200 <= status && status < 500)
                .withStartupTimeout(Duration.ofMinutes(5))
            )
        );
    }

    public int getZipkinPort() {
        return getMappedPort(ZIPKIN_PORT);
    }

    public String getZipkinBaseUrl() {
        return format("http://localhost:%d/", getZipkinPort());
    }

    @Override
    public String getZipkinCollectorUrl() {
        return getZipkinBaseUrl() + "api/v2/spans";
    }

}
