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

package name.remal.tracingspec.retriever.jaeger;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

public class JaegerAllInOneContainer extends GenericContainer<JaegerAllInOneContainer> {

    public static final String IMAGE = "jaegertracing/all-in-one";
    public static final String DEFAULT_TAG = System.getProperty("docker-image-tag", "latest");

    public static final int JAEGER_QUERY_PORT = 16686;
    public static final int JAEGER_COLLECTOR_THRIFT_PORT = 14268;
    public static final int JAEGER_COLLECTOR_GRPC_PORT = 14250;
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
        withEnv("COLLECTOR_ZIPKIN_HTTP_PORT", String.valueOf(ZIPKIN_PORT));

        withExposedPorts(
            JAEGER_QUERY_PORT,
            JAEGER_COLLECTOR_THRIFT_PORT,
            JAEGER_COLLECTOR_GRPC_PORT,
            ZIPKIN_PORT
        );

        waitingFor(new WaitAllStrategy()
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

    public int getCollectorGrpcPort() {
        return getMappedPort(JAEGER_COLLECTOR_GRPC_PORT);
    }

    public int getZipkinPort() {
        return getMappedPort(ZIPKIN_PORT);
    }

}
