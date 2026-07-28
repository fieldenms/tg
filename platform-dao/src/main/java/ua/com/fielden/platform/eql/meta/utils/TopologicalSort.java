package ua.com.fielden.platform.eql.meta.utils;

import java.util.*;
import java.util.Map.Entry;

import static java.util.Collections.unmodifiableList;

public class TopologicalSort {

    public static final String ERR_CYCLE_IN_GRAPH = "Topological sorting is impossible due to a cycle in the input graph.";

    private TopologicalSort() {}

    /// Return a topologically sorted list of keys from the given map.
    ///
    /// @throws TopologicalSortException  if `mapOfDependencies` contains a cycle
    ///
    public static <T> List<T> sortTopologically(final Map<T, Set<T>> mapOfDependencies) throws TopologicalSortException {
        final List<T> sorted = new ArrayList<>();
        final Map<T, Set<T>> localMapOfDependencies = makeDefensiveCopy(mapOfDependencies);

        while (!localMapOfDependencies.isEmpty()) {
            T nextSorted = null;
            // let's find the first item without dependencies and regard it as "sorted"
            for (final Entry<T, Set<T>> el : localMapOfDependencies.entrySet()) {
                if (el.getValue().isEmpty()) {
                    nextSorted = el.getKey();
                    break;
                }
            }

            if (nextSorted == null) {
                final List<T> cycle = extractCycle(localMapOfDependencies);
                throw new TopologicalSortException(ERR_CYCLE_IN_GRAPH + " Cycle: " + cycle, cycle);
            }

            sorted.add(nextSorted);
            localMapOfDependencies.remove(nextSorted); // removing "sorted" item from map of remaining items

            // removing "sorted" item from dependencies of remaining items
            for (final Entry<T, Set<T>> el : localMapOfDependencies.entrySet()) {
                el.getValue().remove(nextSorted);
            }
        }

        return unmodifiableList(sorted);
    }

    private static <T> Map<T, Set<T>> makeDefensiveCopy(final Map<T, Set<T>> mapOfDependencies) {
        final Map<T, Set<T>> result = new HashMap<>();
        for (final Entry<T, Set<T>> entry : mapOfDependencies.entrySet()) {
            result.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return result;
    }

    /// Extracts a concrete cycle from a graph on which topological sorting stalled.
    ///
    /// At a stall every remaining node still depends on another remaining node, so following
    /// dependency edges from any node must eventually revisit one -- that repeat is a cycle.
    /// Starting on a node that merely leads into a cycle is fine: the lead-in prefix is trimmed.
    /// Assumes a closed graph (every dependency is also a key).
    ///
    private static <T> List<T> extractCycle(final Map<T, Set<T>> depMap) {
        final List<T> path = new ArrayList<>();
        final Set<T> onPath = new HashSet<>();
        T node = depMap.keySet().iterator().next();
        while (onPath.add(node)) {
            path.add(node);
            node = depMap.get(node).iterator().next(); // follow any remaining dependency
        }
        path.add(node); // close the loop
        return unmodifiableList(path.subList(path.indexOf(node), path.size()));
    }

}
