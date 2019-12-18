package ua.com.fielden.platform.eql.stage2.elements;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.meta.EntityTypePropInfo;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.Expression1;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Child;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.types.tuples.T2;

public class PathsToTreeTransformator {

    static final Map<IQrySource2<?>, SortedSet<Child>> transform(final Set<EntProp2> props, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        final Map<IQrySource2<?>, SortedSet<Child>> sourceChildren = new HashMap<>();

        for (final Entry<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> sourceProps : groupBySource(props).entrySet()) {
            final T2<SortedSet<Child>, Map<IQrySource2<?>, SortedSet<Child>>> genRes = generateChildren(sourceProps.getKey(), sourceProps.getKey().contextId(), sourceProps.getValue(), emptyList(), domainInfo, sourceProps.getKey());
            sourceChildren.put(sourceProps.getKey(), genRes._1);
            sourceChildren.putAll(genRes._2);
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

    private static T2<SortedSet<Child>, Map<IQrySource2<?>, SortedSet<Child>>> generateChildren(final IQrySource2<?> contextSource, final int contextId, final Map<String, List<AbstractPropInfo<?>>> props, final List<String> context, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo, final IQrySource2<?> contextParentSource) {
        final SortedSet<Child> result = new TreeSet<>();
        final Map<IQrySource2<?>, SortedSet<Child>> other = new HashMap<>();
        
        for (final Entry<AbstractPropInfo<?>, Map<String, List<AbstractPropInfo<?>>>> propEntry : groupByFirstProp(props).entrySet()) {
            final T2<List<Child>, Map<IQrySource2<?>, SortedSet<Child>>> genRes = generateChild(contextSource, propEntry.getKey(), propEntry.getValue(), context, contextId, domainInfo, contextParentSource);
            result.addAll(genRes._1);
            other.putAll(genRes._2);
        }

        return t2(result, other);
    }
    
    private static T2<List<Child>, Map<IQrySource2<?>, SortedSet<Child>>> generateChild(final IQrySource2<?> contextSource, final AbstractPropInfo<?> propInfo, final Map<String, List<AbstractPropInfo<?>>> subprops, final List<String> context, final int contextId, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo, final IQrySource2<?> contextParentSource) {
        final List<Child> result = new ArrayList<>();
        final Map<IQrySource2<?>, SortedSet<Child>> other = new HashMap<>();
        final List<String> newContext = new ArrayList<>(context);
        newContext.add(propInfo.name);

        Expression2 expr2 = null;
        final Set<Child> dependencies = new HashSet<>();
        if (propInfo.expression != null) {
            final TransformationResult<Expression2> tr = expressionToS2(contextSource, propInfo.expression, domainInfo);
            expr2 = tr.item;
            final Map<IQrySource2<?>, SortedSet<Child>> dependenciesResult = transform(tr.updatedContext.getResolvedProps(), domainInfo);

            for (final Entry<IQrySource2<?>, SortedSet<Child>> drEntry : dependenciesResult.entrySet()) {
                if (!drEntry.getKey().equals(contextSource)) {
                    other.put(drEntry.getKey(), drEntry.getValue());
                }
            }
            
            dependencies.addAll(dependenciesResult.get(contextSource));
            result.addAll(dependenciesResult.get(contextSource));
        }
        
        final boolean required = propInfo instanceof EntityTypePropInfo ? ((EntityTypePropInfo) propInfo).required : false;
        final String childContext = newContext.stream().collect(joining("_"));
        final QrySource2BasedOnPersistentType source = propInfo instanceof EntityTypePropInfo ? new QrySource2BasedOnPersistentType(((EntityTypePropInfo) propInfo).javaType(), ((EntityTypePropInfo) propInfo).propEntityInfo, contextId, childContext) 
                 : null;
        final T2<String, Map<String, List<AbstractPropInfo<?>>>> next = getPathAndNextProps(subprops);
        final T2<SortedSet<Child>, Map<IQrySource2<?>, SortedSet<Child>>> genRes = generateChildren(source, contextId, next._2, newContext, domainInfo, contextParentSource);
        final SortedSet<Child> children = genRes._1;
        other.putAll(genRes._2);
        final Child child = new Child(propInfo, children, next._1, childContext, required, source, expr2, contextParentSource, dependencies); 
        System.out.println(child);
        result.add(child);
        return t2(result, other); 
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
    
    private static TransformationResult<Expression2> expressionToS2(final IQrySource2<?> contextSource, final Expression1 expression, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        final PropsResolutionContext prc = new PropsResolutionContext(domainInfo, asList(asList(contextSource)), emptySet());
        return expression.transform(prc);
    }
}