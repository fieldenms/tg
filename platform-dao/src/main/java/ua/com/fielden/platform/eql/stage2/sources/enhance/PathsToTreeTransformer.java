package ua.com.fielden.platform.eql.stage2.sources.enhance;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.eql.exceptions.EqlStage2ProcessingException;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.meta.EntityTypePropInfo;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.UnionTypePropInfo;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage0.StandAloneExpressionBuilder;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.Expression1;
import ua.com.fielden.platform.eql.stage2.operands.Expression2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.BranchNode;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.LeafNode;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;

public class PathsToTreeTransformer {

    private final EqlDomainMetadata domainInfo;
    private final EntQueryGenerator gen;

    public PathsToTreeTransformer(final EqlDomainMetadata domainInfo, final EntQueryGenerator gen) {
        this.domainInfo = domainInfo;
        this.gen = gen;
    }
    
    public final TreeResult transform(final Set<Prop2> props) {
        final Map<Integer, List<LeafNode>> leaves = new HashMap<>();
        final Map<Integer, List<BranchNode>> branches = new HashMap<>();
        final List<ExpressionLinks> expressionsLinks = new ArrayList<>();
        final Map<Integer, Map<String, Expression2>> expressionsData = new HashMap<>();

        for (final SourceTails sourceTails : groupBySource(props)) {
            final T2<SourceResult, TreeResult> genRes = generateSourceNodes(sourceTails.source, sourceTails.tails, true);
            
            leaves.put(sourceTails.source.id(), genRes._1.leaves);
            branches.put(sourceTails.source.id(), genRes._1.branches);
            expressionsLinks.addAll(genRes._1.exprLinks);
            
            leaves.putAll(genRes._2.leavesMap());
            branches.putAll(genRes._2.branchesMap());
            expressionsData.putAll(genRes._2.expressionsData());
        }
        
        expressionsData.putAll(processExpressionsData(expressionsLinks));

        return new TreeResult(leaves, branches, expressionsData);
    }
    
    private T2<SourceResult, TreeResult> generateSourceNodes(
            final ISource2<?> sourceForCalcPropResolution, 
            final List<PendingTail> pendingTails,
            final boolean explicitSource // true if sourceForCalcPropResolution is explicit source
    ) {
        final List<LeafNode> leaves = new ArrayList<>();
        final Map<String, BranchNode> mapOfBranches = new HashMap<>();
        final List<ExpressionLinks> expressionsResolutions = new ArrayList<>();
        final Map<Integer, List<LeafNode>> otherSourcesLeaves = new HashMap<>();
        final Map<Integer, List<BranchNode>> otherSourcesBranches = new HashMap<>();
        final Map<Integer, Map<String, Expression2>> otherSourcesExpressionsData = new HashMap<>();
        
        final Set<String> propsToSkip = explicitSource ? new HashSet<String>(pendingTails.stream().map(p -> p.link.name()).toList()) : emptySet(); 
        final T2<Map<String, CalcPropData>, List<PendingTail>> procRes = 
                enhanceWithCalcPropsData(sourceForCalcPropResolution, emptyMap(), propsToSkip, pendingTails);

        final Map<String, CalcPropData> calcPropData = procRes._1;
        
        for (final FirstChunkGroup propEntry : groupByFirstChunk(procRes._2)) {
        	
        	final CalcPropData cpd = calcPropData.get(propEntry.firstChunk.name);
        	
        	if (cpd != null) {
        	    otherSourcesLeaves.putAll(cpd.internalsResult.leavesMap());
        	    otherSourcesBranches.putAll(cpd.internalsResult.branchesMap());
        	    otherSourcesExpressionsData.putAll(cpd.internalsResult.expressionsData());
        	}
        	
            final T2<NodeResult, TreeResult> genRes = generateNode(propEntry, cpd != null ? cpd.expr : null);

            if (genRes._1.leaf != null) {
                leaves.add(genRes._1.leaf);
            }
            if (genRes._1.branch != null) {
                mapOfBranches.put(genRes._1.branch.name, genRes._1.branch);
            }
            expressionsResolutions.addAll(genRes._1.exprLinks);

            if (genRes._2 != null) {
                otherSourcesLeaves.putAll(genRes._2.leavesMap());
                otherSourcesBranches.putAll(genRes._2.branchesMap());
                otherSourcesExpressionsData.putAll(genRes._2.expressionsData());
            }
        }

        final List<BranchNode> orderedBranches = orderBranches(mapOfBranches, calcPropData);

        return t2(new SourceResult(leaves, orderedBranches, expressionsResolutions), 
                new TreeResult(otherSourcesLeaves, otherSourcesBranches, otherSourcesExpressionsData));
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
			if (!processedCalcDataLocal.containsKey(calcChunk.name)) {	// consider only calc props that have not yet been processed
				final Expression1 exp1 = (Expression1) (new StandAloneExpressionBuilder(gen, calcChunk.data.expression)).getResult().getValue();
				final TransformationContext1 prc = new TransformationContext1(domainInfo, asList(asList(sourceForCalcPropResolution)), false);
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
				
				final T2<Set<String>, Set<String>> propDeps = distillCalcHeadsWithNodes(externalProps);
				final Set<String> dependencies = propDeps._1;
				final Set<String> dependenciesWithSubprops = propDeps._2;
				
				final TreeResult localCalcPropSourcesNodes = transform(internalProps);
				processedCalcDataLocal.put(calcChunk.name, 
				        new CalcPropData(exp2, dependencies, dependenciesWithSubprops, localCalcPropSourcesNodes));
				
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
				t2(processedCalcDataLocal, emptyList()) : 
					enhanceWithCalcPropsData(sourceForCalcPropResolution, processedCalcDataLocal, processedPropsLocal, addedTails);

		final List<PendingTail> allTails = new ArrayList<>();
		allTails.addAll(incomingTails);
		allTails.addAll(recursivelyEnhanced._2);

		return t2(recursivelyEnhanced._1, allTails);
	}
	
    private T2<NodeResult, TreeResult> generateNode(final FirstChunkGroup firstChunkGroup, final Expression2 expression) {
        final T2<List<Prop2Link>, List<PendingTail>> next = getLeafPathsAndNextPendingTails(firstChunkGroup.tails);
        final String propName = firstChunkGroup.firstChunk.name;
        
        if (next._2.isEmpty()) {
            if (expression == null) {
                return t2(new NodeResult(new LeafNode(propName, next._1), null, emptyList()), null);    
            } else {
                return t2(new NodeResult(null, null, listOf(new ExpressionLinks(expression, next._1))), null);
            }
        } else {
            final EntityTypePropInfo<?> propInfo = (EntityTypePropInfo<?>) firstChunkGroup.firstChunk.data;
            final Source2BasedOnPersistentType implicitSource = new Source2BasedOnPersistentType(propInfo.javaType(), propInfo.propEntityInfo, gen.nextSourceId()); 
            
            final T2<SourceResult, TreeResult> genRes = generateSourceNodes(implicitSource, next._2, false);
            final List<ExpressionLinks> exprLinks = new ArrayList<>();
            exprLinks.addAll(genRes._1.exprLinks);
            if (expression != null && !next._1.isEmpty()) {
                exprLinks.add(new ExpressionLinks(expression, next._1));
            }
            final NodeResult node = next._1.isEmpty() ? 
                    new NodeResult(null, new BranchNode(propName, genRes._1.leaves, genRes._1.branches, propInfo.required, implicitSource, expression), exprLinks) : 
                new NodeResult(expression != null ? null : new LeafNode(propName, next._1), new BranchNode(propName, genRes._1.leaves, genRes._1.branches, propInfo.required, implicitSource, expression), exprLinks);
            return t2(node, genRes._2);
        }
    }
	
	private static PendingTail tailFromProp(final Prop2 prop) {
	    return new PendingTail(new Prop2Link(prop.name, prop.source.id()), convertPathToChunks(prop.getPath()));
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
	
	/**
	 * Determines for the given set of Prop2 instances 2 subsets of them (names of their first chunks only):
	 * 1) those that are calculated and without subprops
	 * 2) those that are calculated and have subprops
	 * 
	 * @param props
	 * @return
	 */
	private static T2<Set<String>, Set<String>> distillCalcHeadsWithNodes(final Set<Prop2> props) {
		final Set<String> calcPropsWithSubprops = new HashSet<>();
		final Set<String> calcProps = new HashSet<>();
		
 		for (final Prop2 prop2 : props) {
			final T3<String, Boolean, Boolean> firstProp = obtainFirstChunkInfo(prop2.getPath());
			if (firstProp._2) {
				if (firstProp._3) {
					calcPropsWithSubprops.add(firstProp._1);
				} else {
				    calcProps.add(firstProp._1);    
				}
			}
		}
 		
		return t2(calcProps, calcPropsWithSubprops);
	}
    
	private static Collection<FirstChunkGroup> groupByFirstChunk(final List<PendingTail> tails) {
        final SortedMap<String, FirstChunkGroup> result = new TreeMap<>();  //need predictable order for testing purposes

        for (final PendingTail pt : tails) {
            final PropChunk first = pt.tail.get(0);
            FirstChunkGroup existing = result.get(first.name);
            if (existing == null) {
                existing = new FirstChunkGroup(first);
                result.put(first.name, existing);
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
            if (first.data.hasExpression() && !result.containsKey(first.name)) {
                result.put(first.name, first);
            }
        }

        return result.values();
    }
    
    /**
     * Gets list of calc props with subprops given calc prop directly or transitively depends on.
     * 
     * @param propName
     * @param allCalcData
     * @return
     */
    private static Set<String> unfoldDependencies(final String propName, final Map<String, CalcPropData> allCalcData) {
    	final Set<String> dependencies = new HashSet<>();
    	dependencies.addAll(allCalcData.get(propName).dependenciesWithSubprops);
    	
    	for (final String dependantProp : allCalcData.get(propName).dependencies) {
    		dependencies.addAll(unfoldDependencies(dependantProp, allCalcData));
		}
    	
    	return dependencies;
    }

    private static List<String> orderBranchesWithinCalculated(final Collection<BranchNode> calcNodesWithSubnodes, final Map<String, CalcPropData> allCalcData) {
        final Map<String, Set<String>> mapOfDependencies = new HashMap<>();

        for (final BranchNode node : calcNodesWithSubnodes) {
            mapOfDependencies.put(node.name, unfoldDependencies(node.name, allCalcData));
        }
       
        return sortTopologically(mapOfDependencies);
    }

    private static List<String> sortTopologically(final Map<String, Set<String>> mapOfDependencies) {
        final List<String> sorted = new ArrayList<>();

        while (!mapOfDependencies.isEmpty()) {
            String nextSorted = null;
            // let's find the first item without dependencies and regard it as "sorted"
            for (final Entry<String, Set<String>> el : mapOfDependencies.entrySet()) {
                if (el.getValue().isEmpty()) {
                    nextSorted = el.getKey();
                    break;
                }
            }

            sorted.add(nextSorted);
            mapOfDependencies.remove(nextSorted); // removing "sorted" item from map of remaining items

            // removing "sorted" item from dependencies of remaining items 
            for (final Entry<String, Set<String>> el : mapOfDependencies.entrySet()) {
                el.getValue().remove(nextSorted);
            }
        }

        return sorted;
    }
    
    private static List<BranchNode> orderBranches(final Map<String, BranchNode> all, final Map<String, CalcPropData> allCalcData) {
        final List<BranchNode> calcs = new ArrayList<>();
        final List<BranchNode> orderedItems = new ArrayList<>(); // includes all non-calc prop nodes and calc-prop nodes without 
        //children (they are not participating in "JOIN ON" directly, their expression is stored in associations for later look-up

        for (final BranchNode node : all.values()) {
            // if the node is calc-prop and has children (should participate in "JOIN ON") -- then needs ordering within this kind of nodes
            // Need to include calc-nodes without children into ordering to reveal transitive dependencies, but this is achieved 
            // by getting transitive dependencies from map of CalcPropData
            if (allCalcData.containsKey(node.name) && (!node.branches().isEmpty() || !node.leaves().isEmpty())) { 
                calcs.add(node);
            } else {
                orderedItems.add(node); // no special order required
            }
        }
        
        // adding ordered calc props nodes
        for (final String name : orderBranchesWithinCalculated(calcs, allCalcData)) {
            orderedItems.add(all.get(name));
        }

        return orderedItems;
    }

    private static T2<List<Prop2Link>, List<PendingTail>> getLeafPathsAndNextPendingTails(final List<PendingTail> subprops) {
        final List<PendingTail> nextTails = new ArrayList<>();
        final List<Prop2Link> paths = new ArrayList<>();

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

	/**
	 * Obtains from that Prop2 path data of its first chunk (name, is it calculated, does it have succeeding chunks).
	 * 
	 * @param propPath
	 * @return
	 */
    private static T3<String, Boolean, Boolean> obtainFirstChunkInfo(final List<AbstractPropInfo<?>> propPath) {
        String currentPropName = null;
        int propLength = 0;
        for (final AbstractPropInfo<?> propInfo : propPath) {
            propLength = propLength + 1;
            currentPropName = (currentPropName != null) ? currentPropName + "." + propInfo.name : propInfo.name;
            if (!(propInfo instanceof ComponentTypePropInfo || propInfo instanceof UnionTypePropInfo)) {
                return t3(currentPropName, propInfo.hasExpression(), propPath.size() > propLength);
            }
        }
        throw new EqlStage2ProcessingException(currentPropName, null);
    }

    private static Map<Integer, Map<String, Expression2>> processExpressionsData(final List<ExpressionLinks> expressionsResolutions) {
        final Map<Integer, Map<String, Expression2>> expressionsData = new HashMap<>();
        for (final ExpressionLinks item : expressionsResolutions) {
            for (final Prop2Link link : item.links) {
                Map<String, Expression2> existingSourceMap = expressionsData.get(link.sourceId());
                if (existingSourceMap == null) {
                    existingSourceMap = new HashMap<String, Expression2>();
                    expressionsData.put(link.sourceId(), existingSourceMap);
                }
                existingSourceMap.put(link.name(), item.expr);
            }
        }
        
        return expressionsData;
    }
	
    private static class FirstChunkGroup {
        private final PropChunk firstChunk;
        private final List<PendingTail> tails = new ArrayList<>(); // pending tails that all start from 'firstChunk'

        private FirstChunkGroup(final PropChunk firstChunk) {
            this.firstChunk = firstChunk;
        }
    }
    
    private static record PropChunk(String name, AbstractPropInfo<?> data) { 
	}

	// there are 2 types: 1) tail corresponds to link, 2) tail is shorter (as left side being converted into nodes)
	private static record PendingTail(Prop2Link link, List<PropChunk> tail) {
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

    private static record NodeResult(LeafNode leaf, BranchNode branch, List<ExpressionLinks> exprLinks) {
        private NodeResult {
            exprLinks = List.copyOf(exprLinks);
        }
    }
    
    private static record SourceResult(List<LeafNode> leaves, List<BranchNode> branches, List<ExpressionLinks> exprLinks) {
        private SourceResult {
            leaves = List.copyOf(leaves);
            branches = List.copyOf(branches);
            exprLinks = List.copyOf(exprLinks);
        }
    }
     
    private static record ExpressionLinks(Expression2 expr, List<Prop2Link> links) {
        private ExpressionLinks {
            links = List.copyOf(links);
        }
    }
    
    private static record CalcPropData (Expression2 expr, Set<String> dependencies, Set<String> dependenciesWithSubprops, TreeResult internalsResult) {
        private CalcPropData {
			dependencies = Set.copyOf(dependencies); //dependencies -- names of calc chunks that are used within given calc-prop expression without invoking its subprops
			dependenciesWithSubprops = Set.copyOf(dependenciesWithSubprops); //dependenciesWithSubprops -- names of calc chunks that are used within given calc-prop expression by invoking its subprops
		}
    }
}