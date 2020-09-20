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

import static picocli.CommandLine.ExitCode.USAGE;

import javax.annotation.Nullable;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.boot.ExitCodeGenerator;

@Getter
@Internal
class ExitException extends RuntimeException implements ExitCodeGenerator {

    public static final int INCORRECT_USAGE_EXIT_CODE = USAGE;
    public static final int INCORRECT_CMD_PARAM_EXIT_CODE = USAGE + 1;


    private final int exitCode;

    public ExitException(int exitCode) {
        this.exitCode = exitCode;
    }

    public ExitException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }


    @Nullable
    public static ExitException findExitException(Throwable throwable) {
        if (throwable instanceof ExitException) {
            return (ExitException) throwable;
        }

        val cause = throwable.getCause();
        if (cause != null) {
            return findExitException(cause);
        }

        return null;
    }

}
