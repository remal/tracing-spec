package name.remal.tracingspec.model;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING;
import static java.util.Collections.unmodifiableList;
import static lombok.AccessLevel.NONE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

@Data
public class SpecSpansGraph {

    @JsonCreator(mode = DELEGATING)
    static SpecSpansGraph from(Iterable<? extends SpecSpanNode> roots) {
        val graph = new SpecSpansGraph();
        roots.forEach(graph::doAddRoot);
        return graph;
    }


    @Getter(NONE)
    @Setter(NONE)
    @JsonValue
    final List<SpecSpanNode> roots = new ArrayList<>();

    @UnmodifiableView
    public List<SpecSpanNode> getRoots() {
        return unmodifiableList(roots);
    }

    public void setRoots(Iterable<SpecSpanNode> children) {
        this.roots.clear();
        children.forEach(this::doAddRoot);
    }

    @Contract("_ -> this")
    public SpecSpansGraph addRoot(SpecSpanNode root) {
        removeRoot(root);
        doAddRoot(root);
        return this;
    }

    private void doAddRoot(SpecSpanNode root) {
        root.setParent(null);
        roots.add(root);
    }

    @Contract("_ -> this")
    @SuppressWarnings("java:S1698")
    public SpecSpansGraph removeRoot(SpecSpanNode root) {
        roots.removeIf(node -> node == root);
        return this;
    }


    @Contract("-> this")
    public SpecSpansGraph sort() {
        Collections.sort(roots);
        roots.forEach(SpecSpanNode::sortChildren);
        return this;
    }


    public void visit(SpecSpanNodeVisitor visitor) {
        for (val root : roots) {
            root.visit(visitor);
        }
    }

}
