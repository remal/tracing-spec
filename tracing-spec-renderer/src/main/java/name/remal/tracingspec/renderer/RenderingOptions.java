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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import name.remal.tracingspec.model.SpecSpanNode;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.jetbrains.annotations.Contract;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("tracingspec.renderer.options")
@Data
public class RenderingOptions {

    /**
     * {@link SpecSpansGraph} processors
     */
    final List<SpecSpansGraphProcessor> graphProcessors = new ArrayList<>();

    public void setGraphProcessors(Iterable<? extends SpecSpansGraphProcessor> graphProcessors) {
        this.graphProcessors.clear();
        graphProcessors.forEach(this.graphProcessors::add);
    }

    @Contract("_ -> this")
    public RenderingOptions addGraphProcessor(SpecSpansGraphProcessor graphProcessor) {
        graphProcessors.add(graphProcessor);
        return this;
    }

    @Contract("_ -> this")
    public RenderingOptions addGraphProcessors(Iterable<? extends SpecSpansGraphProcessor> graphProcessors) {
        graphProcessors.forEach(this::addGraphProcessor);
        return this;
    }

    @Contract("_ -> this")
    public RenderingOptions addGraphProcessors(SpecSpansGraphProcessor... graphProcessors) {
        return addGraphProcessors(asList(graphProcessors));
    }


    /**
     * {@link SpecSpanNode} processors
     */
    final List<SpecSpanNodeProcessor> nodeProcessors = new ArrayList<>();

    public void setNodeProcessors(Iterable<? extends SpecSpanNodeProcessor> nodeProcessors) {
        this.nodeProcessors.clear();
        nodeProcessors.forEach(this.nodeProcessors::add);
    }

    @Contract("_ -> this")
    public RenderingOptions addNodeProcessor(SpecSpanNodeProcessor nodeProcessor) {
        nodeProcessors.add(nodeProcessor);
        return this;
    }

    @Contract("_ -> this")
    public RenderingOptions addNodeProcessors(Iterable<? extends SpecSpanNodeProcessor> nodeProcessors) {
        nodeProcessors.forEach(this::addNodeProcessor);
        return this;
    }

    @Contract("_ -> this")
    public RenderingOptions addNodeProcessors(SpecSpanNodeProcessor... nodeProcessors) {
        return addNodeProcessors(asList(nodeProcessors));
    }


    /**
     * Only tags listed here are rendered
     */
    final Set<String> tagsToDisplay = new LinkedHashSet<>();

    public void setTagsToDisplay(Collection<String> tagsToDisplay) {
        this.tagsToDisplay.clear();
        this.tagsToDisplay.addAll(tagsToDisplay);
    }

    @Contract("_ -> this")
    public RenderingOptions addTagToDisplay(String tagName) {
        tagsToDisplay.add(tagName);
        return this;
    }

    @Contract("_ -> this")
    public RenderingOptions addTagsToDisplay(Iterable<String> tagNames) {
        tagNames.forEach(this::addTagToDisplay);
        return this;
    }

    @Contract("_ -> this")
    public RenderingOptions addTagsToDisplay(String... tagNames) {
        return addTagsToDisplay(asList(tagNames));
    }

}
