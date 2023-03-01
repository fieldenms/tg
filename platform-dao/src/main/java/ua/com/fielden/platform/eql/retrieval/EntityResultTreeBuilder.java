package ua.com.fielden.platform.eql.retrieval;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.eql.meta.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.EntityInfo;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.EqlPropertyMetadata;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

public final class EntityResultTreeBuilder {
    
    public static <E extends AbstractEntity<?>> EntityTree<E> build(final Class<E> resultType, final List<T2<String, ResultQueryYieldDetails>> properties, final EqlDomainMetadata md) {
        return build(resultType, properties, -1, md)._1;
    }
    
    private static <E extends AbstractEntity<?>> T2<EntityTree<E>, Integer> build(
            final Class<E> resultType, 
            final List<T2<String, ResultQueryYieldDetails>> properties, 
            final Integer initialIndex,
            final EqlDomainMetadata md) {
        
        final Map<Integer, YieldDetails> singles = new HashMap<>();
        final Map<String /*composite property name*/, EntityTree<? extends AbstractEntity<?>>> entities = new HashMap<>();
        final Map<String /*composite value property name*/, ValueTree> compositeValues = new HashMap<>();
        
        Integer localIndex = initialIndex;
        
        String currentGroup = null;
        Class<? extends AbstractEntity<?>> currentResultType = null;
        ICompositeUserTypeInstantiate currentHibType = null;
        List<T2<String, ResultQueryYieldDetails>> currentGroupDetails = new ArrayList<>();
        
        for (final T2<String, ResultQueryYieldDetails> prop : properties) {
            if (prop._1.contains(".")) {
                final int firstDotIndex = prop._1.indexOf(".");
                final String propGroup = prop._1.substring(0, firstDotIndex);
                
                if (propGroup.equals(currentGroup)) {
                    currentGroupDetails.add(T2.t2(prop._1.substring(firstDotIndex + 1), prop._2));
                } else { // no explicit group header -- either composite value type or EA in SE result yields 
                    if (currentGroup != null) {
                        // finalise current group
                        if (currentResultType != null) {
                            final T2<?, Integer> t2 = build(currentResultType, currentGroupDetails, localIndex, md);
                            entities.put(currentGroup, (EntityTree<? extends AbstractEntity<?>>) t2._1);
                            localIndex = t2._2;
                        } else if (currentHibType != null){ // composites
                            final T2<ValueTree, Integer> t2 = buildValueTree(currentHibType, currentGroupDetails, localIndex);
                            compositeValues.put(currentGroup, t2._1);
                            localIndex = t2._2;
                        } else {
                            throw new IllegalStateException("Incorrect state 1: " + prop._1);
                        }

                        // "restart" current group
                        currentResultType = null;
                        currentHibType = null;
                        currentGroupDetails = new ArrayList<>();
                    }

                    currentGroup = propGroup;

                    if (resultType == EntityAggregates.class) {
                        currentResultType = EntityAggregates.class;
                        currentGroupDetails.add(T2.t2(prop._1.substring(firstDotIndex + 1), prop._2)); 
                    } else {
                        final AbstractPropInfo<?> propInfo = md.getEntityInfo(resultType).getProps().get(currentGroup);
                        if (propInfo != null) {
                            if (EntityUtils.isEntityType(propInfo.javaType())) {
                                currentResultType = (Class<? extends AbstractEntity<?>>) propInfo.javaType();
                                currentGroupDetails.add(T2.t2(prop._1.substring(firstDotIndex + 1), prop._2)); 
                            } else {
                                currentHibType = (ICompositeUserTypeInstantiate) propInfo.hibType;
                                currentGroupDetails.add(T2.t2(prop._1.substring(firstDotIndex + 1), prop._2));
                            }
                        } else {
                            throw new IllegalStateException("Can't find prop metadata: " + prop._1);
                        }
                    } 
                }
                
            } else {
                if (currentGroup != null) {
                    // finalise last group data
                    if (currentResultType != null) {
                        final T2<?, Integer> t2 = build(currentResultType, currentGroupDetails, localIndex, md);
                        entities.put(currentGroup, (EntityTree<? extends AbstractEntity<?>>) t2._1);
                        localIndex = t2._2;
                    } else if (currentHibType != null){ // composites
                        final T2<ValueTree, Integer> t2 = buildValueTree(currentHibType, currentGroupDetails, localIndex);
                        compositeValues.put(currentGroup, t2._1);
                        localIndex = t2._2;
                    } else {
                        throw new IllegalStateException("Incorrect state 2: " + prop._1);
                    }

                    // "restart" current group
                    currentResultType = null;
                    currentHibType = null;
                    currentGroupDetails = new ArrayList<>();

                }

                currentGroup = prop._1;
                
                // can be either ET prop, or primitive prop
                if (EntityUtils.isPersistedEntityType(prop._2.javaType)) {
                    currentResultType = (Class<? extends AbstractEntity<?>>) prop._2.javaType;
                    currentGroupDetails.add(t2(ID, new ResultQueryYieldDetails(ID, Long.class, prop._2.hibType, prop._2.column)));
                } else {
                    currentGroup = null; // no group is actually created for simple prop
                    localIndex = localIndex + 1;
                    singles.put(localIndex, new YieldDetails(prop._1, prop._2.hibType, prop._2.column));
                }
            }
        }
        
        // finalise last group data
        if (currentGroup != null) { //last prop wasn't simple prop
            if (currentResultType != null) {
                final T2<?, Integer> t2 = build(currentResultType, currentGroupDetails, localIndex, md);
                entities.put(currentGroup, (EntityTree<? extends AbstractEntity<?>>) t2._1);
                localIndex = t2._2;
            } else  if (currentHibType != null) { // composites
                final T2<ValueTree, Integer> t2 = buildValueTree(currentHibType, currentGroupDetails, localIndex);
                compositeValues.put(currentGroup, t2._1);
                localIndex = t2._2;
            } else {
                throw new IllegalStateException("Incorrect state 3: " + currentGroup);
            }
        }
        
        return t2(new EntityTree<>(resultType, singles, entities, compositeValues), localIndex);
    }

    private static T2<ValueTree, Integer> buildValueTree(
            final ICompositeUserTypeInstantiate hibType,
            final List<T2<String, ResultQueryYieldDetails>> properties,
            final Integer initialIndex) {
        
        final Map<Integer, YieldDetails> singles = new HashMap<>();
        Integer localIndex = initialIndex;
        
        for (final T2<String, ResultQueryYieldDetails> prop : properties) {
            localIndex = localIndex + 1;
            singles.put(localIndex, new YieldDetails(prop._1, prop._2.hibType, prop._2.column));
        }
        
        return T2.t2(new ValueTree(hibType, singles), localIndex);
    }
}