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
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static name.remal.gradle_plugins.api.BuildTimeConstants.getClassName;
import static name.remal.tracingspec.model.SpecSpanConverter.SPEC_SPAN_CONVERTER;
import static name.remal.tracingspec.model.SpecSpanKind.CLIENT;
import static name.remal.tracingspec.model.SpecSpanKind.CONSUMER;
import static name.remal.tracingspec.model.SpecSpanKind.PRODUCER;
import static name.remal.tracingspec.model.SpecSpanKind.SERVER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;
import lombok.Value;
import lombok.val;
import org.apache.logging.log4j.LogManager;

public abstract class SpecSpansGraphs {

    public static SpecSpansGraph createSpecSpansGraph(Iterable<? extends SpecSpan> specSpansIterable) {
        val specSpans = StreamSupport.stream(specSpansIterable.spliterator(), false)
            .map(SpecSpan.class::cast)
            .collect(toList());

        val groupedSpecSpanNodes = createGroupedSpecSpanNodes(specSpans);

        new LinkedHashMap<>(groupedSpecSpanNodes).forEach((spanId, groupNodes) -> {
            val parentSpanId = specSpans.stream()
                .filter(span -> span.getSpanId().equals(spanId))
                .map(SpecSpan::getParentSpanId)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
            if (parentSpanId == null) {
                return;
            }

            val parentGroupNodes = groupedSpecSpanNodes.get(parentSpanId);
            if (parentGroupNodes == null) {
                logParentSpanNotFound(parentSpanId, spanId, groupNodes.get(0));
                groupedSpecSpanNodes.remove(spanId);
                return;
            }

            // Attach to the latest parent node with the same serviceName:
            val firstGroupNode = groupNodes.get(0);
            boolean isAttached = false;
            for (int index = parentGroupNodes.size() - 1; 0 <= index; --index) {
                val parentGroupNode = parentGroupNodes.get(index);
                if (Objects.equals(firstGroupNode.getServiceName(), parentGroupNode.getServiceName())) {
                    firstGroupNode.setParent(parentGroupNode);
                    isAttached = true;
                    break;
                }
            }

            if (!isAttached) {
                // Just attach to the latest parent group node:
                val lastParentGroupNode = parentGroupNodes.get(parentGroupNodes.size() - 1);
                firstGroupNode.setParent(lastParentGroupNode);
            }
        });

        val graph = new SpecSpansGraph();
        graph.setRoots(groupedSpecSpanNodes.values().stream()
            .flatMap(Collection::stream)
            .filter(node -> node.getParent() == null)
            .collect(toList())
        );

        graph.sort();

        return graph;
    }

    @SuppressWarnings("java:S3776")
    private static Map<String, List<SpecSpanNode>> createGroupedSpecSpanNodes(Iterable<? extends SpecSpan> specSpans) {
        Map<String, List<SpecSpanNode>> groupedNodes = new LinkedHashMap<>();
        specSpans.forEach(span -> {
            val node = SPEC_SPAN_CONVERTER.toNode(span);
            groupedNodes.computeIfAbsent(span.getSpanId(), __ -> new ArrayList<>()).add(node);
        });

        groupedNodes.forEach((groupSpanId, group) -> {
            if (group.size() <= 1) {
                // The group consists of only one item, no need to do anything:
                return;
            }

            if (group.stream().allMatch(span -> span.getStartedAt() != null)) {
                // All spans within the group have startedAt, let's sort by it:
                group.sort(Comparator.comparing(SpecSpanNode::getStartedAt));
            }

            if (group.size() == 2) {
                for (val kindsOrder : KINDS_ORDERS) {
                    val firstSpan = group.stream()
                        .filter(span -> span.getKind() == kindsOrder.getFirst())
                        .findFirst()
                        .orElse(null);
                    val secondSpan = group.stream()
                        .filter(span -> span.getKind() == kindsOrder.getSecond())
                        .findFirst()
                        .orElse(null);
                    if (firstSpan != null && secondSpan != null) {
                        // It's a client->server like group (see KINDS_ORDERS):
                        group.set(0, firstSpan);
                        group.set(1, secondSpan);
                        break;
                    }
                }
            }

            // Make spans from the group to be linked to each other:
            for (int index = 1; index < group.size(); ++index) {
                val prevNode = group.get(index - 1);
                val node = group.get(index);
                node.setParent(prevNode);
            }
        });

        return groupedNodes;
    }

    private static final List<KindOrder> KINDS_ORDERS = asList(
        new KindOrder(CLIENT, SERVER),
        new KindOrder(PRODUCER, CONSUMER)
    );

    @Value
    private static class KindOrder {
        SpecSpanKind first;
        SpecSpanKind second;
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

    private static void logParentSpanNotFound(String parentSpanId, String spanId, SpecSpanNode node) {
        if (IS_LOG4J2_IN_CLASSPATH) {
            logParentSpanNotFoundLog4j(parentSpanId, spanId, node);
        }
    }

    private static void logParentSpanNotFoundLog4j(String parentSpanId, String spanId, SpecSpanNode node) {
        LogManager.getLogger(SpecSpansGraphs.class).warn(() -> format(
            "Parent span %s can't be found for the span with ID=%s (%s)",
            parentSpanId,
            spanId,
            node
        ));
    }


    private SpecSpansGraphs() {
    }

}
