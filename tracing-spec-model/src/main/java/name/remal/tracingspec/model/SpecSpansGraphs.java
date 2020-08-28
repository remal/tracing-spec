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

package name.remal.tracingspec.model;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.api.BuildTimeConstants.getClassName;
import static name.remal.tracingspec.model.SpecSpanConverter.SPEC_SPAN_CONVERTER;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;
import lombok.val;
import org.apache.logging.log4j.LogManager;

public abstract class SpecSpansGraphs {

    public static SpecSpansGraph createSpecSpansGraph(Iterable<? extends SpecSpan> specSpans) {
        Map<String, SpecSpanNode> nodes = new LinkedHashMap<>();
        specSpans.forEach(specSpan -> {
            val node = SPEC_SPAN_CONVERTER.toNode(specSpan);
            nodes.put(specSpan.getSpanId(), node);
        });

        specSpans.forEach(specSpan -> {
            val spanId = specSpan.getSpanId();
            val parentSpanId = specSpan.getParentSpanId();
            if (parentSpanId == null) {
                return;
            }
            val parentNode = nodes.get(parentSpanId);
            if (parentNode != null) {
                val node = requireNonNull(nodes.get(spanId));
                node.setParent(parentNode);
            } else {
                logParentSpanNotFound(specSpan, specSpans);
                nodes.remove(spanId);
            }
        });

        val graph = new SpecSpansGraph();
        graph.setRoots(nodes.values().stream()
            .filter(node -> node.getParent() == null)
            .collect(toList())
        );

        graph.sort();

        return graph;
    }


    private static final boolean IS_LOG4J2_IN_CLASSPATH = isLog4j2InClasspath();

    private static boolean isLog4j2InClasspath() {
        try {
            Class.forName(getClassName(LogManager.class), false, SpecSpansGraphs.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void logParentSpanNotFound(SpecSpan specSpan, Iterable<? extends SpecSpan> specSpans) {
        if (IS_LOG4J2_IN_CLASSPATH) {
            logParentSpanNotFoundLog4j(specSpan, specSpans);
        }
    }

    private static void logParentSpanNotFoundLog4j(SpecSpan specSpan, Iterable<? extends SpecSpan> specSpans) {
        LogManager.getLogger(SpecSpansGraphs.class).warn(() -> format(
            "Parent span can't be found for %s in [%s]",
            specSpan,
            StreamSupport.stream(specSpans.spliterator(), false)
                .map(Objects::toString)
                .collect(joining(", "))
        ));
    }


    private SpecSpansGraphs() {
    }

}
