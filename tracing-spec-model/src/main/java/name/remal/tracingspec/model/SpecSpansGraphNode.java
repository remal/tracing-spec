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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.ImmutableSpecSpansGraphNode.SpecSpansGraphNodeBuilder;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = SpecSpansGraphNodeBuilder.class)
public interface SpecSpansGraphNode extends DisconnectedSpecSpan, Comparable<SpecSpansGraphNode> {

    static SpecSpansGraphNodeBuilder builder() {
        return ImmutableSpecSpansGraphNode.builder();
    }


    List<SpecSpansGraphNode> getChildren();


    @Override
    default int compareTo(SpecSpansGraphNode other) {
        val startedAt1 = getStartedAt();
        val startedAt2 = other.getStartedAt();
        if (startedAt1.isPresent() && startedAt2.isPresent()) {
            return startedAt1.get().compareTo(startedAt2.get());

        } else if (startedAt1.isPresent()) {
            return -1;

        } else if (startedAt2.isPresent()) {
            return 1;

        } else {
            return 0;
        }
    }


    @SneakyThrows
    default void visit(SpecSpansGraphVisitor visitor) {
        visit(visitor, null);
    }

    @SneakyThrows
    default void visit(SpecSpansGraphVisitor visitor, @Nullable SpecSpansGraphNode parentNode) {
        visitor.visitNode(this, parentNode);
        for (val child : getChildren()) {
            child.visit(visitor, this);
        }
        visitor.postVisitNode(this, parentNode);
    }

}
