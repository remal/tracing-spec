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

package utils.test.debug;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;

import java.time.Duration;

public interface TestDebug {

    boolean IS_IN_DEBUG = getRuntimeMXBean().getInputArguments().toString().contains("jdwp");

    Duration AWAIT_TIMEOUT = IS_IN_DEBUG ? Duration.ofHours(1) : Duration.ofMinutes(1);

}
