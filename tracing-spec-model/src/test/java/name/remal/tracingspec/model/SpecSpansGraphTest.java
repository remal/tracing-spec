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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static utils.test.json.ObjectMapperProvider.readJsonResource;
import static utils.test.json.ObjectMapperProvider.writeJsonString;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SpecSpansGraphTest {

    @Nested
    class Json {

        @Test
        void empty() {
            match(
                "graph-empty.json",
                SpecSpansGraph.builder()
                    .build()
            );
        }

        @Test
        void roots_only() {
            match(
                "graph-roots-only.json",
                SpecSpansGraph.builder()
                    .addRoot(SpecSpansGraphNode.builder()
                        .spanId("1")
                        .name("name A")
                        .build()
                    )
                    .addRoot(SpecSpansGraphNode.builder()
                        .spanId("2")
                        .name("name B")
                        .build()
                    )
                    .build()
            );
        }

        @Test
        void with_children() {
            match(
                "graph-with-children.json",
                SpecSpansGraph.builder()
                    .addRoot(SpecSpansGraphNode.builder()
                        .spanId("1")
                        .name("root")
                        .addChild(SpecSpansGraphNode.builder()
                            .spanId("2")
                            .name("parent")
                            .addChild(SpecSpansGraphNode.builder()
                                .spanId("3")
                                .name("child")
                                .build()
                            )
                            .build()
                        )
                        .build()
                    )
                    .build()
            );
        }

        private void match(
            @Language("file-reference") String jsonResourceName,
            SpecSpansGraph graph
        ) {
            assertThat(
                writeJsonString(graph),
                equalTo(writeJsonString(
                    readJsonResource(jsonResourceName, Object.class)
                ))
            );

            assertThat(
                readJsonResource(jsonResourceName, SpecSpansGraph.class),
                equalTo(graph)
            );
        }

    }

}
