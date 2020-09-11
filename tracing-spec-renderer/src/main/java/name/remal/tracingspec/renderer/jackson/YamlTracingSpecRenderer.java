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

package name.remal.tracingspec.renderer.jackson;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.MINIMIZE_QUOTES;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class YamlTracingSpecRenderer extends BaseJacksonTracingSpecRenderer {

    public YamlTracingSpecRenderer(YAMLMapper objectMapper) {
        super(objectMapper);
    }

    public YamlTracingSpecRenderer() {
        super(new YAMLMapper()
            .enable(MINIMIZE_QUOTES)
            .findAndRegisterModules()
        );
    }

    @Override
    public String getRendererName() {
        return "yaml";
    }

}
