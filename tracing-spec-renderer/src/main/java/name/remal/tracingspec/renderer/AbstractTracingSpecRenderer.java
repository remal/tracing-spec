package name.remal.tracingspec.renderer;

import static java.nio.file.Files.createDirectories;

import java.nio.file.Path;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.jetbrains.annotations.Contract;

public abstract class AbstractTracingSpecRenderer<Result> implements TracingSpecRenderer<Result> {

    protected abstract Result renderTracingSpecImpl(SpecSpansGraph graph);

    protected abstract void writeResultToPath(Result result, Path path);


    @Override
    public final Result renderTracingSpec(SpecSpansGraph graph) {
        checkServiceNameExistence(graph);
        return renderTracingSpecImpl(graph);
    }

    private static void checkServiceNameExistence(SpecSpansGraph graph) {
        graph.visit(node -> {
            if (node.getServiceName() == null) {
                throw new IllegalStateException("Node doesn't have service name: " + node);
            }
        });
    }


    @Override
    @SneakyThrows
    public void renderTracingSpecToPath(SpecSpansGraph graph, Path path) {
        path = path.toAbsolutePath();
        val parentPath = path.getParent();
        if (parentPath != null && !parentPath.equals(path)) {
            createDirectories(parentPath);
        }

        val result = renderTracingSpec(graph);
        writeResultToPath(result, path);
    }


    @Contract("null -> true")
    protected static boolean isEmpty(@Nullable CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    @Contract("null -> false")
    protected static boolean isNotEmpty(@Nullable CharSequence charSequence) {
        return !isEmpty(charSequence);
    }


    @Contract("null, _ -> param2")
    protected static <T> T defaultValue(@Nullable T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    protected static String defaultValue(@Nullable String value) {
        return defaultValue(value, "");
    }

}
