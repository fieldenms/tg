package ua.com.fielden.platform.eql.stage2.sources.enhance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.eql.meta.query.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.query.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.meta.query.UnionTypePropInfo;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.ImplicitNode;

public class PathToTreeTransformerUtils {

    public static PendingTail tailFromProp(final Prop2 prop) {
        return new PendingTail(new Prop2Lite(prop.name, prop.source.id()), convertPathToChunks(prop.getPath()));
    }

    public static final Collection<SourceTails> groupBySource(final Set<Prop2> props) {
        final SortedMap<Integer, SourceTails> result = new TreeMap<>(); //need predictable order for testing purposes
        for (final Prop2 prop : props) {
            final SourceTails existing = result.get(prop.source.id());
            if (existing != null) {
                existing.tails().add(tailFromProp(prop));
            } else {
                final List<PendingTail> added = new ArrayList<>();
                added.add(tailFromProp(prop));
                result.put(prop.source.id(), new SourceTails(prop.source, added));
            }
        }
        return result.values();
    }
    
    public static Collection<FirstChunkGroup> groupByFirstChunk(final List<PendingTail> tails) {
        final SortedMap<String, FirstChunkGroup> result = new TreeMap<>(); //need predictable order for testing purposes

        for (final PendingTail pt : tails) {
            final PropChunk first = pt.tail().get(0);
            FirstChunkGroup existing = result.get(first.name());
            if (existing == null) {
                existing = new FirstChunkGroup(first);
                result.put(first.name(), existing);
            }

            if (pt.tail().size() == 1) {
                existing.origins.add(pt.link());
            } else {
                existing.tails.add(new PendingTail(pt.link(), pt.tail().subList(1, pt.tail().size())));    
            }
        }

        return result.values();
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

    public static List<PropChunk> convertPathToChunks(final List<AbstractPropInfo<?>> propPath) {
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