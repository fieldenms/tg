package ua.com.fielden.platform.algorithm.search;

/**
 * A contract for a predicated defined for tree nodes.
 *
 * @author TG Team
 *
 * @param <T> -- type of the state held by the tree node
 * @param <N> -- type of the tree node itself
 */
public interface ITreeNodePredicate<T, N extends ITreeNode<T>> {
    /**
     * Should return <code>true</code> if the node satisfies the predicate. Otherwise, false.
     *
     * @param node
     * @return
     */
    boolean eval(final N node);
}
