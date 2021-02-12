package name.remal.tracingspec.application;

import lombok.SneakyThrows;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.stereotype.Component;

@Internal
@Component
class DefaultSystemUtils implements SystemUtils {

    @Override
    @SneakyThrows
    public void sleep(long millis) {
        Thread.sleep(millis);
    }

}
