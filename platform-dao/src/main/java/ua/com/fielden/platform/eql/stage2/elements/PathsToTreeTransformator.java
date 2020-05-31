package ua.com.fielden.platform.eql.stage2.elements;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.meta.EntityTypePropInfo;
import ua.com.fielden.platform.eql.meta.UnionTypePropInfo;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Child;
import ua.com.fielden.platform.eql.stage2.elements.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.types.tuples.T2;

public class PathsToTreeTransformator {
    
    static int id = 0;
    
    static int next() {
        id = id + 1;
        return id;
    }

    public static Map<IQrySource2<?>, List<ChildGroup>> groupChildren(final Set<EntProp2> props, final  Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        final Map<IQrySource2<?>, List<ChildGroup>> result = new HashMap<>();
        for (final Entry<IQrySource2<?>, SortedSet<Child>> el : transform(props, domainInfo).entrySet()) {
//            System.out.println("------> " + el.getKey());
            for (Child ch : el.getValue()) {
//                System.out.println(ch);
            }
            result.put(el.getKey(), convertToGroup(el.getValue(), emptyList()));
        }
        return result;
    }
    
    protected static final Map<IQrySource2<?>, SortedSet<Child>> transform(final Set<EntProp2> props, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        final Map<IQrySource2<?>, SortedSet<Child>> sourceChildren = new HashMap<>();

        for (final Entry<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> sourceProps : groupBySource(props).entrySet()) {
            final T2<SortedSet<Child>, Map<IQrySource2<?>, SortedSet<Child>>> genRes = generateChildren(sourceProps.getKey(), sourceProps.getKey().contextId(), sourceProps.getValue(), emptyList(), domainInfo, sourceProps.getKey());
            sourceChildren.put(sourceProps.getKey(), genRes._1);
            sourceChildren.putAll(genRes._2);
        }
        
        return sourceChildren;
    }

    private static final Map<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>>  groupBySource(final Set<EntProp2> props) {
        final Map<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> result = new HashMap<>();
        for (final EntProp2 prop : props) {
            final Map<String, List<AbstractPropInfo<?>>> existing = result.get(prop.source);
            if (existing != null) {
                existing.put(prop.name, prop.getPath());
            } else {
                final Map<String, List<AbstractPropInfo<?>>> added = new HashMap<>();
                added.put(prop.name, prop.getPath());
                result.put(prop.source, added);
            }
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

    private static T2<SortedSet<Child>, Map<IQrySource2<?>, SortedSet<Child>>> generateChildren(final IQrySource2<?> contextSource, final String contextId, final Map<String, List<AbstractPropInfo<?>>> props, final List<String> context, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo, final IQrySource2<?> contextParentSource) {
        final SortedSet<Child> result = new TreeSet<>();
        final Map<IQrySource2<?>, SortedSet<Child>> other = new HashMap<>();
        
        for (final Entry<AbstractPropInfo<?>, Map<String, List<AbstractPropInfo<?>>>> propEntry : groupByFirstProp(props).entrySet()) {
            final T2<List<Child>, Map<IQrySource2<?>, SortedSet<Child>>> genRes = generateChild(contextSource, propEntry.getKey(), propEntry.getValue(), context, contextId, domainInfo, contextParentSource);
            result.addAll(genRes._1);
            other.putAll(genRes._2);
        }

        return t2(result, other);
    }
    
    private static T2<List<Child>, Map<IQrySource2<?>, SortedSet<Child>>> generateChild(final IQrySource2<?> contextSource, final AbstractPropInfo<?> propInfo, final Map<String, List<AbstractPropInfo<?>>> subprops, final List<String> context, final String contextId, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo, final IQrySource2<?> contextParentSource) {
        final List<Child> result = new ArrayList<>();
        final Map<IQrySource2<?>, SortedSet<Child>> other = new HashMap<>();

        Expression2 expr2 = null;
        final Set<Child> dependencies = new HashSet<>();
        if (propInfo.hasExpression()) {
            
            expr2 = expressionToS2(contextSource, propInfo, domainInfo);
            final Map<IQrySource2<?>, SortedSet<Child>> dependenciesResult = transform(expr2.collectProps(), domainInfo);

            for (final Entry<IQrySource2<?>, SortedSet<Child>> drEntry : dependenciesResult.entrySet()) {
                if (!drEntry.getKey().equals(contextSource)) {
                    other.put(drEntry.getKey(), drEntry.getValue());
                } else {
                    dependencies.addAll(drEntry.getValue());
                    result.addAll(drEntry.getValue());
                }
            }
        }
        
        final boolean required = propInfo instanceof EntityTypePropInfo ? ((EntityTypePropInfo) propInfo).required : false;

        final List<String> newContext = new ArrayList<>(context);
        newContext.add(propInfo.name);
        final String childContext = newContext.stream().collect(joining("_"));
        final String sourceContextId = isEmpty(childContext) ? contextId : contextId + "_" + childContext;
        final QrySource2BasedOnPersistentType source = propInfo instanceof EntityTypePropInfo ? new QrySource2BasedOnPersistentType(((EntityTypePropInfo) propInfo).javaType(), ((EntityTypePropInfo) propInfo).propEntityInfo, sourceContextId) 
                 : null;
        final T2<String, Map<String, List<AbstractPropInfo<?>>>> next = getPathAndNextProps(subprops);
        final SortedSet<Child> children = new TreeSet<>();
        if (!next._2.isEmpty()) {
            final T2<SortedSet<Child>, Map<IQrySource2<?>, SortedSet<Child>>> genRes = generateChildren(source, contextId, next._2, newContext, domainInfo, contextParentSource);
            children.addAll(genRes._1);
            other.putAll(genRes._2);
        }
        
        result.add(new Child(propInfo, children, next._1, required, source, expr2, contextParentSource, dependencies, next()));

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
    
    private static Expression2 expressionToS2(final IQrySource2<?> contextSource, final AbstractPropInfo<?> propInfo, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        final PropsResolutionContext prc = new PropsResolutionContext(domainInfo, asList(asList(contextSource)), contextSource.contextId() + "_" + propInfo.name); 
        return propInfo.expression.transform(prc);
    }
    
    private static List<ChildGroup> convertToGroup(final SortedSet<Child> children, final List<String> parentPrefix) {
        final List<ChildGroup> result = new ArrayList<>();
        final List<String> items = new ArrayList<>();
        final Map<String, Set<Child>> map = new HashMap<>();
        final Map<String, SortedMap<String, QrySource2BasedOnPersistentType>> mapSources = new HashMap<>();
        final Set<String> unions = new HashSet<>();
        
        for (final Child child : children) {
            if (items.contains(child.main.name)) {
                map.get(child.main.name).add(child);
                if (child.source != null) {
                    mapSources.get(child.main.name).put(child.source.contextId, child.source);
                }
            } else {
                items.add(child.main.name);
                final Set<Child> itemChildren = new HashSet<>();
                final SortedMap<String, QrySource2BasedOnPersistentType> itemSources = new TreeMap<>();

                itemChildren.add(child);
                if (child.source != null) {
                    itemSources.put(child.source.contextId, child.source);    
                }
                
                map.put(child.main.name, itemChildren);
                mapSources.put(child.main.name, itemSources);
            }
            
            if (child.main instanceof UnionTypePropInfo) {
                unions.add(child.main.name);
            }
        }
        
        for (final String item : items) {
            final Child first = map.get(item).iterator().next();
            final SortedSet<Child> mergedItems = new TreeSet<>();
            
            final Set<T2<String, IQrySource2<?>>> groupPaths = new HashSet<>();
            
            for (final Child c : map.get(item)) {
                mergedItems.addAll(c.items);
                if (c.fullPath != null) {
                    groupPaths.add(t2(c.fullPath, c.parentSource));    
                }
            }
            
            final List<String> newParentPrefix = new ArrayList<>(parentPrefix);
            newParentPrefix.add(item);

            if (unions.contains(item)) {
                result.addAll(convertToGroup(mergedItems, newParentPrefix));
                final String itemName = parentPrefix.isEmpty() ? item : newParentPrefix.stream().collect(Collectors.joining("."));
                result.add(new ChildGroup(itemName, emptyList(), groupPaths, first.required, mapSources.get(item).isEmpty() ? first.source : mapSources.get(item).entrySet().iterator().next().getValue(), first.expr));

            } else {
                final List<ChildGroup> groupItems = convertToGroup(mergedItems, emptyList());
                final String itemName = parentPrefix.isEmpty() ? item : newParentPrefix.stream().collect(Collectors.joining("."));
                result.add(new ChildGroup(itemName, groupItems, groupPaths, first.required, mapSources.get(item).isEmpty() ? first.source : mapSources.get(item).entrySet().iterator().next().getValue(), first.expr));
            }
        }   
        
        return result;
    }
}