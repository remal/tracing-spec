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
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static name.remal.tracingspec.model.SpecSpansGraphs.createSpecSpansGraph;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static picocli.CommandLine.defaultFactory;
import static utils.test.text.RandomString.nextRandomString;
import static utils.test.tracing.SpanIdGenerator.nextSpanId;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpan;
import static utils.test.tracing.SpecSpanGenerator.nextSpecSpanNode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import lombok.val;
import name.remal.tracingspec.model.SpecSpansGraph;
import name.remal.tracingspec.renderer.SpecSpansGraphPreparer;
import name.remal.tracingspec.renderer.TracingSpecRenderer;
import name.remal.tracingspec.retriever.SpecSpansRetriever;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;

class TracingSpecApplicationRunnerComponentTest {

    final Collection<CommandLineCommand> commands = new ArrayList<>();

    final TracingSpecApplicationRunner runner = new TracingSpecApplicationRunner(defaultFactory(), commands);

    final SystemUtils systemUtils = mock(SystemUtils.class);

    @Nested
    class Render {

        final SpecSpansRetriever retriever = mock(SpecSpansRetriever.class);

        @SuppressWarnings("unchecked")
        final ObjectProvider<SpecSpansRetriever> retrieverProvider = mock(ObjectProvider.class);

        {
            when(retrieverProvider.getObject()).thenReturn(retriever);
        }

        final SpecSpansGraphPreparer preparer = mock(SpecSpansGraphPreparer.class);

        final TracingSpecRenderer<?> renderer = mock(TracingSpecRenderer.class);

        final RenderCommand renderCommand = new RenderCommand(
            retrieverProvider,
            preparer,
            singletonList(renderer)
        );

        {
            commands.add(renderCommand);
        }

        @Test
        void positive_scenario() {
            val traceId = nextSpanId();
            val targetPath = Paths.get(nextRandomString());
            val span = nextSpecSpan();
            val graph = createSpecSpansGraph(singletonList(span));
            when(retriever.retrieveSpecSpansForTrace(traceId)).thenReturn(singletonList(span));
            when(preparer.prepareSpecSpansGraph(singletonList(span))).thenReturn(graph);
            when(renderer.getRendererName()).thenReturn("test");

            runner.run(
                "render",
                "--spring.application.name=test",
                traceId,
                "test",
                targetPath.toString()
            );

            verify(renderer).renderTracingSpecToPath(graph, targetPath);
        }

    }


    @Nested
    class Match {

        final SpecSpansRetriever retriever = mock(SpecSpansRetriever.class);

        @SuppressWarnings("unchecked")
        final ObjectProvider<SpecSpansRetriever> retrieverProvider = mock(ObjectProvider.class);

        {
            when(retrieverProvider.getObject()).thenReturn(retriever);
        }

        final SpecSpansGraphPreparer preparer = mock(SpecSpansGraphPreparer.class);

        final MatchCommand matchCommand = new MatchCommand(retrieverProvider, preparer, systemUtils);

        {
            commands.add(matchCommand);
        }

        @Test
        void positive_scenario(@TempDir Path tempDir) throws Throwable {
            val patternGraph = new SpecSpansGraph()
                .addRoot(nextSpecSpanNode(root -> {
                    root.setName("root");
                }));
            val patternGraphFile = tempDir.resolve("pattern-graph.yaml");
            write(patternGraphFile, MatchCommand.writeYamlToString(patternGraph).getBytes(UTF_8));

            val traceId = nextSpanId();
            val span = nextSpecSpan(it -> {
                it.setName("root");
            });
            val graph = createSpecSpansGraph(singletonList(span));
            when(retriever.retrieveSpecSpansForTrace(traceId)).thenReturn(singletonList(span));
            when(preparer.prepareSpecSpansGraph(singletonList(span))).thenReturn(graph);

            val patternGraphFilePath = patternGraphFile.toString();
            assertDoesNotThrow((Executable) () -> runner.run(
                "match",
                "--spring.application.name=test",
                traceId,
                patternGraphFilePath
            ));
        }

        @Test
        void negative_scenario(@TempDir Path tempDir) throws Throwable {
            val patternGraph = new SpecSpansGraph()
                .addRoot(nextSpecSpanNode(root -> {
                    root.setName("root");
                }));
            val patternGraphFile = tempDir.resolve("pattern-graph.yaml");
            write(patternGraphFile, MatchCommand.writeYamlToString(patternGraph).getBytes(UTF_8));

            val traceId = nextSpanId();
            val graph = new SpecSpansGraph();
            when(retriever.retrieveSpecSpansForTrace(traceId)).thenReturn(emptyList());
            when(preparer.prepareSpecSpansGraph(emptyList())).thenReturn(graph);

            val patternGraphFilePath = patternGraphFile.toString();
            int maxAttempts = ThreadLocalRandom.current().nextInt(10, 20);
            long delayMillis = ThreadLocalRandom.current().nextInt(1000, 2000);
            val exception = assertThrows(
                ExitException.class,
                () -> runner.run(
                    "match",
                    "--spring.application.name=test",
                    "--attempts=" + maxAttempts,
                    "--attempts-delay=" + delayMillis,
                    traceId,
                    patternGraphFilePath
                )
            );

            verify(systemUtils, times(maxAttempts - 1)).sleep(delayMillis);

            assertThat(exception.getExitCode(), equalTo(1));
            assertThat(exception.getMessage(), equalTo(
                format(
                    "Pattern graph%n%s%ndoesn't match to%n%s%n%nRetrieved spans:%n%s",
                    MatchCommand.writeYamlToString(patternGraph),
                    MatchCommand.writeYamlToString(graph),
                    MatchCommand.writeJsonToString(emptyList())
                )
            ));
        }

    }

}
