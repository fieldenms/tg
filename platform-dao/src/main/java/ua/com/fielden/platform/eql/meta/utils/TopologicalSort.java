package ua.com.fielden.platform.eql.meta.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;

public class TopologicalSort {

    private TopologicalSort() {}

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
                throw new TopologicalSortException("Topological sorting is impossible due to a cycle in the input graph.");
            }

            sorted.add(nextSorted);
            localMapOfDependencies.remove(nextSorted); // removing "sorted" item from map of remaining items

            // removing "sorted" item from dependencies of remaining items
            for (final Entry<T, Set<T>> el : localMapOfDependencies.entrySet()) {
                el.getValue().remove(nextSorted);
            }
        }

        return sorted;
    }

    private static <T> Map<T, Set<T>> makeDefensiveCopy(final Map<T, Set<T>> mapOfDependencies) {
        final Map<T, Set<T>> result = new HashMap<>();
        for (final Entry<T, Set<T>> entry : mapOfDependencies.entrySet()) {
            result.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return result;
    }
}
