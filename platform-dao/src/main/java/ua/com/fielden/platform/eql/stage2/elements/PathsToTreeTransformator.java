package ua.com.fielden.platform.eql.stage2.elements;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.types.tuples.T2.t2;

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
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.types.tuples.T2;

public class PathsToTreeTransformator {

    static final Map<IQrySource2<?>, SortedSet<Child>> transform(final Set<EntProp2> props) {
        final Map<IQrySource2<?>, SortedSet<Child>> sourceChildren = new HashMap<>();

        for (final Entry<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> sourceProps : groupBySource(props).entrySet()) {
            sourceChildren.put(sourceProps.getKey(), generateChildren(sourceProps.getKey().contextId(), sourceProps.getValue(), emptyList()));
        }

        return sourceChildren;
    }

    static final Map<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> groupBySource(final Set<EntProp2> props) {
        final Map<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> result = new HashMap<>();

        for (final EntProp2 prop : props) {
            Map<String, List<AbstractPropInfo<?>>> existing = result.get(prop.source);
            if (existing == null) {
                existing = new HashMap<>();
                result.put(prop.source, existing);
            }
            existing.put(prop.name, prop.getPath());
        }

        return result;
    }

    private static Map<AbstractPropInfo<?>, Map<String, List<AbstractPropInfo<?>>>> groupByFirstProp(final Map<String, List<AbstractPropInfo<?>>> props) {
        final Map<AbstractPropInfo<?>, Map<String, List<AbstractPropInfo<?>>>> result = new HashMap<>();

        for (final Entry<String, List<AbstractPropInfo<?>>> propEntry : props.entrySet()) {
            final AbstractPropInfo<?> first = propEntry.getValue().get(0);
            Map<String, List<AbstractPropInfo<?>>> existing = result.get(first);
            if (existing == null) {
                existing = new HashMap<>();
                result.put(first, existing);
            }

            existing.put(propEntry.getKey(), propEntry.getValue());
        }

        return result;
    }

    private static SortedSet<Child> generateChildren(final int contextId, final Map<String, List<AbstractPropInfo<?>>> props, final List<String> context) {
        final SortedSet<Child> result = new TreeSet<>();
        
        for (final Entry<AbstractPropInfo<?>, Map<String, List<AbstractPropInfo<?>>>> propEntry : groupByFirstProp(props).entrySet()) {
            result.add(generateChild(propEntry.getKey(), propEntry.getValue(), context, contextId));
        }

        return result;
    }
    
    private static Child generateChild(final AbstractPropInfo<?> propInfo, final Map<String, List<AbstractPropInfo<?>>> subprops, final List<String> context, final int contextId) {
        final List<String> newContext = new ArrayList<>(context);
        newContext.add(propInfo.name);

        final boolean required = propInfo instanceof EntityTypePropInfo ? ((EntityTypePropInfo) propInfo).required : false;
        final String childContext = newContext.stream().collect(joining("_"));
        final QrySource2BasedOnPersistentType source = propInfo instanceof EntityTypePropInfo ? new QrySource2BasedOnPersistentType(((EntityTypePropInfo) propInfo).javaType(), ((EntityTypePropInfo) propInfo).propEntityInfo, contextId, childContext) 
                 : null;
        final T2<String, Map<String, List<AbstractPropInfo<?>>>> next = getPathAndNextProps(subprops);
        final SortedSet<Child> children = generateChildren(contextId, next._2, newContext);
        return new Child(propInfo, children, next._1, childContext, required, source);
    }    
    
    private static T2<String, Map<String, List<AbstractPropInfo<?>>>> getPathAndNextProps(final Map<String, List<AbstractPropInfo<?>>> subprops) {
        final Map<String, List<AbstractPropInfo<?>>> nextProps = new HashMap<>();
        String path = null;

        for (final Entry<String, List<AbstractPropInfo<?>>> subpropEntry : subprops.entrySet()) {
            if (subpropEntry.getValue().size() > 1) {
                nextProps.put(subpropEntry.getKey(), subpropEntry.getValue().subList(1, subpropEntry.getValue().size()));
            } else {
                path = subpropEntry.getKey();
            }
        }
        return t2(path, nextProps);
    }
}