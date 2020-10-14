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
import static name.remal.tracingspec.application.ExitException.INCORRECT_CMD_PARAM_EXIT_CODE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import name.remal.tracingspec.renderer.TracingSpecRenderer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DefaultRendersRegistry implements RendersRegistry {

    private final List<TracingSpecRenderer<?>> renderers;

    @Override
    public TracingSpecRenderer<?> getRendererByName(String rendererName) {
        return renderers.stream()
            .filter(it -> it.getRendererName().equals(rendererName))
            .findAny()
            .orElseThrow(() -> {
                val configuredRendererNames = renderers.stream()
                    .map(TracingSpecRenderer::getRendererName)
                    .collect(toList());
                return new ExitException(format(
                    "Renderer doesn't exist or isn't configured: %s. These renderers are configured: %s.",
                    rendererName,
                    String.join(", ", configuredRendererNames)
                ), INCORRECT_CMD_PARAM_EXIT_CODE);
            });
    }

}
