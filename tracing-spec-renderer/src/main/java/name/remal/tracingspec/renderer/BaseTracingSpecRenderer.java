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

import static name.remal.tracingspec.model.SpecSpansGraphs.createSpecSpansGraph;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpanNode;
import name.remal.tracingspec.model.SpecSpanNodeVisitor;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.jetbrains.annotations.Contract;

@NotThreadSafe
public abstract class BaseTracingSpecRenderer<Result> implements TracingSpecRenderer<Result> {

    protected abstract Result renderSpecSpansGraph(SpecSpansGraph graph);


    private final List<SpecSpanNodeProcessor> nodeProcessors = new ArrayList<>();

    @Override
    public void addNodeProcessor(SpecSpanNodeProcessor nodeProcessor) {
        this.nodeProcessors.add(nodeProcessor);
    }

    private final Set<String> tagsToDisplay = new LinkedHashSet<>();

    @Override
    public void addTagToDisplay(String tagName) {
        tagsToDisplay.add(tagName);
    }

    protected boolean isDisplayableTag(String tagName) {
        return tagsToDisplay.contains(tagName);
    }


    @Override
    public final Result renderTracingSpec(List<SpecSpan> specSpans) {
        if (specSpans.isEmpty()) {
            throw new IllegalArgumentException("specSpans must not be empty");
        }

        val specSpansGraph = createSpecSpansGraph(specSpans);
        processNodes(specSpansGraph);
        checkServiceNameExistence(specSpansGraph);
        return renderSpecSpansGraph(specSpansGraph);
    }

    private void processNodes(SpecSpansGraph graph) {
        nodeProcessors.stream()
            .sorted()
            .forEach(processor ->
                graph.visit(new SpecSpanNodeVisitor() {
                    @Override
                    public void visit(SpecSpanNode node) throws Throwable {
                        processor.processNode(node);
                    }
                })
            );
    }

    private static void checkServiceNameExistence(SpecSpansGraph graph) {
        val haveSpansWithServiceName = new AtomicBoolean(false);
        val haveSpansWithoutServiceName = new AtomicBoolean(false);
        graph.visit(new SpecSpanNodeVisitor() {
            @Override
            public void visit(SpecSpanNode node) {
                if (node.getServiceName() != null) {
                    haveSpansWithServiceName.set(true);
                } else {
                    haveSpansWithoutServiceName.set(true);
                }
            }
        });

        if (haveSpansWithServiceName.get() && haveSpansWithoutServiceName.get()) {
            throw new IllegalStateException("Some specSpans do have serviceName and some - not");
        }
    }


    @Contract("null -> true")
    protected static boolean isEmpty(@Nullable CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    @Contract("null -> false")
    protected static boolean isNotEmpty(@Nullable CharSequence charSequence) {
        return !isEmpty(charSequence);
    }

}
