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

import static java.util.Collections.unmodifiableList;
import static lombok.AccessLevel.NONE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

@NotThreadSafe
@Data
public class SpecSpanNode implements SpecSpanInfo<SpecSpanNode>, Comparable<SpecSpanNode> {

    @Nullable
    String name;

    @Nullable
    SpecSpanKind kind;

    boolean async;

    @Nullable
    String serviceName;

    @Nullable
    String remoteServiceName;

    @Nullable
    Instant startedAt;

    final Map<String, String> tags = new LinkedHashMap<>();

    final Set<SpecSpanAnnotation> annotations = new LinkedHashSet<>();


    @Nullable
    @Setter(NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    SpecSpanNode parent;

    public void setParent(@Nullable SpecSpanNode parent) {
        if (this.parent == parent) {
            return;
        }

        val prevParent = this.parent;
        if (prevParent != null) {
            prevParent.removeChild(this);
        }

        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    @Getter(NONE)
    @Setter(NONE)
    final List<SpecSpanNode> children = new ArrayList<>();

    @UnmodifiableView
    public List<SpecSpanNode> getChildren() {
        return unmodifiableList(children);
    }

    public void setChildren(Iterable<SpecSpanNode> children) {
        this.children.forEach(child -> child.parent = null);
        this.children.clear();
        children.forEach(this::doAddChild);
    }

    @Contract("_ -> this")
    public SpecSpanNode addChild(SpecSpanNode child) {
        removeChild(child);
        doAddChild(child);
        return this;
    }

    private void doAddChild(SpecSpanNode child) {
        child.setParent(this);
    }

    @Contract("_ -> this")
    public SpecSpanNode removeChild(SpecSpanNode child) {
        val childIndex = getChildIndex(child);
        if (childIndex >= 0) {
            child.parent = null;
            children.remove(childIndex);
        }
        return this;
    }

    @Nullable
    public SpecSpanNode getPrevious() {
        if (parent != null) {
            val childIndex = parent.getChildIndex(this);
            if (childIndex >= 1) {
                return parent.children.get(childIndex - 1);
            }
        }
        return null;
    }

    @Nullable
    public SpecSpanNode getNext() {
        if (parent != null) {
            val childIndex = parent.getChildIndex(this);
            if (childIndex < parent.children.size() - 1) {
                return parent.children.get(childIndex + 1);
            }
        }
        return null;
    }

    private int getChildIndex(SpecSpanNode child) {
        for (int i = 0; i < children.size(); ++i) {
            if (child == children.get(i)) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public int compareTo(SpecSpanNode other) {
        val otherStartedAt = other.startedAt;
        if (startedAt == null && otherStartedAt == null) {
            return 0;
        } else if (startedAt != null && otherStartedAt != null) {
            return startedAt.compareTo(otherStartedAt);
        } else if (startedAt == null) {
            return 1;
        } else {
            return -1;
        }
    }


    @Contract("-> this")
    public SpecSpanNode sortChildren() {
        Collections.sort(children);
        children.forEach(SpecSpanNode::sortChildren);
        return this;
    }


    @SneakyThrows
    public void visit(SpecSpanNodeVisitor visitor) {
        visitor.visit(this);
        for (val child : children) {
            child.visit(visitor);
        }
        visitor.postVisit(this);
    }

}
