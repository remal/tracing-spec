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
import static lombok.AccessLevel.PROTECTED;
import static name.remal.tracingspec.application.SerializationUtils.readYamlOrJson;
import static name.remal.tracingspec.application.SerializationUtils.writeJsonToString;
import static name.remal.tracingspec.application.SerializationUtils.writeYamlToString;

import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import name.remal.tracingspec.matcher.SpecSpansGraphMatcher;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpansGraph;
import name.remal.tracingspec.renderer.SpecSpansGraphPreparer;
import name.remal.tracingspec.retriever.SpecSpansRetriever;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Internal
@Command(name = "match", description = "Match tracing graph with pattern graph (EXPERIMENTAL)")
@Component
@RequiredArgsConstructor
@ToString
@Setter(value = PROTECTED, onMethod_ = {@VisibleForTesting})
@Getter(value = PROTECTED, onMethod_ = {@VisibleForTesting})
@SuppressWarnings("java:S3749")
@Experimental
class MatchCommand implements CommandLineCommand {

    private static final int DEFAULT_ATTEMPTS = 3;
    private static final int DEFAULT_ATTEMPTS_DELAY_MILLIS = 10_000;


    private final ObjectProvider<SpecSpansRetriever> retriever;

    private final SpecSpansGraphPreparer graphPreparer;

    private final SystemUtils systemUtils;


    @Parameters(index = "0", description = "Trace ID")
    private String traceId;

    @Parameters(index = "1", description = "Pattern graph file (YAML/JSON/JSON5)")
    private Path patternGraphFile;


    @Option(names = "--attempts", description = "Attempts number (default " + DEFAULT_ATTEMPTS + ")")
    private int attempts = DEFAULT_ATTEMPTS;

    @Option(
        names = "--attempts-delay",
        description = "Delay between attempts in milliseconds (default " + DEFAULT_ATTEMPTS_DELAY_MILLIS + ")"
    )
    private long attemptsDelayMillis = DEFAULT_ATTEMPTS_DELAY_MILLIS;


    @Override
    public void run() {
        val patternGraph = readYamlOrJson(SpecSpansGraph.class, patternGraphFile);
        val matcher = new SpecSpansGraphMatcher(patternGraph);

        int attempt = 0;
        while (true) {
            ++attempt;
            final List<SpecSpan> spans;
            final SpecSpansGraph graph;
            try {
                spans = retriever.getObject().retrieveSpecSpansForTrace(traceId);
                graph = graphPreparer.prepareSpecSpansGraph(spans);
            } catch (Throwable exception) {
                if (attempt >= attempts) {
                    throw exception;
                } else {
                    systemUtils.sleep(attemptsDelayMillis);
                    continue;
                }
            }

            if (matcher.matches(graph)) {
                return;
            }

            if (attempt >= attempts) {
                throw new ExitException(
                    format(
                        "Pattern graph%n%s%ndoesn't match to%n%s%n%nRetrieved spans:%n%s",
                        writeYamlToString(patternGraph),
                        writeYamlToString(graph),
                        writeJsonToString(spans)
                    ),
                    1
                );
            } else {
                systemUtils.sleep(attemptsDelayMillis);
            }
        }
    }

}
