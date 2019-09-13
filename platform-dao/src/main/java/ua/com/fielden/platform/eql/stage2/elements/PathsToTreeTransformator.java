package ua.com.fielden.platform.eql.stage2.elements;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.EntityTypePropInfo;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Child;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;

public class PathsToTreeTransformator {

    static final Map<IQrySource2<?>, SortedSet<Child>> transform(final Set<EntProp2> props) {
        final Map<IQrySource2<?>, SortedSet<Child>> sourceChildren = new HashMap<>();

        for (final Entry<IQrySource2<?>, Map<String, List<AbstractPropInfo<?, ?>>>> sourceProps : groupBySource(props).entrySet()) {
            sourceChildren.put(sourceProps.getKey(), generateChildren(sourceProps.getValue(), emptyList()));
        }

        return sourceChildren;
    }

    static final Map<IQrySource2<?>, Map<String, List<AbstractPropInfo<?, ?>>>> groupBySource(final Set<EntProp2> props) {
        final Map<IQrySource2<?>, Map<String, List<AbstractPropInfo<?, ?>>>> result = new HashMap<>();

        for (final EntProp2 prop : props) {
            Map<String, List<AbstractPropInfo<?, ?>>> existing = result.get(prop.source);
            if (existing == null) {
                existing = new HashMap<>();
                result.put(prop.source, existing);
            }
            existing.put(prop.name, prop.getPath());
        }

        return result;
    }

    private static Map<AbstractPropInfo<?, ?>, Map<String, List<AbstractPropInfo<?, ?>>>> generateChildrenPlus(final Map<String, List<AbstractPropInfo<?, ?>>> props) {
        final Map<AbstractPropInfo<?, ?>, Map<String, List<AbstractPropInfo<?, ?>>>> result = new HashMap<>();

        for (final Entry<String, List<AbstractPropInfo<?, ?>>> propList : props.entrySet()) {
            final AbstractPropInfo<?, ?> first = propList.getValue().get(0);
            Map<String, List<AbstractPropInfo<?, ?>>> existing = result.get(first);
            if (existing == null) {
                existing = new HashMap<>();
                result.put(first, existing);
            }

            existing.put(propList.getKey(), propList.getValue());
        }

        return result;
    }

    private static SortedSet<Child> generateChildren(final Map<String, List<AbstractPropInfo<?, ?>>> props, final List<String> context) {
        final SortedSet<Child> result = new TreeSet<>();
        for (final Entry<AbstractPropInfo<?, ?>, Map<String, List<AbstractPropInfo<?, ?>>>> propEntry : generateChildrenPlus(props).entrySet()) {
            final Map<String, List<AbstractPropInfo<?, ?>>> propsTr = new HashMap<>();
            String path = null;
            for (final Entry<String, List<AbstractPropInfo<?, ?>>> list2 : propEntry.getValue().entrySet()) {
                if (list2.getValue().size() > 1) {
                    propsTr.put(list2.getKey(), list2.getValue().subList(1, list2.getValue().size()));
                } else {
                    path = list2.getKey();
                }
            }
            final List<String> newContext = new ArrayList<>(context);
            newContext.add(propEntry.getKey().getName());

            final boolean required = propEntry.getKey() instanceof EntityTypePropInfo ? ((EntityTypePropInfo) propEntry.getKey()).required : false;

            result.add(new Child(propEntry.getKey(), generateChildren(propsTr, newContext), path, newContext.stream().collect(joining("_")), required));
        }

        return result;
    }
}
