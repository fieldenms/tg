package ua.com.fielden.platform.eql.stage2.elements;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.eql.stage2.elements.ChildToChildGroupTransformator.convertToGroup;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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
            result.put(el.getKey(), convertToGroup(el.getValue()));
        }
        return result;
    }
    
    protected final Map<String, List<Child>> transform(final Set<EntProp2> props) {
        final Map<String, List<Child>> sourceChildren = new HashMap<>();

        for (final SourceAndItsProps sourceProps : groupBySource(props).values()) {
            final T2<List<Child>, Map<String, List<Child>>> genRes = generateQrySourceChildren( //
                    sourceProps.source, //
                    sourceProps.source.contextId(), //
                    sourceProps.propPathesByFullNames, //
                    emptyList());
            sourceChildren.put(sourceProps.source.contextId(), genRes._1);
            sourceChildren.putAll(genRes._2);
        }
        
        return sourceChildren;
    }

    private static final Map<String, SourceAndItsProps> groupBySource(final Set<EntProp2> props) {
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
            T2<String, List<AbstractPropInfo<?>>> pp = getFirstPropData(propEntry.getValue());
            FirstPropInfoAndItsPathes existing = result.get(pp._1);
            if (existing == null) {
                existing = new FirstPropInfoAndItsPathes(pp._2.get(0), pp._1);
                result.put(pp._1, existing);
            }

            existing.itsPropPathesByFullNames.put(propEntry.getKey(), pp._2);
        }

        return result;
    }
    
    private static T2<String, List<AbstractPropInfo<?>>> getFirstPropData(final List<AbstractPropInfo<?>> propPath) {
        int firstNonHeader = 0;
        for (AbstractPropInfo<?> propInfo : propPath) {
            if (propInfo instanceof ComponentTypePropInfo || propInfo instanceof UnionTypePropInfo) {
                firstNonHeader = firstNonHeader + 1;
            } else {
                break;
            }
        }
        
        if (firstNonHeader == 0) {
            return t2(propPath.get(0).name, propPath);
        } else {
            final String propName = propPath.subList(0, firstNonHeader + 1).stream().map(e -> e.name).collect(joining("."));
            final List<AbstractPropInfo<?>> propPath1 = propPath.subList(firstNonHeader, propPath.size());
            return t2(propName, propPath1);
        }
    }
    
    private T2<List<Child>, Map<String, List<Child>>> generateQrySourceChildren( //
            final IQrySource2<?> sourceForCalcPropResolution, // 
            final String explicitSourceId, //
            final Map<String, List<AbstractPropInfo<?>>> props, //long name + path
            final List<String> context //           
            ) {
        final List<Child> result = new ArrayList<>();
        final Map<String, List<Child>> other = new HashMap<>();
        
        for (final FirstPropInfoAndItsPathes propEntry : groupByFirstProp(props).values()) {
            final List<String> childContext = new ArrayList<>(context);
            childContext.add(propEntry.firstPropName);
            
            final T2<List<Child>, Map<String, List<Child>>> genRes = generateChild( //
                    sourceForCalcPropResolution, // 
                    explicitSourceId, //
                    propEntry, //
                    childContext);
            
            result.addAll(genRes._1);
            other.putAll(genRes._2);
        }
        return t2(result, other);
    }
    
    private T2<List<Child>, Map<String, List<Child>>> generateChild( //
            final IQrySource2<?> sourceForCalcPropResolution, //  
            final String explicitSourceId, //
            final FirstPropInfoAndItsPathes firstPropInfoAndItsPathes,
            final List<String> childContext //
            ) {
        
        final List<Child> result = new ArrayList<>();
        final Map<String, List<Child>> other = new HashMap<>();
        
        final String childContextString = childContext.stream().collect(joining("_"));

        final Set<String> dependenciesNames = new HashSet<>();
        final CalcPropResult calcPropResult = processCalcProp(firstPropInfoAndItsPathes.firstPropInfo, sourceForCalcPropResolution, childContextString);
        other.putAll(calcPropResult.internalSources);
        dependenciesNames.addAll(calcPropResult.externalSourceChildren.stream().map(c -> c.name).collect(toSet()));
        result.addAll(calcPropResult.externalSourceChildren);

        final T2<String, Map<String, List<AbstractPropInfo<?>>>> next = getPathAndNextProps(firstPropInfoAndItsPathes.itsPropPathesByFullNames);
        
        final String propName = firstPropInfoAndItsPathes.firstPropName;
        
        final String explicitSourceContextId = next._1 == null ? null : explicitSourceId;
        
        if (next._2.isEmpty()) {
            result.add(new Child(propName, emptyList(), next._1, false, null, calcPropResult.expression, explicitSourceContextId, emptySet()));    
        } else {
            final EntityTypePropInfo<?> propInfo = (EntityTypePropInfo<?>) firstPropInfoAndItsPathes.firstPropInfo;

            final QrySource2BasedOnPersistentType source = new QrySource2BasedOnPersistentType(propInfo.javaType(), propInfo.propEntityInfo, explicitSourceId + "_" + childContextString); 
            
            final T2<List<Child>, Map<String, List<Child>>> genRes = generateQrySourceChildren(
                    source, 
                    explicitSourceId,
                    next._2,
                    childContext);
            
            other.putAll(genRes._2);

            result.add(new Child(propName, genRes._1, next._1, propInfo.required, source, calcPropResult.expression, explicitSourceContextId, dependenciesNames));
        }
        
        return t2(result, other); 
    }    
    
    private CalcPropResult processCalcProp(final AbstractPropInfo<?> propInfo, final IQrySource2<?>  sourceForCalcPropResolution, final String childContext) {
        if (propInfo.hasExpression()) {
            final Expression2 expr2 = expressionToS2(sourceForCalcPropResolution, propInfo.expression, childContext);
            final Map<String, List<Child>> dependenciesResult = transform(expr2.collectProps());
            final List<Child> externalSourceChildren = dependenciesResult.remove(sourceForCalcPropResolution.contextId());
            return new CalcPropResult(expr2, dependenciesResult, externalSourceChildren == null ? emptyList() : externalSourceChildren);
        }
        
        return new CalcPropResult(null, emptyMap(), emptyList());
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
        private final String firstPropName;
        private final Map<String, List<AbstractPropInfo<?>>> itsPropPathesByFullNames = new HashMap<>(); // long names and their pathes that all start from 'propInfo'
        
        private FirstPropInfoAndItsPathes(final AbstractPropInfo<?> firstPropInfo, final String firstPropName) {
            this.firstPropInfo = firstPropInfo;
            this.firstPropName = firstPropName;
        }
    }
}