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

package name.remal.tracingspec.renderer.plantuml.sequence;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.renderer.BaseTracingSpecRenderer;
import name.remal.tracingspec.renderer.plantuml.BaseTracingSpecPlantumlRenderer;

public class TracingSpecPlantumlSequenceRenderer extends BaseTracingSpecPlantumlRenderer {

    @Override
    protected String renderFilteredTracingSpec(List<SpecSpan> specSpans) {
        List<String> diagram = new ArrayList<>();
        diagram.add("@startuml");
        renderFilteredTracingSpecRoot(
            specSpans,
            diagram
        );
        diagram.add("@enduml");
        return String.join("\n", diagram);
    }

    private static void renderFilteredTracingSpecRoot(
        List<SpecSpan> specSpans,
        List<String> diagram
    ) {
        val rootSpans = specSpans.stream()
            .filter(span -> !span.getParentSpanId().isPresent())
            .sorted(BaseTracingSpecRenderer::compareByStartedAt)
            .collect(toList());

        for (val span : rootSpans) {
            val serviceName = span.getServiceName();
            diagram.add(format(
                "[%s %s: %s",
                span.isSync() ? "->" : "->>",
                quoteString(serviceName),
                escapeString(span.getName())
            ));
            diagram.add(format("activate %s", quoteString(serviceName)));

            renderFilteredTracingSpecChildren(
                specSpans,
                span.getSpanId(),
                serviceName,
                diagram
            );

            if (span.isSync()) {
                diagram.add("return");
            } else {
                diagram.add(format("deactivate %s", quoteString(serviceName)));
            }
        }
    }

    private static void renderFilteredTracingSpecChildren(
        List<SpecSpan> specSpans,
        String parentSpanId,
        Optional<String> parentSpanServiceName,
        List<String> diagram
    ) {
        val siblingSpans = specSpans.stream()
            .filter(span -> parentSpanId.equals(span.getParentSpanId().orElse(null)))
            .sorted(BaseTracingSpecRenderer::compareByStartedAt)
            .collect(toList());

        SpecSpan prevSiblingSpan = null;
        for (val span : siblingSpans) {
            val serviceName = span.getServiceName();

            if (prevSiblingSpan != null) {
                if (prevSiblingSpan.getServiceName().equals(serviceName)) {
                    diagram.add("|||");
                }
            }

            diagram.add(format(
                "%s %s %s: %s",
                quoteString(parentSpanServiceName),
                span.isSync() ? "->" : "->>",
                quoteString(serviceName),
                escapeString(span.getName())
            ));
            diagram.add(format("activate %s", quoteString(serviceName)));

            renderFilteredTracingSpecChildren(
                specSpans,
                span.getSpanId(),
                serviceName,
                diagram
            );

            if (span.isSync()) {
                diagram.add("return");
            } else {
                diagram.add(format("deactivate %s", quoteString(serviceName)));
            }

            prevSiblingSpan = span;
        }
    }

}
