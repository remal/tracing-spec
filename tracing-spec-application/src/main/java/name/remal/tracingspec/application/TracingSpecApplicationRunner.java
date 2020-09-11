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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.api.BuildTimeConstants.getStringProperty;
import static name.remal.tracingspec.application.ExitException.INCORRECT_CMD_PARAM_STATUS;
import static name.remal.tracingspec.application.ExitException.INCORRECT_USAGE_STATUS;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;
import name.remal.tracingspec.renderer.RenderingOptions;
import name.remal.tracingspec.renderer.TracingSpecRenderer;
import name.remal.tracingspec.retriever.SpecSpansRetriever;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ToString
public class TracingSpecApplicationRunner implements ApplicationRunner {

    private final SpecSpansRetriever retriever;

    private final List<TracingSpecRenderer<?>> renderers;

    private final RenderingOptions renderingOptions;

    @Override
    public void run(ApplicationArguments args) {
        val nonOptionArgs = args.getNonOptionArgs();
        if (nonOptionArgs.size() != 3) {
            throw incorrectUsageException();
        }

        val traceId = nonOptionArgs.get(0);

        val rendererName = nonOptionArgs.get(1);
        val renderer = renderers.stream()
            .filter(it -> it.getRendererName().equals(rendererName))
            .findAny()
            .orElse(null);
        if (renderer == null) {
            throw new ExitException(format(
                "Renderer doesn't exist or isn't configured: %s. These renderers are configured: %s.",
                rendererName,
                String.join(", ", getConfiguredRendererNames())
            ), INCORRECT_CMD_PARAM_STATUS);
        }

        final Path outputPath;
        try {
            outputPath = Paths.get(nonOptionArgs.get(2));
        } catch (Throwable exception) {
            throw new ExitException(exception.getMessage(), INCORRECT_CMD_PARAM_STATUS);
        }

        val spans = retriever.retrieveSpecSpansForTrace(traceId);
        renderer.renderTracingSpecToPath(spans, renderingOptions, outputPath);
    }

    private ExitException incorrectUsageException() {
        val sb = new StringBuilder();
        sb.append("Usage: java ")
            .append(getStringProperty("name"))
            .append(".jar <trace ID> <renderer name> <output file>");

        sb.append("\n    These renderers are configured: ")
            .append(String.join(", ", getConfiguredRendererNames()))
            .append('.');

        return new ExitException(sb.toString(), INCORRECT_USAGE_STATUS);
    }

    private List<String> getConfiguredRendererNames() {
        return renderers.stream()
            .map(TracingSpecRenderer::getRendererName)
            .collect(toList());
    }

}
