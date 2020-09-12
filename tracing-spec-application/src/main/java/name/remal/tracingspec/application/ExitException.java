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

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus.Internal;

@Getter
@Internal
class ExitException extends RuntimeException {

    public static final int INCORRECT_USAGE_STATUS = 2;
    public static final int INCORRECT_CMD_PARAM_STATUS = 3;


    private final int status;

    public ExitException(int status) {
        this.status = status;
    }

    public ExitException(String message, int status) {
        super(message);
        this.status = status;
    }

}
