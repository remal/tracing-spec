package name.remal.tracingspec.renderer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static utils.test.resource.Resources.readTextResource;

import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.val;
import org.intellij.lang.annotations.Language;

public abstract class AbstractStringTracingSpecRendererTestBase<Renderer extends AbstractStringTracingSpecRenderer>
    extends TracingSpecRendererTestBase<String, Renderer> {

    protected abstract String getExpectedResourceExtension();

    private static final Pattern NEW_LINES = Pattern.compile("[\n\r]+");
    private static final Pattern SPACES_AT_THE_END = Pattern.compile("\\s+$");

    @Override
    protected String normalizeResult(String result) {
        val lines = NEW_LINES.split(result);
        return Stream.of(lines)
            .map(line -> SPACES_AT_THE_END.matcher(line).replaceFirst(""))
            .filter(line -> !line.isEmpty())
            .collect(joining("\n"));
    }

    @Override
    protected final String getExpectedResourceName(String resourceName) {
        val classResourcePrefix = getClass().getName()
            .substring(0, getClass().getName().lastIndexOf('.') + 1)
            .replace('.', '/');

        val resourceNamePrefix = resourceName.substring(0, resourceName.lastIndexOf('/') + 1);
        assertThat(classResourcePrefix, startsWith(resourceNamePrefix));

        val relativePrefix = classResourcePrefix.substring(resourceNamePrefix.length());

        final String expectedResourceFileName;
        {
            val resourceFileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
            val resourceBaseFileName = resourceFileName.substring(0, resourceFileName.lastIndexOf('.'));
            expectedResourceFileName = resourceBaseFileName + '.' + getExpectedResourceExtension();
        }

        return resourceNamePrefix + relativePrefix + expectedResourceFileName;
    }

    @Override
    protected final String readExpectedResult(@Language("file-reference") String expectedResourceName) {
        return readTextResource(expectedResourceName);
    }

    @Override
    protected final String readExpectedResult(Path path) throws Throwable {
        val bytes = readAllBytes(path);
        return new String(bytes, UTF_8);
    }

}
