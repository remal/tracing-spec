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

package shared.testcontainers;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.testcontainers.lifecycle.Startable;

@RequiredArgsConstructor
class TestcontainersLifecycle implements DisposableBean {

    private static final Logger logger = LogManager.getLogger(TestcontainersLifecycle.class);

    private final ApplicationContext applicationContext;


    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    private void stopContainers() {
        if (isStopped.compareAndSet(false, true)) {

            val startableBeans = applicationContext.getBeansOfType(Startable.class).values();
            if (!startableBeans.isEmpty()) {
                logger.info("Stopping {} containers", startableBeans.size());
            }

            startableBeans.forEach(Startable::stop);

        }
    }


    @Override
    public void destroy() {
        stopContainers();
    }

}
