package name.remal.tracingspec.renderer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;

import java.nio.file.Path;
import lombok.SneakyThrows;

public abstract class AbstractStringTracingSpecRenderer extends AbstractTracingSpecRenderer<String> {

    @Override
    @SneakyThrows
    protected void writeResultToPath(String result, Path path) {
        write(path, result.getBytes(UTF_8));
    }

}
