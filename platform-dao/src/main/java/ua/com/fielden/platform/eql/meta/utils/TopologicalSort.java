package ua.com.fielden.platform.eql.meta.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TopologicalSort {

    public static <T> List<T> sortTopologically(final Map<T, Set<T>> mapOfDependencies) {
        final List<T> sorted = new ArrayList<>();

        while (!mapOfDependencies.isEmpty()) {
            T nextSorted = null;
            // let's find the first item without dependencies and regard it as "sorted"
            for (final Entry<T, Set<T>> el : mapOfDependencies.entrySet()) {
                if (el.getValue().isEmpty()) {
                    nextSorted = el.getKey();
                    break;
                }
            }

            sorted.add(nextSorted);
            mapOfDependencies.remove(nextSorted); // removing "sorted" item from map of remaining items

            // removing "sorted" item from dependencies of remaining items 
            for (final Entry<T, Set<T>> el : mapOfDependencies.entrySet()) {
                el.getValue().remove(nextSorted);
            }
        }

        return sorted;
    }
}