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

@FunctionalInterface
public interface SpecSpanNodeVisitor {

    /**
     * @return {@code true} - visit the node, {@code false} - don't visit the node and its children
     */
    default boolean filterNode(SpecSpanNode node) throws Throwable {
        return true;
    }

    void visit(SpecSpanNode node) throws Throwable;

    /**
     * Executed when the node and all its children is visited
     */
    default void postVisit(SpecSpanNode node) throws Throwable {
    }

}
