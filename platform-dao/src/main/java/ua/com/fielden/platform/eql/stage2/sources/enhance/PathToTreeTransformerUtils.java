package ua.com.fielden.platform.eql.stage2.sources.enhance;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.eql.exceptions.EqlStage2ProcessingException;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.HelperNodeForImplicitJoins;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.enhance.PathsToTreeTransformer.AbstractLinks;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;

import java.util.*;

import static java.util.Collections.unmodifiableList;

/**
 * A collection of helper methods to perform core logic of {@link PathsToTreeTransformer}.
 *
 * @author TG Team
 */
public class PathToTreeTransformerUtils {

    private PathToTreeTransformerUtils() {}

    public static PendingTail tailFromProp(final Prop2 prop) {
        return new PendingTail(new Prop2Lite(prop.propPath, prop.source.id()), convertPathToChunks(prop.getPath()));
    }

    public static List<SourceTails> groupBySource(final Set<Prop2> props) {
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
        final SortedMap<String, T3<PropChunk, List<Prop2Lite>, List<PendingTail>>> firstChunkDataMap = new TreeMap<>(); // need predictable order for testing purposes

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
     * Obtains unique set of first chunks (only calculated) from the list of pending tails.
     */
    public static Collection<PropChunk> getFirstCalcChunks(final List<PendingTail> props) {
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
     * Establishes proper sequence of helper nodes to be used for JOINs generation later on.
     */
    public static List<HelperNodeForImplicitJoins> orderHelperNodes(final List<HelperNodeForImplicitJoins> all, final List<String> dependentCalcPropOrderFromMetadata) {
        final Map<String, HelperNodeForImplicitJoins> dependentCalcPropNodes = new HashMap<>();
        final List<HelperNodeForImplicitJoins> independentCalcPropNodes = new ArrayList<>();
        final List<HelperNodeForImplicitJoins> result = new ArrayList<>(); // includes all non-calc prop nodes

        for (final HelperNodeForImplicitJoins node : all) {
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
            final HelperNodeForImplicitJoins foundCalcProp = dependentCalcPropNodes.get(calcProp);
            if (foundCalcProp != null) { // TODO why to check null condition?
                result.add(foundCalcProp);
            }
        }

        return result;
    }

    private static ImmutableList<PropChunk> convertPathToChunks(final List<AbstractQuerySourceItem<?>> propPath) {
        final var chunks = ImmutableList.<PropChunk>builder();

        String currentPropName = null;
        for (final AbstractQuerySourceItem<?> querySourceInfoItem : propPath) {
            currentPropName = (currentPropName != null) ? currentPropName + "." + querySourceInfoItem.name : querySourceInfoItem.name;
            if (!isHeaderProperty(querySourceInfoItem)) {
                // need to finalise and reset currentPropName
                chunks.add(new PropChunk(currentPropName, querySourceInfoItem));
                currentPropName = null;
            }
        }

        return chunks.build();
    }

    /**
     * Determines whether {@code querySourceItem} represents a header for a component or union type property.
     */
    public static boolean isHeaderProperty(final AbstractQuerySourceItem<?> querySourceItem) {
        return querySourceItem instanceof QuerySourceItemForComponentType || querySourceItem instanceof QuerySourceItemForUnionType;
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
                existingSourceMap.put(link.propPath(), item.resolution);
            }
        }

        return resolutionsData;
    }

    record SourceTails(ISource2<?> source, List<PendingTail> tails) {
    }

    /**
     * There are 2 types:
     * <ol>
     *   <li> tail corresponds to link
     *   <li> tail is shorter (as left side being converted into nodes)
     * </ol>
     */
    record PendingTail(Prop2Lite link, List<PropChunk> tail) {
        public PendingTail {
            if (tail.isEmpty()) {
                throw new EqlStage2ProcessingException("Tail cannot be empty. Link: %s".formatted(link));
            }
            tail = ImmutableList.copyOf(tail);
        }
    }

    /**
     * @param origins originals props for which {@link #firstChunk} happened to be the last PropChunk in their pending tail
     * @param tails   tails that follow {@link #firstChunk}
     */
    record FirstChunkGroup(PropChunk firstChunk, List<Prop2Lite> origins, List<PendingTail> tails) {}

    /**
     * Lightweight representation of {@link Prop2} taht contains all ingredients of its identity.
     * <p>
     * Used to build associations between {@link Prop2} and a corresponding {@link Prop3}.
     *
     * @param propPath  propPath from a corresponding existing {@link Prop2}
     * @param sourceId  {@link ISource2#id()} from a corresponding existing {@link Prop2}
     */
    record Prop2Lite (String propPath, Integer sourceId) {}
}
