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

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.ImmutableSpecSpansGraph.SpecSpansGraphBuilder;
import org.immutables.value.Value;

@Value.Immutable
public interface SpecSpansGraph {

    static SpecSpansGraphBuilder builder() {
        return ImmutableSpecSpansGraph.builder();
    }

    @JsonCreator(mode = DELEGATING)
    static SpecSpansGraph from(Iterable<? extends SpecSpansGraphNode> roots) {
        return ImmutableSpecSpansGraph.builder().addAllRoots(roots).build();
    }


    @JsonValue
    List<SpecSpansGraphNode> getRoots();


    @SneakyThrows
    default void visit(SpecSpansGraphVisitor visitor) {
        for (val root : getRoots()) {
            root.visit(visitor, null);
        }
    }

}
