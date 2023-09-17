package ua.com.fielden.platform.eql.stage2.sources.enhance;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.query.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.meta.query.EntityTypePropInfo;
import ua.com.fielden.platform.eql.meta.query.UnionTypePropInfo;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage0.StandAloneExpressionBuilder;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.ImplicitNode;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.types.tuples.T2;

public class PathsToTreeTransformer {

    private final QuerySourceInfoProvider querySourceInfoProvider;
    private final EntQueryGenerator gen;

    public PathsToTreeTransformer(final QuerySourceInfoProvider querySourceInfoProvider, final EntQueryGenerator gen) {
        this.querySourceInfoProvider = querySourceInfoProvider;
        this.gen = gen;
    }
    
    public final TreeResult transform(final Set<Prop2> props) {
        final Map<Integer, List<ImplicitNode>> nodes = new HashMap<>();
        final List<ExpressionLinks> expressionsLinks = new ArrayList<>();
        final List<Prop3Links> propLinks = new ArrayList<>();

        for (final SourceTails sourceTails : groupBySource(props)) {
            final T2<List<ImplicitNode>, TreeResult> genRes = generateSourceNodes(sourceTails.source, sourceTails.tails, true);
            
            nodes.put(sourceTails.source.id(), genRes._1);
            nodes.putAll(genRes._2.implicitNodesMap());
            expressionsLinks.addAll(genRes._2.expressionsData());
            propLinks.addAll(genRes._2.propsData());
        }
        
        return new TreeResult(nodes, propLinks, expressionsLinks);
    }
    
    private T2<List<ImplicitNode>, TreeResult> generateSourceNodes(
            final ISource2<?> sourceForCalcPropResolution, 
            final List<PendingTail> pendingTails,
            final boolean explicitSource // true if sourceForCalcPropResolution is explicit source
    ) {
        final List<ImplicitNode> listOfNodes = new ArrayList<>();
        final Map<Integer, List<ImplicitNode>> otherSourcesNodes = new HashMap<>();
        final List<ExpressionLinks> expressionLinks = new ArrayList<>();
        final List<Prop3Links> propLinks = new ArrayList<>();
        
        final Set<String> propsToSkip = explicitSource ? new HashSet<String>(pendingTails.stream().map(p -> p.link.name()).toList()) : emptySet(); 
        final T2<Map<String, CalcPropData>, List<PendingTail>> procRes = 
                enhanceWithCalcPropsData(sourceForCalcPropResolution, emptyMap(), propsToSkip, pendingTails);

        final Map<String, CalcPropData> calcPropData = procRes._1;
        
        for (final FirstChunkGroup propEntry : groupByFirstChunk(procRes._2)) {
        	
        	final CalcPropData cpd = calcPropData.get(propEntry.firstChunk.name());
        	
        	if (cpd != null) {
        	    otherSourcesNodes.putAll(cpd.internalsResult.implicitNodesMap());
        	    expressionLinks.addAll(cpd.internalsResult.expressionsData());
        	    propLinks.addAll(cpd.internalsResult.propsData());
        	}
        	
        	final T2<ImplicitNode, TreeResult> genRes = generateNode(propEntry, cpd != null ? cpd.expr : null, sourceForCalcPropResolution.id());
        	
            if (genRes._1 != null) {
                listOfNodes.add(genRes._1);
            }

            if (genRes._2 != null) {
                otherSourcesNodes.putAll(genRes._2.implicitNodesMap());
                expressionLinks.addAll(genRes._2.expressionsData());
                propLinks.addAll(genRes._2.propsData());
            }
        }
        
        final List<String> orderedCalcPropsForType = isUnionEntityType(sourceForCalcPropResolution.sourceType()) || sourceForCalcPropResolution.sourceType().equals(EntityAggregates.class) ? emptyList() : querySourceInfoProvider.getCalcPropsOrder(sourceForCalcPropResolution.sourceType());

        final List<ImplicitNode> orderedNodes = orderImplicitNodes(listOfNodes, orderedCalcPropsForType);

        return t2(orderedNodes, new TreeResult(otherSourcesNodes, propLinks, expressionLinks));
    }

	private T2<Map<String, CalcPropData>, List<PendingTail>> enhanceWithCalcPropsData(
			final ISource2<?> sourceForCalcPropResolution, 
			final Map<String, CalcPropData> processedCalcData,
			final Set<String> processedProps, // Prop2 name
			final List<PendingTail> incomingTails) {
		
		final Map<String, CalcPropData> processedCalcDataLocal = new HashMap<>(); // used to store processed calc props (key is the same as FirstPropInfoAndItsPathes.firstPropName)
		processedCalcDataLocal.putAll(processedCalcData);
		
		final Set<String> processedPropsLocal = new HashSet<>();
		processedPropsLocal.addAll(processedProps);

		final List<PendingTail> addedTails = new ArrayList<>();

		for (final PropChunk calcChunk : getFirstCalcChunks(incomingTails)) {
			if (!processedCalcDataLocal.containsKey(calcChunk.name())) {	// consider only calc props that have not yet been processed
				final Expression1 exp1 = (Expression1) (new StandAloneExpressionBuilder(gen, calcChunk.data().expression)).getResult().getValue();
				final TransformationContext1 prc = (new TransformationContext1(querySourceInfoProvider)).cloneWithAdded(sourceForCalcPropResolution);
				final Expression2 exp2 = exp1.transform(prc);
				final Set<Prop2> expProps = exp2.collectProps();
				// separate into external and internal
				final Set<Prop2> externalProps = new HashSet<>();
				final Set<Prop2> internalProps = new HashSet<>();

				for (final Prop2 prop2 : expProps) {
					if (prop2.source.id().equals(sourceForCalcPropResolution.id())) {
						externalProps.add(prop2);
					} else {
						internalProps.add(prop2);
					}
				}
				
				final TreeResult localCalcPropSourcesNodes = transform(internalProps);
				processedCalcDataLocal.put(calcChunk.name(), new CalcPropData(exp2, localCalcPropSourcesNodes));
				
				for (final Prop2 prop : externalProps) {
					if (!processedPropsLocal.contains(prop.name)) {
						addedTails.add(tailFromProp(prop));
						processedPropsLocal.add(prop.name);
					}
				}
			}
		}
		
		// let's recursively enhance newly added tails (as they can have unprocessed calc props among them)
		final T2<Map<String, CalcPropData>, List<PendingTail>> recursivelyEnhanced = addedTails.isEmpty() ? 
				t2(unmodifiableMap(processedCalcDataLocal), emptyList()) : 
					enhanceWithCalcPropsData(sourceForCalcPropResolution, processedCalcDataLocal, processedPropsLocal, addedTails);

		final List<PendingTail> allTails = new ArrayList<>();
		allTails.addAll(incomingTails);
		allTails.addAll(recursivelyEnhanced._2);

		return t2(recursivelyEnhanced._1, allTails);
	}
	
    private T2<ImplicitNode, TreeResult> generateNode(final FirstChunkGroup firstChunkGroup, final Expression2 expression, final Integer currResolutionSourceId) {
        final T2<List<Prop2Lite>, List<PendingTail>> next = getLeafPathsAndNextPendingTails(firstChunkGroup.tails);
        final String propName = firstChunkGroup.firstChunk.name();
        
        if (next._2.isEmpty()) {
            if (expression == null) {
                return t2(null, new TreeResult(emptyMap(), List.of(new Prop3Links(new Prop3Lite(propName, currResolutionSourceId), next._1)), emptyList()));
            } else {
                return t2(null, new TreeResult(emptyMap(), emptyList(), List.of(new ExpressionLinks(expression, next._1))));
            }
        } else {
            final EntityTypePropInfo<?> propInfo = (EntityTypePropInfo<?>) firstChunkGroup.firstChunk.data();
            final Source2BasedOnPersistentType implicitSource = new Source2BasedOnPersistentType(propInfo.javaType(), propInfo.propQuerySourceInfo, gen.nextSourceId()); 
            
            final T2<List<ImplicitNode>, TreeResult> genRes = generateSourceNodes(implicitSource, next._2, false);
            final List<ExpressionLinks> expressionLinks = new ArrayList<>();
            final List<Prop3Links> propLinks = new ArrayList<>();
            expressionLinks.addAll(genRes._2.expressionsData());
            propLinks.addAll(genRes._2.propsData());
            
            if (!next._1.isEmpty()) {
                if (expression != null) {
                    expressionLinks.add(new ExpressionLinks(expression, next._1));
                } else {
                    propLinks.add(new Prop3Links(new Prop3Lite(propName, currResolutionSourceId), next._1));
                }
            }
            
            final ImplicitNode node = new ImplicitNode(propName, genRes._1, propInfo.nonnullable, implicitSource, expression);
            
            return t2(node, new TreeResult(genRes._2.implicitNodesMap(), propLinks, expressionLinks));
        }
    }
	
	private static PendingTail tailFromProp(final Prop2 prop) {
	    return new PendingTail(new Prop2Lite(prop.name, prop.source.id()), convertPathToChunks(prop.getPath()));
	}
	
    private static final Collection<SourceTails> groupBySource(final Set<Prop2> props) {
        final SortedMap<Integer, SourceTails> result = new TreeMap<>(); //need predictable order for testing purposes
        for (final Prop2 prop : props) {
            final SourceTails existing = result.get(prop.source.id());
            if (existing != null) {
                existing.tails.add(tailFromProp(prop));
            } else {
                final List<PendingTail> added = new ArrayList<>();
                added.add(tailFromProp(prop));
                result.put(prop.source.id(), new SourceTails(prop.source, added));
            }
        }
        return result.values();
    }
	
	private static Collection<FirstChunkGroup> groupByFirstChunk(final List<PendingTail> tails) {
        final SortedMap<String, FirstChunkGroup> result = new TreeMap<>();  //need predictable order for testing purposes

        for (final PendingTail pt : tails) {
            final PropChunk first = pt.tail.get(0);
            FirstChunkGroup existing = result.get(first.name());
            if (existing == null) {
                existing = new FirstChunkGroup(first);
                result.put(first.name(), existing);
            }

            existing.tails.add(pt);
        }

        return result.values();
    }
	
	/**
	 * Obtains unique set of first chunks (only calculated) from the collection of pending tails.
	 * 
	 * @param props
	 * @return
	 */
	private static Collection<PropChunk> getFirstCalcChunks(final Collection<PendingTail> props) {
        final SortedMap<String, PropChunk> result = new TreeMap<>();  //need predictable order for testing purposes

        for (final PendingTail propEntry : props) {
            final PropChunk first = propEntry.tail.get(0);
            if (first.data().hasExpression() && !result.containsKey(first.name())) {
                result.put(first.name(), first);
            }
        }

        return result.values();
    }

    /**
     * Establishes proper sequence of implicit nodes to be used for JOINs generation later on. 
     * 
     * @param all
     * @param dependentCalcPropOrderFromMetadata
     * @return
     */
	private static List<ImplicitNode> orderImplicitNodes(final List<ImplicitNode> all, final List<String> dependentCalcPropOrderFromMetadata) {
        final Map<String, ImplicitNode> dependentCalcPropNodes = new HashMap<>();
        final List<ImplicitNode> independentCalcPropNodes = new ArrayList<>();
        final List<ImplicitNode> result = new ArrayList<>(); // includes all non-calc prop nodes 

        for (final ImplicitNode node : all) {
            if (node.expr == null) { 
                result.add(node); // adding all non-calc nodes first
            } else if (dependentCalcPropOrderFromMetadata.contains(node.name)){
                dependentCalcPropNodes.put(node.name, node); 
            } else {
                independentCalcPropNodes.add(node);
            }
        }
        
        result.addAll(independentCalcPropNodes); // adding all independent calc prop nodes 
        
        // adding all dependent calc prop nodes in the order as specified in their metadata
        for (final String calcProp : dependentCalcPropOrderFromMetadata) {
            final ImplicitNode foundCalcProp = dependentCalcPropNodes.get(calcProp);
            if (foundCalcProp != null) {
                result.add(foundCalcProp);        
            }
        }

        return result;
    }

    private static T2<List<Prop2Lite>, List<PendingTail>> getLeafPathsAndNextPendingTails(final List<PendingTail> subprops) {
        final List<PendingTail> nextTails = new ArrayList<>();
        final List<Prop2Lite> paths = new ArrayList<>();

        for (final PendingTail subpropEntry : subprops) {
            if (subpropEntry.tail.size() > 1) {
                nextTails.add(new PendingTail(subpropEntry.link, subpropEntry.tail.subList(1, subpropEntry.tail.size())));
            } else {
            	paths.add(subpropEntry.link);
            }
        }
        return t2(paths, nextTails);
    }

	private static List<PropChunk> convertPathToChunks(final List<AbstractPropInfo<?>> propPath) {
		final List<PropChunk> result = new ArrayList<>();
		String currentPropName = null;
		for (final AbstractPropInfo<?> propInfo : propPath) {
			currentPropName = (currentPropName != null) ? currentPropName + "." + propInfo.name : propInfo.name;
			if (!(propInfo instanceof ComponentTypePropInfo || propInfo instanceof UnionTypePropInfo)) {
				// need to finalise and reset currentPropName
				result.add(new PropChunk(currentPropName, propInfo));
				currentPropName = null;
			}
		}

		return result;
	}

    private static class FirstChunkGroup {
        private final PropChunk firstChunk;
        private final List<PendingTail> tails = new ArrayList<>(); // pending tails that all start from 'firstChunk'

        private FirstChunkGroup(final PropChunk firstChunk) {
            this.firstChunk = firstChunk;
        }
    }
    
	// there are 2 types: 1) tail corresponds to link, 2) tail is shorter (as left side being converted into nodes)
	private static record PendingTail(Prop2Lite link, List<PropChunk> tail) {
    	private PendingTail {
    		tail = List.copyOf(tail);
		}
    }
    
    private static record SourceTails(ISource2<?> source, List<PendingTail> tails) {
        private SourceTails(final ISource2<?> source, final List<PendingTail> tails) {
            this.source = source;
            this.tails = tails;// unmodifiableList(tails);
        }
    }
    
    private static record CalcPropData (Expression2 expr, TreeResult internalsResult) {
    }
}