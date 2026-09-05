package ua.com.fielden.platform.eql.meta.utils;

import java.util.*;
import java.util.Map.Entry;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toCollection;

public class TopologicalSort {

    public static final String
            ERR_CYCLE_IN_GRAPH = "Topological sorting is impossible due to a cycle in the graph: %s.",
            ERR_DANGLING_EDGES = "Topological sorting is impossible: the graph has dangling edges to %s.";

    private TopologicalSort() {}

    /// Returns a topologically sorted list of the graph's vertices, each preceded by the vertices it depends on.
    ///
    /// The argument is an adjacency map: keys are the vertices, and each value is the set of vertices that its
    /// vertex depends on (its out-neighbours).
    /// The vertex set is therefore the key set, so a well-formed digraph requires every dependency to be a key
    /// as well (`E ⊆ V × V`).
    /// An edge to a vertex that has no entry of its own is a *dangling edge*.
    ///
    /// @throws TopologicalSortException  if the graph contains a cycle, or has dangling edges.
    ///     Only in the former case is [TopologicalSortException#cycle()] populated.
    ///
    public static <T> List<T> sortTopologically(final Map<T, Set<T>> adjacency) throws TopologicalSortException {
        final List<T> sorted = new ArrayList<>();
        final Map<T, Set<T>> remaining = makeDefensiveCopy(adjacency);

        while (!remaining.isEmpty()) {
            T nextSorted = null;
            // let's find the first vertex without dependencies and regard it as "sorted"
            for (final Entry<T, Set<T>> el : remaining.entrySet()) {
                if (el.getValue().isEmpty()) {
                    nextSorted = el.getKey();
                    break;
                }
            }

            if (nextSorted == null) {
                // A stall has two possible causes, and only one of them is a cycle.
                // A dangling edge can never be discharged, because only vertices that are keys are ever removed
                // from the remaining dependency sets -- so it stalls the sort even in an acyclic graph.
                final Set<T> dangling = danglingVertices(remaining);
                if (!dangling.isEmpty()) {
                    throw new TopologicalSortException(ERR_DANGLING_EDGES.formatted(dangling));
                }
                final List<T> cycle = extractCycle(remaining);
                throw new TopologicalSortException(ERR_CYCLE_IN_GRAPH.formatted(cycle), cycle);
            }

            sorted.add(nextSorted);
            remaining.remove(nextSorted); // removing the "sorted" vertex from the remaining subgraph

            // removing the "sorted" vertex from the dependencies of the remaining vertices
            for (final Entry<T, Set<T>> el : remaining.entrySet()) {
                el.getValue().remove(nextSorted);
            }
        }

        return unmodifiableList(sorted);
    }

    private static <T> Map<T, Set<T>> makeDefensiveCopy(final Map<T, Set<T>> adjacency) {
        final Map<T, Set<T>> result = new HashMap<>();
        for (final Entry<T, Set<T>> entry : adjacency.entrySet()) {
            result.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return result;
    }

    /// Extracts a concrete cycle from a digraph on which topological sorting stalled.
    ///
    /// Precondition: the graph is well-formed -- every vertex named as a dependency is itself a key
    /// (`E ⊆ V × V`), so there are no dangling edges.
    /// The sort preserves this, because a sorted vertex is removed from the key set and from every
    /// remaining dependency set together.
    ///
    /// At a stall no remaining vertex has out-degree 0, so in a finite digraph any maximal walk must
    /// revisit a vertex, and the first repeat closes a cycle.
    /// Vertices traversed before that repeat merely lead into the cycle, and are trimmed.
    ///
    private static <T> List<T> extractCycle(final Map<T, Set<T>> adjacency) {
        final List<T> path = new ArrayList<>();
        final Set<T> onPath = new HashSet<>();
        T vertex = adjacency.keySet().iterator().next();
        while (onPath.add(vertex)) {
            path.add(vertex);
            vertex = adjacency.get(vertex).iterator().next(); // follow any remaining edge
        }
        path.add(vertex); // close the loop
        return unmodifiableList(path.subList(path.indexOf(vertex), path.size()));
    }

    /// The heads of dangling edges: vertices named as a dependency that are not themselves vertices of the graph.
    ///
    private static <T> Set<T> danglingVertices(final Map<T, Set<T>> adjacency) {
        return adjacency.values().stream()
                .flatMap(Set::stream)
                .filter(vertex -> !adjacency.containsKey(vertex))
                .collect(toCollection(LinkedHashSet::new));
    }

}
