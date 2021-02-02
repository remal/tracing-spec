package name.remal.tracingspec.application;

import static name.remal.tracingspec.application.ExitException.findExitException;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.context.ConfigurableApplicationContext;

@Internal
@RequiredArgsConstructor
class ExitExceptionReporter implements SpringBootExceptionReporter {

    @SuppressWarnings("unused")
    private final ConfigurableApplicationContext context;

    @Override
    public boolean reportException(Throwable failure) {
        return findExitException(failure) != null;
    }

}
