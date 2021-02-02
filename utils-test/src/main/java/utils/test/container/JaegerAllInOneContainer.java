package utils.test.container;

import static java.lang.String.format;
import static org.testcontainers.containers.wait.strategy.WaitAllStrategy.Mode.WITH_INDIVIDUAL_TIMEOUTS_ONLY;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

public class JaegerAllInOneContainer extends GenericContainer<JaegerAllInOneContainer>
    implements WithZipkinCollectorUrl {

    public static final String IMAGE = System.getProperty("jaeger-image", "jaegertracing/all-in-one");
    public static final String DEFAULT_TAG = System.getProperty("jaeger-image-tag", "latest");

    public static final int JAEGER_QUERY_PORT = 16686;
    public static final int JAEGER_COLLECTOR_THRIFT_PORT = 14268;
    public static final int ZIPKIN_PORT = 9411;

    public JaegerAllInOneContainer() {
        this(IMAGE + ":" + DEFAULT_TAG);
    }

    public JaegerAllInOneContainer(String dockerImageName) {
        super(dockerImageName);
    }

    @Override
    @SuppressWarnings("java:S109")
    protected void configure() {
        withEnv("LOG_LEVEL", "info");

        withEnv("COLLECTOR_ZIPKIN_HTTP_PORT", ZIPKIN_PORT + "");

        withExposedPorts(
            JAEGER_QUERY_PORT,
            JAEGER_COLLECTOR_THRIFT_PORT,
            ZIPKIN_PORT
        );

        waitingFor(new WaitAllStrategy(WITH_INDIVIDUAL_TIMEOUTS_ONLY)
            .withStrategy(new HttpWaitStrategy()
                .forPort(JAEGER_QUERY_PORT)
                .forStatusCodeMatching(status -> 200 <= status && status < 500)
            )
            .withStrategy(new HttpWaitStrategy()
                .forPort(JAEGER_COLLECTOR_THRIFT_PORT)
                .forStatusCodeMatching(status -> 200 <= status && status < 500)
            )
        );
    }

    public int getQueryPort() {
        return getMappedPort(JAEGER_QUERY_PORT);
    }

    public int getCollectorThriftPort() {
        return getMappedPort(JAEGER_COLLECTOR_THRIFT_PORT);
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
