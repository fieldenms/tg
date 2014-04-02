package ua.com.fielden.platform.algorithm.search;

/**
 * A contract for a search algorithm.
 * 
 * @author TG Team
 * 
 * @param <T>
 *            -- type of the state held by the tree node
 * @param <N>
 *            -- type of the tree node itself
 */
public interface ISearchAlgorithm<T, N extends ITreeNode<T>> {
    /**
     * Accepts the root node and the predicate to be satisfied. Return the first predicate satisfying node. If the whole tree needs to be traces in order to visit all of its nodes
     * then predicate implementation should always return <code>false</code>.
     * <p>
     * If no predicate satisfying node has been found then <code>null</code> value is returned.
     * 
     * @param root
     * @param predicate
     * @return
     */
    N search(final N root, final ITreeNodePredicate<T, N> predicate);
}
