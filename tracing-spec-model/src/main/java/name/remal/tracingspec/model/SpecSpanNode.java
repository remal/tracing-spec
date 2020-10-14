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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public class SpecSpanNode extends SpecSpanInfo<SpecSpanNode> {

    @Nullable
    @Setter(NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    SpecSpanNode parent;

    @Getter(NONE)
    @Setter(NONE)
    @JsonProperty(index = 10001)
    final List<SpecSpanNode> children = new ArrayList<>();


    @SuppressWarnings("java:S1698")
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

    @JsonIgnore
    public final boolean isRoot() {
        return getParent() == null;
    }


    @UnmodifiableView
    public List<SpecSpanNode> getChildren() {
        return unmodifiableList(children);
    }

    public void clearChildren() {
        this.children.clear();
    }

    public void setChildren(Iterable<SpecSpanNode> children) {
        this.children.forEach(child -> child.parent = null);
        this.children.clear();
        children.forEach(child -> child.setParent(this));
    }

    @Contract("_ -> this")
    public SpecSpanNode addChild(SpecSpanNode childToAdd) {
        removeChild(childToAdd);
        childToAdd.setParent(this);
        return this;
    }

    @Contract("_, _ -> this")
    public SpecSpanNode addChildAfter(SpecSpanNode childToAdd, SpecSpanNode childToFind) {
        addChildWithIndexIncrement(childToAdd, childToFind, 1);
        return this;
    }

    @Contract("_, _ -> this")
    public SpecSpanNode addChildBefore(SpecSpanNode childToAdd, SpecSpanNode childToFind) {
        addChildWithIndexIncrement(childToAdd, childToFind, 0);
        return this;
    }

    @SuppressWarnings("java:S1698")
    public SpecSpanNode addChildWithIndexIncrement(
        SpecSpanNode childToAdd,
        SpecSpanNode childToFind,
        int indexIncrement
    ) {
        if (childToAdd == childToFind) {
            throw new IllegalArgumentException("childToAdd == childToFind");
        }

        if (getChildIndex(childToFind) < 0) {
            throw new IllegalArgumentException(this + " doesn't have child: " + childToFind);
        }

        childToAdd.setParent(null);

        // We have to calculate the index, as it can be changed after calling setParent() above
        val index = getChildIndex(childToFind);
        childToAdd.parent = this;
        children.add(index + indexIncrement, childToAdd);

        return this;
    }

    @Contract("_ -> this")
    public SpecSpanNode removeChild(SpecSpanNode childToRemove) {
        val childIndex = getChildIndex(childToRemove);
        if (childIndex >= 0) {
            childToRemove.parent = null;
            children.remove(childIndex);
        }
        return this;
    }

    @Nullable
    @JsonIgnore
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
    @JsonIgnore
    public SpecSpanNode getNext() {
        if (parent != null) {
            val childIndex = parent.getChildIndex(this);
            if (childIndex < parent.children.size() - 1) {
                return parent.children.get(childIndex + 1);
            }
        }
        return null;
    }

    @SuppressWarnings("java:S1698")
    private int getChildIndex(SpecSpanNode child) {
        for (int i = 0; i < children.size(); ++i) {
            if (child == children.get(i)) {
                return i;
            }
        }
        return -1;
    }


    @Contract("-> this")
    public SpecSpanNode sortChildren() {
        Collections.sort(children);
        children.forEach(SpecSpanNode::sortChildren);
        return this;
    }


    public void append(SpecSpanNode nodeToAppend) {
        nodeToAppend.setParent(null);
        new ArrayList<>(nodeToAppend.getChildren()).forEach(childToAppend -> childToAppend.setParent(this));

        setHidden(isHidden() && nodeToAppend.isHidden());
        if (getName() == null) {
            setName(nodeToAppend.getName());
        }
        if (getKind() == null) {
            setKind(nodeToAppend.getKind());
        }
        if (!isAsync()) {
            setAsync(nodeToAppend.isAsync());
        }
        if (getServiceName() == null) {
            setServiceName(nodeToAppend.getServiceName());
        }
        if (getRemoteServiceName() == null) {
            setRemoteServiceName(nodeToAppend.getRemoteServiceName());
        }
        if (getStartedAt() == null) {
            setStartedAt(nodeToAppend.getStartedAt());
        }
        if (getDescription() == null) {
            setDescription(nodeToAppend.getDescription());
        }
        nodeToAppend.getTags().forEach(getTags()::putIfAbsent);
        nodeToAppend.getAnnotations().forEach(this::addAnnotation);
    }

    public final void appendTo(SpecSpanNode targetNode) {
        targetNode.append(this);
    }


    @SneakyThrows
    public void visit(SpecSpanNodeVisitor visitor) {
        if (!visitor.filterNode(this)) {
            return;
        }

        visitor.visit(this);
        for (val child : new ArrayList<>(getChildren())) {
            child.visit(visitor);
        }
        visitor.postVisit(this);
    }

}
