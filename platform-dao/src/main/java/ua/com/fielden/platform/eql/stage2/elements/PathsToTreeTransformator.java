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
import java.util.TreeMap;
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
    
    public PathsToTreeTransformator(final LongMetadata domainInfo, final EntQueryGenerator gen) {
        this.domainInfo = domainInfo;
        this.gen = gen;
    }
    
    public Map<String, List<ChildGroup>> groupChildren(final Set<EntProp2> props) {
        final Map<String, List<ChildGroup>> result = new HashMap<>();
        for (final Entry<String, List<Child>> el : transform(props).entrySet()) {
            result.put(el.getKey(), convertToGroup(el.getValue(), emptyList()));
        }
        return result;
    }
    
    protected final Map<String, List<Child>> transform(final Set<EntProp2> props) {
        final Map<String, List<Child>> sourceChildren = new HashMap<>();

        for (final SourceAndItsProps sourceProps : groupBySource(props).values()) {
            final T2<List<Child>, Map<String, List<Child>>> genRes = generateExplicitQrySourceChildren(sourceProps);
            sourceChildren.put(sourceProps.source.contextId(), genRes._1);
            sourceChildren.putAll(genRes._2);
        }
        
        return sourceChildren;
    }

    private static final Map<String, SourceAndItsProps>  groupBySource(final Set<EntProp2> props) {
        final Map<String, SourceAndItsProps> result = new HashMap<>();
        for (final EntProp2 prop : props) {
            final SourceAndItsProps existing = result.get(prop.source.contextId());
            if (existing != null) {
                existing.propPathesByFullNames.put(prop.name, prop.getPath()); // NOTE: for rare cases where two EntProp2 are identical except isId value, replacement can occur, but with identical value of path 
            } else {
                final Map<String, List<AbstractPropInfo<?>>> added = new HashMap<>();
                added.put(prop.name, prop.getPath());
                result.put(prop.source.contextId(), new SourceAndItsProps(prop.source, added));
            }
        }
        return result;
    }

    private static SortedMap<String, FirstPropInfoAndItsPathes> groupByFirstProp(final Map<String, List<AbstractPropInfo<?>>> props) {
        final SortedMap<String, FirstPropInfoAndItsPathes> result = new TreeMap<>();

        for (final Entry<String, List<AbstractPropInfo<?>>> propEntry : props.entrySet()) {
            final AbstractPropInfo<?> first = propEntry.getValue().get(0);
            FirstPropInfoAndItsPathes existing = result.get(first.name);
            if (existing == null) {
                existing = new FirstPropInfoAndItsPathes(first, new HashMap<>());
                result.put(first.name, existing);
            }

            existing.itsPropPathesByFullNames.put(propEntry.getKey(), propEntry.getValue());
        }

        return result;
    }

    private T2<List<Child>, Map<String, List<Child>>> generateExplicitQrySourceChildren(final SourceAndItsProps sourceProps) {
        final T3<List<Child>, Map<String, List<Child>>, List<Child>> result = generateQrySourceChildren( //
                false, //
                sourceProps.source, //
                sourceProps.source, //
                sourceProps.propPathesByFullNames, //
                emptyList());

        assert(result._3.isEmpty());
        return t2(result._1, result._2);
    }
    
    private T3<List<Child>, Map<String, List<Child>>, List<Child>> generateQrySourceChildren( //
            final boolean isHeader, //
            final IQrySource2<?> sourceForCalcPropResolution, // 
            final IQrySource2<?> explicitSource, //
            final Map<String, List<AbstractPropInfo<?>>> props, //long name + path
            final List<String> context //           
            ) {
        final List<Child> result = new ArrayList<>();
        final Map<String, List<Child>> other = new HashMap<>();
        final List<Child> unionResult = new ArrayList<>();
        
        for (final FirstPropInfoAndItsPathes propEntry : groupByFirstProp(props).values()) {
            final List<String> childContext = new ArrayList<>(context);
            childContext.add(propEntry.firstPropInfo.name);
            
            final T3<List<Child>, Map<String, List<Child>>, List<Child>> genRes = generateChild( //
                    isHeader, //
                    sourceForCalcPropResolution, // 
                    explicitSource, //
                    propEntry, //
                    childContext);
            
            result.addAll(genRes._1);
            other.putAll(genRes._2);
            unionResult.addAll(genRes._3);
        }
        return t3(result, other, unionResult);
    }
    
    private T3<List<Child>, Map<String, List<Child>>, List<Child>> generateChild( //
            final boolean isHeader, //
            final IQrySource2<?> sourceForCalcPropResolution, //  
            final IQrySource2<?> explicitSource, //
            final FirstPropInfoAndItsPathes firstPropInfoAndItsPathes,
            final List<String> childContext //
            ) {
        
        final List<Child> result = new ArrayList<>();
        final Map<String, List<Child>> other = new HashMap<>();
        final List<Child> unionResult = new ArrayList<>();
        
        final String childContextString = childContext.stream().collect(joining("_"));

        final List<Child> dependencies = new ArrayList<>();
        final CalcPropResult calcPropResult = processCalcProp(firstPropInfoAndItsPathes.firstPropInfo, sourceForCalcPropResolution, childContextString);
        other.putAll(calcPropResult.internalSources);
        if (calcPropResult.expression != null) {
            if (isHeader) {
                unionResult.addAll(calcPropResult.externalSourceChildren);
            } else {
                dependencies.addAll(calcPropResult.externalSourceChildren);
                result.addAll(calcPropResult.externalSourceChildren);
            }
        }
        

        final T2<String, Map<String, List<AbstractPropInfo<?>>>> next = getPathAndNextProps(firstPropInfoAndItsPathes.itsPropPathesByFullNames);
        
        if (next._2.isEmpty()) {
            result.add(new Child(firstPropInfoAndItsPathes.firstPropInfo, emptyList(), next._1, false, null, calcPropResult.expression, next._1 == null ? null : explicitSource.contextId(), dependencies));    
        } else {
            final boolean required = firstPropInfoAndItsPathes.firstPropInfo instanceof EntityTypePropInfo ? ((EntityTypePropInfo<?>) firstPropInfoAndItsPathes.firstPropInfo).required : false;

            final QrySource2BasedOnPersistentType source = generateQrySourceForPropInfo(firstPropInfoAndItsPathes.firstPropInfo, explicitSource.contextId(), childContextString);
            
            final IQrySource2<?> updatedSourceForCalcPropResolution = source != null ? source : sourceForCalcPropResolution;
            
            final T3<List<Child>, Map<String, List<Child>>, List<Child>> genRes = generateQrySourceChildren(
                    source == null, 
                    updatedSourceForCalcPropResolution, 
                    explicitSource,
                    next._2,
                    childContext);
            
            other.putAll(genRes._2);

            if (source == null && !genRes._3.isEmpty()) {
                if (isHeader) {
                    unionResult.addAll(genRes._3); // will be used once nested union props are supported at Metadata and HibMapping levels.
                } else {
                    dependencies.addAll(genRes._3);
                    result.addAll(genRes._3);    
                }
            }

            result.add(new Child(firstPropInfoAndItsPathes.firstPropInfo, genRes._1, next._1, required, source, calcPropResult.expression, next._1 == null ? null : explicitSource.contextId(), dependencies));
        }
        
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
    
    private static QrySource2BasedOnPersistentType generateQrySourceForPropInfo (final AbstractPropInfo<?> propInfo, final String explicitSourceContextId, final String childContextString) {
        if (propInfo instanceof EntityTypePropInfo) {
            final String sourceContextId = explicitSourceContextId + "_" + childContextString;
            return new QrySource2BasedOnPersistentType(((EntityTypePropInfo<?>) propInfo).javaType(), ((EntityTypePropInfo<?>) propInfo).propEntityInfo, sourceContextId);
        } else {
            return null;
        }
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
    
    private Expression2 expressionToS2( //
            final IQrySource2<?> contextSource, //
            final ExpressionModel expressionModel, // 
            final String context) {
        final String sourceId = contextSource.contextId() + "_" + context;
        final PropsResolutionContext prc = new PropsResolutionContext(domainInfo, asList(asList(contextSource)), sourceId);
        final Expression1 exp = (Expression1) (new StandAloneExpressionBuilder(gen, expressionModel)).getResult().getValue();
        return exp.transform(prc);
    }
    
    private static List<ChildGroup> convertToGroup(final List<Child> children, final List<String> parentPrefix) {
        final List<ChildGroup> result = new ArrayList<>();
        final List<String> items = new ArrayList<>();
        final Map<String, List<Child>> map = new HashMap<>();
        final Map<String, SortedMap<String, QrySource2BasedOnPersistentType>> mapSources = new HashMap<>();
        final Set<String> unions = new HashSet<>();
        
        // collecting data from children of the same name
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
        
        // ordering children for correct dependencies resolution
        final Map<String, Set<String>> mapOfDependencies = new HashMap<>();
        
        for (final String item : items) {
            mapOfDependencies.put(item, map.get(item).get(0).dependencies);
        }

        final List<String> orderedItems = new ArrayList<>();
        
        while (!mapOfDependencies.isEmpty()) {
            String found = null;
            for (Entry<String, Set<String>> el : mapOfDependencies.entrySet()) {
                if (el.getValue().isEmpty()) {
                    found = el.getKey();
                    break;
                }
            }

            orderedItems.add(found);
            mapOfDependencies.remove(found);
            
            for (Entry<String, Set<String>> el : mapOfDependencies.entrySet()) {
                el.getValue().remove(found);
            }
        }

        // transforming into ChildGroup list
        for (final String item : orderedItems) {
            final Child first = map.get(item).iterator().next();
            final List<Child> mergedItems = new ArrayList<>();
            
            final Map<String, String> groupPaths = new HashMap<>();
            
            for (final Child c : map.get(item)) {
                mergedItems.addAll(c.getItems());
                if (c.fullPath != null) {
                    groupPaths.put(c.fullPath, c.explicitSourceId);    
                }
            }
            
            final List<String> newParentPrefix = new ArrayList<>(parentPrefix);
            newParentPrefix.add(item);

            final String itemName = parentPrefix.isEmpty() ? item : newParentPrefix.stream().collect(Collectors.joining("."));

            if (unions.contains(item)) {
                if (!mergedItems.isEmpty()) {
                    result.addAll(convertToGroup(mergedItems, newParentPrefix));    
                }
                result.add(new ChildGroup(itemName, emptyList(), groupPaths, first.required, mapSources.get(item).isEmpty() ? first.source : mapSources.get(item).entrySet().iterator().next().getValue(), first.expr));

            } else {
                final List<ChildGroup> groupItems = mergedItems.isEmpty() ? emptyList() : convertToGroup(mergedItems, emptyList());
                result.add(new ChildGroup(itemName, groupItems, groupPaths, first.required, mapSources.get(item).isEmpty() ? first.source : mapSources.get(item).entrySet().iterator().next().getValue(), first.expr));
            }
        }   
        
        return result;
    }
    
    private static class CalcPropResult {
        final Expression2 expression; 
        final Map<String, List<Child>> internalSources;
        final List<Child> externalSourceChildren;
        
        private CalcPropResult(final Expression2 expression, final Map<String, List<Child>> internalSources, final List<Child> externalSourceChildren) {
            this.expression = expression;
            this.internalSources = internalSources;
            this.externalSourceChildren = externalSourceChildren;
        }
    }
    
    private static class SourceAndItsProps {
        private final IQrySource2<?> source;
        private final Map<String, List<AbstractPropInfo<?>>> propPathesByFullNames;

        public SourceAndItsProps(final IQrySource2<?> source, final Map<String, List<AbstractPropInfo<?>>> propPathesByFullNames) {
            this.source = source;
            this.propPathesByFullNames = propPathesByFullNames;
        }
    }

    private static class FirstPropInfoAndItsPathes {
        private final AbstractPropInfo<?> firstPropInfo; //first API from path
        private final Map<String, List<AbstractPropInfo<?>>> itsPropPathesByFullNames; // long names and their pathes that all start from 'propInfo'
        
        private FirstPropInfoAndItsPathes(final AbstractPropInfo<?> firstPropInfo, final Map<String, List<AbstractPropInfo<?>>> itsPropPathesByFullNames) {
            this.firstPropInfo = firstPropInfo;
            this.itsPropPathesByFullNames = itsPropPathesByFullNames;
        }
    }
}