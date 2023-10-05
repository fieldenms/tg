package ua.com.fielden.platform.eql.stage2.sources.enhance;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceInfoItem;
import ua.com.fielden.platform.eql.meta.query.ComponentTypeQuerySourceInfoItem;
import ua.com.fielden.platform.eql.meta.query.UnionTypeQuerySourceInfoItem;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.ImplicitNode;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;

public class PathToTreeTransformerUtils {

    public static PendingTail tailFromProp(final Prop2 prop) {
        return new PendingTail(new Prop2Lite(prop.name, prop.source.id()), convertPathToChunks(prop.getPath()));
    }

    public static final Collection<SourceTails> groupBySource(final Set<Prop2> props) {
        final SortedMap<Integer, T2<ISource2<?>, List<PendingTail>>> result = new TreeMap<>(); //need predictable order for testing purposes
        for (final Prop2 prop : props) {
            final T2<ISource2<?>, List<PendingTail>> existing = result.get(prop.source.id());
            if (existing != null) {
                existing._2.add(tailFromProp(prop));
            } else {
                final List<PendingTail> added = new ArrayList<>();
                added.add(tailFromProp(prop));
                result.put(prop.source.id(), T2.t2(prop.source, added));
            }
        }
        return result.values().stream().map(el -> new SourceTails(el._1, unmodifiableList(el._2))).toList();
    }
    
    public static Collection<FirstChunkGroup> groupByFirstChunk(final List<PendingTail> tails) {
        final SortedMap<String, T3<PropChunk, List<Prop2Lite>, List<PendingTail>>> firstChunkDataMap = new TreeMap<>(); //need predictable order for testing purposes
        
        for (final PendingTail pt : tails) {
            final PropChunk first = pt.tail().get(0);
            T3<PropChunk, List<Prop2Lite>, List<PendingTail>> existing = firstChunkDataMap.get(first.name());
            if (existing == null) {
                existing = T3.t3(first, new ArrayList<Prop2Lite>(), new ArrayList<PendingTail>());
                firstChunkDataMap.put(first.name(), existing);
            }

            if (pt.tail().size() == 1) {
                existing._2.add(pt.link());
            } else {
                existing._3.add(new PendingTail(pt.link(), pt.tail().subList(1, pt.tail().size())));    
            }
        }
        
        return firstChunkDataMap.values().stream().map(el -> new FirstChunkGroup(el._1, unmodifiableList(el._2), unmodifiableList(el._3))).toList();
    }

    /**
     * Obtains unique set of first chunks (only calculated) from the collection of pending tails.
     * 
     * @param props
     * @return
     */
    public static Collection<PropChunk> getFirstCalcChunks(final Collection<PendingTail> props) {
        final SortedMap<String, PropChunk> result = new TreeMap<>(); //need predictable order for testing purposes

        for (final PendingTail propEntry : props) {
            final PropChunk first = propEntry.tail().get(0);
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
    public static List<ImplicitNode> orderImplicitNodes(final List<ImplicitNode> all, final List<String> dependentCalcPropOrderFromMetadata) {
        final Map<String, ImplicitNode> dependentCalcPropNodes = new HashMap<>();
        final List<ImplicitNode> independentCalcPropNodes = new ArrayList<>();
        final List<ImplicitNode> result = new ArrayList<>(); // includes all non-calc prop nodes 

        for (final ImplicitNode node : all) {
            if (node.expr == null) {
                result.add(node); // adding all non-calc nodes first
            } else if (dependentCalcPropOrderFromMetadata.contains(node.name)) {
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

    public static List<PropChunk> convertPathToChunks(final List<AbstractQuerySourceInfoItem<?>> propPath) {
        final List<PropChunk> result = new ArrayList<>();
        String currentPropName = null;
        for (final AbstractQuerySourceInfoItem<?> querySourceInfoItem : propPath) {
            currentPropName = (currentPropName != null) ? currentPropName + "." + querySourceInfoItem.name : querySourceInfoItem.name;
            if (!(querySourceInfoItem instanceof ComponentTypeQuerySourceInfoItem || querySourceInfoItem instanceof UnionTypeQuerySourceInfoItem)) {
                // need to finalise and reset currentPropName
                result.add(new PropChunk(currentPropName, querySourceInfoItem));
                currentPropName = null;
            }
        }

        return result;
    }
    
    public static <T> Map<Integer, Map<String, T>> groupByExplicitSources(final List<? extends AbstractLinks<T>> resolutionsLinks) {
        final Map<Integer, Map<String, T>> resolutionsData = new HashMap<>();
        for (final AbstractLinks<T> item : resolutionsLinks) {
            for (final Prop2Lite link : item.links) {
                Map<String, T> existingSourceMap = resolutionsData.get(link.sourceId());
                if (existingSourceMap == null) {
                    existingSourceMap = new HashMap<String, T>();
                    resolutionsData.put(link.sourceId(), existingSourceMap);
                }
                existingSourceMap.put(link.name(), item.resolution);
            }
        }
        
        return resolutionsData;
    }
}