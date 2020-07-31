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

import javax.annotation.Nullable;
import lombok.val;

public abstract class SpecSpansGraphUtils {

    @Nullable
    public static SpecSpansGraphNode getPreviousNodeFor(
        SpecSpansGraphNode node,
        @Nullable SpecSpansGraphNode parentNode
    ) {
        return getByIndexDeltaFor(node, parentNode, -1);
    }

    @Nullable
    public static SpecSpansGraphNode getNextNodeFor(
        SpecSpansGraphNode node,
        @Nullable SpecSpansGraphNode parentNode
    ) {
        return getByIndexDeltaFor(node, parentNode, +1);
    }

    @Nullable
    private static SpecSpansGraphNode getByIndexDeltaFor(
        SpecSpansGraphNode node,
        @Nullable SpecSpansGraphNode parentNode,
        int indexDelta
    ) {
        if (parentNode == null) {
            return null;
        }

        val siblings = parentNode.getChildren();
        val nodeIndex = siblings.indexOf(node);
        if (nodeIndex < 0) {
            throw new IllegalStateException(format(
                "Node %s is not presented in the parent node children list: %s",
                node,
                parentNode
            ));
        }

        val returnIndex = nodeIndex + indexDelta;
        if (0 <= returnIndex && returnIndex <= siblings.size() - 1) {
            return siblings.get(returnIndex);
        }

        return null;
    }


    private SpecSpansGraphUtils() {
    }

}
