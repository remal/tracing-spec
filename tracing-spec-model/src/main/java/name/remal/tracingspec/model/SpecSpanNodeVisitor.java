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
