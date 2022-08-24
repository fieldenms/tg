package ua.com.fielden.platform.eql.stage2;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.meta.EntityTypePropInfo;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.UnionTypePropInfo;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage0.StandAloneExpressionBuilder;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.ChildGroup;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.types.tuples.T2;

public class PathsToTreeTransformer {

    private final EqlDomainMetadata domainInfo;
    private final EntQueryGenerator gen;

    public PathsToTreeTransformer(final EqlDomainMetadata domainInfo, final EntQueryGenerator gen) {
        this.domainInfo = domainInfo;
        this.gen = gen;
    }

    public final Map<Integer, List<ChildGroup>> transform(final Set<Prop2> props) {
        final Map<Integer, List<ChildGroup>> sourceChildren = new HashMap<>();

        for (final SourceAndItsProps sourceProps : groupBySource(props).values()) {
            final T2<List<ChildGroup>, Map<Integer, List<ChildGroup>>> genRes = generateQrySourceChildren( //
                    sourceProps.source, //
                    sourceProps.propPathsByFullNames);
            sourceChildren.put(sourceProps.source.id(), genRes._1);
            sourceChildren.putAll(genRes._2);
        }

        return sourceChildren;
    }

    private static final Map<Integer, SourceAndItsProps> groupBySource(final Set<Prop2> props) {
        final Map<Integer, SourceAndItsProps> result = new HashMap<>();
        for (final Prop2 prop : props) {
            final SourceAndItsProps existing = result.get(prop.source.id());
            if (existing != null) {
                existing.propPathsByFullNames.add(new PendingTail(prop.name, prop.source.id(), prop.getPath())); // NOTE: for rare cases where two EntProp2 are identical except isId value, replacement can occur, but with identical value of path 
            } else {
                final List<PendingTail> added = new ArrayList<>();
                added.add(new PendingTail(prop.name, prop.source.id(), prop.getPath()));
                result.put(prop.source.id(), new SourceAndItsProps(prop.source, added));
            }
        }
        return result;
    }
    
    private T2<Map<String, CalcPropData>, List<PendingTail>> enhanceWithCalcProps(
    		final ISource2<?> sourceForCalcPropResolution,
    		final Map<String, CalcPropData> existingMapExpressionByNames,
    		final List<PendingTail> incomingPropPathesByFullNames) {
    	
    	final Map<String, CalcPropData> mapExpressionByNames = new HashMap<>();
    	mapExpressionByNames.putAll(existingMapExpressionByNames); // key is the same as FirstPropInfoAndItsPathes.firstPropName
    	final List<PendingTail> addedPropPathesByFullNames = new ArrayList<>();
    
    	for (final FirstPropInfoAndItsPaths pe : groupByFirstProp(incomingPropPathesByFullNames).values()) {
    		
    		if (pe.firstPropInfo.expression != null && !mapExpressionByNames.containsKey(pe.firstPropName)) {
    	        final TransformationContext prc = new TransformationContext(domainInfo, asList(asList(sourceForCalcPropResolution)), false);
    	        final Expression1 exp1 = (Expression1) (new StandAloneExpressionBuilder(gen, pe.firstPropInfo.expression)).getResult().getValue();
    	        final Expression2 exp2 = exp1.transform(prc);
    	        final Set<Prop2> expProps = exp2.collectProps();
    	        final Set<String> dependencies = new HashSet<>();
    	        // separate in external and internal
    	        //final Set<Prop2> externalProps = new HashSet<>();
    	        final Set<Prop2> internalProps = new HashSet<>();
    	        for (Prop2 prop2 : expProps) {
					if (prop2.source.id().equals(sourceForCalcPropResolution.id())) {
						dependencies.add(getFirstPropData(prop2.getPath())._1);
					//	if (!incomingPropPathesByFullNames.containsKey(prop2.name) && !addedPropPathesByFullNames.containsKey(prop2.name)) {
							addedPropPathesByFullNames.add(new PendingTail(prop2.name, sourceForCalcPropResolution.id(), prop2.getPath()));
					//	}
						//externalProps.add(prop2);
					} else {
						internalProps.add(prop2);
					}
				}
    	        
    	        
    	        final Map<Integer, List<ChildGroup>> localCalcPropSourcesNodes = transform(internalProps);
    	        mapExpressionByNames.put(pe.firstPropName, new CalcPropData(exp2, dependencies, localCalcPropSourcesNodes));
    		}
    	}
    	
    	final T2<Map<String, CalcPropData>, List<PendingTail>> recRes =
    			addedPropPathesByFullNames.isEmpty() ? T2.t2(mapExpressionByNames, Collections.emptyList()) : enhanceWithCalcProps(sourceForCalcPropResolution, mapExpressionByNames, addedPropPathesByFullNames);
    	
    	final List<PendingTail> fullListPropPathesByFullNames = new ArrayList<>();
    	fullListPropPathesByFullNames.addAll(incomingPropPathesByFullNames);
    	fullListPropPathesByFullNames.addAll(addedPropPathesByFullNames);
    	fullListPropPathesByFullNames.addAll(recRes._2);
    	
    	return T2.t2(recRes._1, fullListPropPathesByFullNames);
    }
   
    private static SortedMap<String, FirstPropInfoAndItsPaths> groupByFirstProp(final List<PendingTail> props) {
        final SortedMap<String, FirstPropInfoAndItsPaths> result = new TreeMap<>();

        for (final PendingTail propEntry : props) {
            final T2<String, List<AbstractPropInfo<?>>> pp = getFirstPropData(propEntry.tail);
            FirstPropInfoAndItsPaths existing = result.get(pp._1);
            if (existing == null) {
                existing = new FirstPropInfoAndItsPaths(pp._2.get(0), pp._1);
                result.put(pp._1, existing);
            }

            existing.itsPropPathsByFullNames.add(new PendingTail(propEntry.fullPathName, propEntry.sourceId, pp._2));
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

    private T2<List<ChildGroup>, Map<Integer, List<ChildGroup>>> generateQrySourceChildren(
            final ISource2<?> sourceForCalcPropResolution, 
            final List<PendingTail> propPathesByFullNames
    ) {
        final Map<String, ChildGroup> result = new HashMap<>();
        final Map<Integer, List<ChildGroup>> other = new HashMap<>();

        // here need to S2 each calc prop and add its props resolved to parent to "propPathesByFullNames" and 
        // invoke transform(final Set<Prop2> props) for all other props (resolving to calc prop local sources) and add the result to "other" in loop.
        // 1) collect firstProps that are calc-props
        // 2) transform those props to Expression2 by resolving them against "sourceForCalcPropResolution" (store its association with prop name/path)
        // 3) Expression2.collectProps() -> separate those that has "sourceForCalcPropResolution" as their source from the rest (lets call them OTHERS)
        // 4) add the first group of props to initial "propPathesByFullNames" and also collect their names for "dependentProps" related to given Expression2 item
        // 5) invoke "transform" with OTHERS and add its result to "other".
        
        // need to repeat 1)-3) until no occurrence of calc-props within newly discovered props that resolves to "sourceForCalcPropResolution"
                
        final T2<Map<String, CalcPropData>, List<PendingTail>> procRes = enhanceWithCalcProps(sourceForCalcPropResolution, emptyMap(), propPathesByFullNames);
        
        final Map<String, CalcPropData> propData = procRes._1;
        for (final FirstPropInfoAndItsPaths propEntry : groupByFirstProp(procRes._2).values()) {
        	final T2<FirstPropInfoAndItsPaths, Expression2> propChildInput;
        	final CalcPropData cpd = propData.get(propEntry.firstPropName);
        	if (cpd != null) {
        		other.putAll(cpd.internals);
        		propChildInput = T2.t2(propEntry, cpd.expr);
        	} else {
        		propChildInput = T2.t2(propEntry, null);
        	}
            final T2<ChildGroup, Map<Integer, List<ChildGroup>>> genRes = generateChild(propChildInput);

            result.put(genRes._1.name, genRes._1);
            other.putAll(genRes._2);
        }

        final List<String> ordered = orderItems(result.values(), propData);
        final List<ChildGroup> orderedChildred = new ArrayList<>();

        for (String string : ordered) {
			orderedChildred.add(result.get(string));
		}
        
        return t2(orderedChildred, other);
    }
    
    private static List<String> orderItems(final Collection<ChildGroup> children, final Map<String, CalcPropData> propData) {
        final Map<String, Set<String>> mapOfDependencies = new HashMap<>();

        for (ChildGroup childGroup : children) {
        	mapOfDependencies.put(childGroup.name, Collections.emptySet());
		}
        
        for (final Entry<String, CalcPropData> item : propData.entrySet()) {
            mapOfDependencies.put(item.getKey(), new HashSet<>(item.getValue().dependencies));
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

        return orderedItems;
    }
    
    private T2<ChildGroup, Map<Integer, List<ChildGroup>>> generateChild(final T2<FirstPropInfoAndItsPaths, Expression2> firstPropInfoAndItsPathes) {
        final T2<Map<String, Integer>, List<PendingTail>> next = getLeafPathsOrNextPendingTails(firstPropInfoAndItsPathes._1.itsPropPathsByFullNames);
        final String propName = firstPropInfoAndItsPathes._1.firstPropName;
        
        if (next._2.isEmpty()) {
        	return t2(new ChildGroup(propName, emptyList(), next._1, false, null, firstPropInfoAndItsPathes._2), emptyMap());
        } else {
            final EntityTypePropInfo<?> propInfo = (EntityTypePropInfo<?>) firstPropInfoAndItsPathes._1.firstPropInfo;
            final Source2BasedOnPersistentType implicitSource = new Source2BasedOnPersistentType(propInfo.javaType(), propInfo.propEntityInfo, gen.nextSourceId()); 
            final T2<List<ChildGroup>, Map<Integer, List<ChildGroup>>> genRes = generateQrySourceChildren(implicitSource, next._2);
            return t2(new ChildGroup(propName, genRes._1, next._1, propInfo.required, implicitSource, firstPropInfoAndItsPathes._2), genRes._2);
        }
    }

    private static T2<Map<String, Integer>, List<PendingTail>> getLeafPathsOrNextPendingTails(final List<PendingTail> subprops) {
        final List<PendingTail> nextTails = new ArrayList<>();
        final Map<String, Integer> paths = new HashMap<>();

        for (final PendingTail subpropEntry : subprops) {
            if (subpropEntry.tail.size() > 1) {
                nextTails.add(new PendingTail(subpropEntry.fullPathName, subpropEntry.sourceId, subpropEntry.tail.subList(1, subpropEntry.tail.size())));
            } else {
            	paths.put(subpropEntry.fullPathName, subpropEntry.sourceId); //TODO ensure unique keys -- paths.put(subpropEntry.sourceId, subpropEntry.fullPathName);
            }
        }
        return t2(paths, nextTails);
    }
    
    private static class PendingTail {
    	private final String fullPathName;
    	private final Integer sourceId;
    	private final List<AbstractPropInfo<?>> tail;
		
    	private PendingTail(final String fullPathName, final Integer sourceId, final List<AbstractPropInfo<?>> tail) {
			this.fullPathName = fullPathName;
			this.sourceId = sourceId;
			this.tail = tail;
		}
    }

    private static class SourceAndItsProps {
        private final ISource2<?> source;
        private final List<PendingTail> propPathsByFullNames;

        private SourceAndItsProps(final ISource2<?> source, final List<PendingTail> propPathesByFullNames) {
            this.source = source;
            this.propPathsByFullNames = propPathesByFullNames;
        }
    }

    private static class FirstPropInfoAndItsPaths {
        private final AbstractPropInfo<?> firstPropInfo; //first API from path
        private final String firstPropName; // can contain dots (in case of union type or composite value type props) 
        private final List<PendingTail> itsPropPathsByFullNames = new ArrayList<>(); // long names and their pathes that all start from 'propInfo'

        private FirstPropInfoAndItsPaths(final AbstractPropInfo<?> firstPropInfo, final String firstPropName) {
            this.firstPropInfo = firstPropInfo;
            this.firstPropName = firstPropName;
        }
    }
    
    private static class CalcPropData {
		private final Expression2 expr;
    	private final Set<String> dependencies;
    	private final Map<Integer, List<ChildGroup>> internals;

    	private CalcPropData(Expression2 expr, Set<String> dependencies, Map<Integer, List<ChildGroup>> internals) {
			this.expr = expr;
			this.dependencies = dependencies;
			this.internals = internals;
		}
    }
}