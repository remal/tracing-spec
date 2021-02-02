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
            val exitException = findExitException(cause);
            if (exitException != null) {
                return exitException;
            }
        }

        for (val suppressed : throwable.getSuppressed()) {
            val exitException = findExitException(suppressed);
            if (exitException != null) {
                return exitException;
            }
        }

        return null;
    }

}
