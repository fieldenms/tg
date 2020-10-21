package ua.com.fielden.platform.eql.stage2.elements;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;

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

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.meta.EntityTypePropInfo;
import ua.com.fielden.platform.eql.meta.LongMetadata;
import ua.com.fielden.platform.eql.meta.UnionTypePropInfo;
import ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.builders.StandAloneExpressionBuilder;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.operands.Expression1;
import ua.com.fielden.platform.eql.stage2.elements.operands.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.elements.sources.Child;
import ua.com.fielden.platform.eql.stage2.elements.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;

public class PathsToTreeTransformator {
    
    private final LongMetadata domainInfo;
    private final EntQueryGenerator gen;
    private int id = 0;
    
    public PathsToTreeTransformator(final LongMetadata domainInfo, final EntQueryGenerator gen) {
        this.domainInfo = domainInfo;
        this.gen = gen;
    }
    
    private int next() {
        id = id + 1;
        return id;
    }

    public Map<String, List<ChildGroup>> groupChildren(final Set<EntProp2> props) {
        final Map<String, List<ChildGroup>> result = new HashMap<>();
        for (final Entry<String, List<Child>> el : transform(props).entrySet()) {
            result.put(el.getKey(), convertToGroup(new TreeSet<Child>(el.getValue()), emptyList()));
        }
        return result;
    }
    
    protected final Map<String, List<Child>> transform(final Set<EntProp2> props) {
        final Map<String, List<Child>> sourceChildren = new HashMap<>();

        for (final T2<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> sourceProps : groupBySource(props).values()) {
            final T3<List<Child>, Map<String, List<Child>>, List<Child>> genRes = generateQrySourceChildren(
                    sourceProps._1, 
                    sourceProps._1, 
                    sourceProps._2, 
                    sourceProps._1,
                    emptyList());
            assert(genRes._3.size() == 0);
            sourceChildren.put(sourceProps._1.contextId(), genRes._1);
            sourceChildren.putAll(genRes._2);
        }
        
        return sourceChildren;
    }

    private static final SortedMap<String, T2<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>>>  groupBySource(final Set<EntProp2> props) {
        final SortedMap<String, T2<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>>> result = new TreeMap<>();
        for (final EntProp2 prop : props) {
            final T2<IQrySource2<?>, Map<String, List<AbstractPropInfo<?>>>> existing = result.get(prop.source.contextId());
            if (existing != null) {
                existing._2.put(prop.name, prop.getPath()); // NOTE: for rare cases where two EntProp2 are identical except isId value replacement can occur, but with identical value of path 
            } else {
                final Map<String, List<AbstractPropInfo<?>>> added = new HashMap<>();
                added.put(prop.name, prop.getPath());
                result.put(prop.source.contextId(), t2(prop.source, added));
            }
        }
        return result;
    }
    
    private static SortedMap<String, T2<AbstractPropInfo<?>, Map<String, List<AbstractPropInfo<?>>>>> groupByFirstProp(final Map<String, List<AbstractPropInfo<?>>> props) {
        final SortedMap<String, T2<AbstractPropInfo<?>, Map<String, List<AbstractPropInfo<?>>>>> result = new TreeMap<>();

        for (final Entry<String, List<AbstractPropInfo<?>>> propEntry : props.entrySet()) {
            final AbstractPropInfo<?> first = propEntry.getValue().get(0);
            T2<AbstractPropInfo<?>, Map<String, List<AbstractPropInfo<?>>>> existing = result.get(first.name);
            if (existing == null) {
                existing = t2(first, new HashMap<>());
                result.put(first.name, existing);
            }

            existing._2.put(propEntry.getKey(), propEntry.getValue());
        }

        return result;
    }

    private T3<List<Child>, Map<String, List<Child>>, List<Child>> generateQrySourceChildren(
            final IQrySource2<?> contextSource, 
            final IQrySource2<?> lastPersistentSource, 
            final Map<String, List<AbstractPropInfo<?>>> props, //long name + path
            final IQrySource2<?> explicitSource,
            final List<String> context            
            ) {
        final List<Child> result = new ArrayList<>();
        final Map<String, List<Child>> other = new HashMap<>();
        final List<Child> unionResult = new ArrayList<>();
        
        for (final T2<AbstractPropInfo<?>, Map<String, List<AbstractPropInfo<?>>>> propEntry : groupByFirstProp(props).values()) {
            final T3<List<Child>, Map<String, List<Child>>, List<Child>> genRes = generateChild(
                    contextSource, 
                    lastPersistentSource, 
                    explicitSource, 
                    propEntry._1, 
                    propEntry._2, 
                    context);
            
            result.addAll(genRes._1);
            other.putAll(genRes._2);
            unionResult.addAll(genRes._3);
        }
        return t3(result, other, unionResult);
    }
    
    private T3<List<Child>, Map<String, List<Child>>, List<Child>> generateChild(
            final IQrySource2<?> contextSource, 
            final IQrySource2<?> lastPersistentSource, 
            final IQrySource2<?> explicitSource,
            final AbstractPropInfo<?> propInfo, //first API from path
            final Map<String, List<AbstractPropInfo<?>>> subprops, // long names and their pathes that all start from 'propInfo' 
            final List<String> context
            ) {
        
        final List<Child> result = new ArrayList<>();
        final Map<String, List<Child>> other = new HashMap<>();
        final List<Child> unionResult = new ArrayList<>();
        
        final List<String> newContext = new ArrayList<>(context);
        newContext.add(propInfo.name);
        final String childContext = newContext.stream().collect(joining("_"));

        final List<Child> dependencies = new ArrayList<>();
        final IQrySource2<?> sourceForCalcPropResolution = contextSource != null ?  contextSource : lastPersistentSource;
        final CalcPropResult calcPropResult = processCalcProp(propInfo, sourceForCalcPropResolution, childContext);
        other.putAll(calcPropResult.internalSources);
        if (calcPropResult.expression != null) {
            if (contextSource != null) {
                dependencies.addAll(calcPropResult.externalSourceChildren);
                result.addAll(calcPropResult.externalSourceChildren);
            } else {
                unionResult.addAll(calcPropResult.externalSourceChildren);
            }
        }
        
        final boolean required = propInfo instanceof EntityTypePropInfo ? ((EntityTypePropInfo<?>) propInfo).required : false;

        final String sourceContextId = explicitSource.contextId() + "_" + childContext;
        final QrySource2BasedOnPersistentType source = generateQrySourceForPropInfo(propInfo, sourceContextId);
        final T2<String, Map<String, List<AbstractPropInfo<?>>>> next = getPathAndNextProps(subprops);
        final List<Child> children = new ArrayList<>();
        if (!next._2.isEmpty()) {
            final IQrySource2<?>  updateLps = source != null ? source : lastPersistentSource;
            final T3<List<Child>, Map<String, List<Child>>, List<Child>> genRes = generateQrySourceChildren(
                    source, 
                    updateLps, 
                    next._2,
                    explicitSource,
                    newContext);
            children.addAll(genRes._1);
            other.putAll(genRes._2);
            if (source == null && !genRes._3.isEmpty()) {
                if (contextSource != null) {
                        dependencies.addAll(genRes._3);
                        result.addAll(genRes._3);    
                } else {
                    unionResult.addAll(genRes._3); // will be used once nested union props are supported at Metadata and HibMapping levels.
                }
            }
            
        }
        result.add(new Child(propInfo, children, next._1, required, source, calcPropResult.expression, explicitSource, dependencies, next()));
        return t3(result, other, unionResult); 
    }    
    
    private CalcPropResult processCalcProp(final AbstractPropInfo<?> propInfo, final IQrySource2<?>  sourceForCalcPropResolution, final String childContext) {
        if (propInfo.hasExpression() && !(propInfo instanceof ComponentTypePropInfo || propInfo instanceof UnionTypePropInfo)) {
            final Expression2 expr2 = expressionToS2(sourceForCalcPropResolution, propInfo.expression, childContext);
            final Map<String, List<Child>> dependenciesResult = transform(expr2.collectProps());
            final List<Child> externalSourceChildren = dependenciesResult.remove(sourceForCalcPropResolution.contextId());
            return new CalcPropResult(expr2, dependenciesResult, externalSourceChildren == null ? emptyList() : externalSourceChildren);
        }
        
        return new CalcPropResult(null, emptyMap(), emptyList());
    }
    
    private static QrySource2BasedOnPersistentType generateQrySourceForPropInfo (final AbstractPropInfo<?> propInfo, final String sourceContextId) {
        return propInfo instanceof EntityTypePropInfo ? 
                new QrySource2BasedOnPersistentType(((EntityTypePropInfo<?>) propInfo).javaType(), ((EntityTypePropInfo<?>) propInfo).propEntityInfo, sourceContextId) 
                 : null;
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
    
    private Expression2 expressionToS2(
            final IQrySource2<?> contextSource, 
            final ExpressionModel expressionModel, 
            final String context) {
        final String sourceId = contextSource.contextId() + "_" + context;
        final PropsResolutionContext prc = new PropsResolutionContext(domainInfo, asList(asList(contextSource)), sourceId); 
        final Expression1 exp = (Expression1) (new StandAloneExpressionBuilder(gen, expressionModel)).getResult().getValue();
        return exp.transform(prc);
    }
    
    private static List<ChildGroup> convertToGroup(final SortedSet<Child> children, final List<String> parentPrefix) {
        final List<ChildGroup> result = new ArrayList<>();
        final List<String> items = new ArrayList<>();
        final Map<String, List<Child>> map = new HashMap<>();
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
                final List<Child> itemChildren = new ArrayList<>();
                final SortedMap<String, QrySource2BasedOnPersistentType> itemSources = new TreeMap<>();

                itemChildren.add(child);
                if (child.source != null) {
                    itemSources.put(child.source.contextId, child.source);    
                }
                
                map.put(child.main.name, itemChildren);
                mapSources.put(child.main.name, itemSources);
            }
            
            if (child.main instanceof UnionTypePropInfo || child.main instanceof ComponentTypePropInfo) {
                unions.add(child.main.name);
            }
        }
        
        for (final String item : items) {
            final Child first = map.get(item).iterator().next();
            final SortedSet<Child> mergedItems = new TreeSet<>();
            
            final Map<String, IQrySource2<?>> groupPaths = new HashMap<>();
            
            for (final Child c : map.get(item)) {
                mergedItems.addAll(c.getItems());
                if (c.fullPath != null) {
                    groupPaths.put(c.fullPath, c.parentSource);    
                }
            }
            
            final List<String> newParentPrefix = new ArrayList<>(parentPrefix);
            newParentPrefix.add(item);

            if (unions.contains(item)) {
                if (!mergedItems.isEmpty()) {
                    result.addAll(convertToGroup(mergedItems, newParentPrefix));    
                }
                final String itemName = parentPrefix.isEmpty() ? item : newParentPrefix.stream().collect(Collectors.joining("."));
                result.add(new ChildGroup(itemName, emptyList(), groupPaths, first.required, mapSources.get(item).isEmpty() ? first.source : mapSources.get(item).entrySet().iterator().next().getValue(), first.expr));

            } else {
                final List<ChildGroup> groupItems = mergedItems.isEmpty() ? emptyList() : convertToGroup(mergedItems, emptyList());
                final String itemName = parentPrefix.isEmpty() ? item : newParentPrefix.stream().collect(Collectors.joining("."));
                result.add(new ChildGroup(itemName, groupItems, groupPaths, first.required, mapSources.get(item).isEmpty() ? first.source : mapSources.get(item).entrySet().iterator().next().getValue(), first.expr));
            }
        }   
        
        return result;
    }
    
    private static class CalcPropResult {
        final Expression2 expression; 
        final Map<String, List<Child>> internalSources;
        final List<Child> externalSourceChildren;
        
        public CalcPropResult(final Expression2 expression, final Map<String, List<Child>> internalSources, final List<Child> externalSourceChildren) {
            this.expression = expression;
            this.internalSources = internalSources;
            this.externalSourceChildren = externalSourceChildren;
        }
    }
}