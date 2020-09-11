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

public abstract class StringTracingSpecRendererTestBase<Renderer extends BaseStringTracingSpecRenderer>
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
