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

package name.remal.tracingspec.retriever.zipkin;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

public class ZipkinContainer extends GenericContainer<ZipkinContainer> {

    public static final String IMAGE = "openzipkin/zipkin";
    public static final String DEFAULT_TAG = System.getProperty("docker-image-tag", "latest");

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
        withEnv("LOGGING_LEVEL_ROOT", "TRACE");

        withExposedPorts(
            ZIPKIN_PORT
        );

        waitingFor(new WaitAllStrategy()
            .withStrategy(new HttpWaitStrategy()
                .forPort(ZIPKIN_PORT)
                .forStatusCodeMatching(status -> 200 <= status && status < 500)
            )
        );
    }

    public int getZipkinPort() {
        return getMappedPort(ZIPKIN_PORT);
    }

}
