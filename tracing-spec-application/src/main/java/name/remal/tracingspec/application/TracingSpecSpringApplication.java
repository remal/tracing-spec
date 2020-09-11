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

package name.remal.tracingspec.application;

import static org.springframework.boot.Banner.Mode.OFF;
import static org.springframework.boot.WebApplicationType.NONE;

import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TracingSpecSpringApplication {

    private static final Logger logger = LogManager.getLogger(TracingSpecSpringApplication.class);

    public static void main(String... args) {
        try {
            val application = new SpringApplication(TracingSpecSpringApplication.class);
            application.setBannerMode(OFF);
            application.setWebApplicationType(NONE);

            val context = application.run(args);

            if (context.isRunning()) {
                context.stop();
            }
            if (context.isActive()) {
                context.close();
            }

        } catch (ExitException exception) {
            val message = exception.getMessage();
            if (message != null && !message.isEmpty()) {
                logger.error(message);
            }
            System.exit(exception.getStatus());
        }
    }

}
