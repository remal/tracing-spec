package name.remal.tracingspec.matcher;

import java.util.List;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanNode;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Contract;

@Experimental
@Data
public class SpecSpansGraphMatcher {

    final SpecSpansGraph patternGraph;

    public SpecSpansGraphMatcher(SpecSpansGraph patternGraph) {
        if (patternGraph.getRoots().isEmpty()) {
            throw new IllegalArgumentException("patternGraph must have roots");
        }
        this.patternGraph = patternGraph;
    }

    @Contract("null -> false")
    public boolean matches(@Nullable SpecSpansGraph graph) {
        if (graph == null) {
            return false;
        }

        return matches(graph.getRoots(), patternGraph.getRoots());
    }

    private static boolean matches(List<SpecSpanNode> nodes, List<SpecSpanNode> patternNodes) {
        if (nodes.size() != patternNodes.size()) {
            return false;
        }

        int matchedPatternNodesCount = 0;
        for (val node : nodes) {
            boolean matches = false;
            for (val patternNode : patternNodes) {
                if (new SpecSpanInfoMatcher(patternNode).matches(node)
                    && matches(node.getChildren(), patternNode.getChildren())
                ) {
                    ++matchedPatternNodesCount;
                    matches = true;
                    break;
                }
            }
            if (!matches) {
                return false;
            }
        }

        if (matchedPatternNodesCount != patternNodes.size()) {
            return false;
        }

        return true;
    }

}
