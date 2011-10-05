package ua.com.fielden.platform.algorithm.search.bfs;

import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.algorithm.search.FifoQueue;
import ua.com.fielden.platform.algorithm.search.IQueue;
import ua.com.fielden.platform.algorithm.search.ISearchAlgorithm;
import ua.com.fielden.platform.algorithm.search.ITreeNode;
import ua.com.fielden.platform.algorithm.search.ITreeNodePredicate;

/**
 * Breadth-first search algorithm for a graph-like state space (i.e. handles loops gracefully).
 *
 * @author TG Team
 *
 * @param <T>
 */
public final class BreadthFirstSearch<T, N extends ITreeNode<T>> implements ISearchAlgorithm<T, N> {

    private IQueue<N> frontier = new FifoQueue<N>();
    private Set<N> explored = new HashSet<N>();

    @Override
    public N search(final N root, final ITreeNodePredicate<T, N> predicate) {
	frontier.push(root);

	while (!frontier.isEmpty()) {
	    final N node = frontier.pop();
	    if (predicate.eval(node)) {
		return node;
	    }

	    explored.add(node);
	    final Iterable<N> children = (Iterable<N>) node.children();
	    for (final N child : children) {
		if (!explored.contains(child) && !frontier.contains(child)) {
		    frontier.push(child);
		}
	    }
	}

	return null;
    }

}
