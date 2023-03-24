package ua.com.fielden.platform.eql.stage1.sources;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.eql.stage2.etc.Yield2;
import ua.com.fielden.platform.types.tuples.T2;

public class YieldInfoNodesGenerator {
    public static Map<String, YieldInfoNode> generate(final Collection<Yield2> yields) {

        final List<T2<List<String>, Class<?>>> yieldsInfo = new ArrayList<>();
        for (final Yield2 yield : yields) {
            yieldsInfo.add(t2(asList(yield.alias.split("\\.")), yield.javaType()));
        }

        return group(yieldsInfo);
    }

    private static Map<String, YieldInfoNode> group(final List<T2<List<String>, Class<?>>> yieldsData) {
        final Map<String, List<T2<List<String>, Class<?>>>> yieldsTreeData = new HashMap<>();
        final Map<String, Class<?>> yieldsWithoutSubprops = new HashMap<>();

        for (final T2<List<String>, Class<?>> yieldData : yieldsData) {
            final String first = yieldData._1.get(0);

            List<T2<List<String>, Class<?>>> existing = yieldsTreeData.get(first);

            if (existing == null) {
                existing = new ArrayList<>();
                yieldsTreeData.put(first, existing);
            }

            if (yieldData._1.size() == 1) {
                yieldsWithoutSubprops.put(first, yieldData._2);
            } else {
                existing.add(t2(yieldData._1.subList(1, yieldData._1.size()), yieldData._2));
            }
        }

        final Map<String, YieldInfoNode> result = new HashMap<>();

        for (final Entry<String, List<T2<List<String>, Class<?>>>> yieldTree : yieldsTreeData.entrySet()) {
            final Class<?> yieldWithoutSubprops = yieldsWithoutSubprops.get(yieldTree.getKey());
            result.put(yieldTree.getKey(), new YieldInfoNode(yieldTree.getKey(), yieldWithoutSubprops == null ? null : yieldWithoutSubprops, yieldTree.getValue().isEmpty() ? emptyMap() : group(yieldTree.getValue())));
        }

        return result;
    }
}