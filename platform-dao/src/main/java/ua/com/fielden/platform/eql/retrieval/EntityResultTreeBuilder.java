package ua.com.fielden.platform.eql.retrieval;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.eql.meta.PropType.LONG_PROP_TYPE;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.type.BigDecimalType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.query.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.query.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.retrieval.records.EntityTree;
import ua.com.fielden.platform.eql.retrieval.records.HibernateScalar;
import ua.com.fielden.platform.eql.retrieval.records.QueryResultLeaf;
import ua.com.fielden.platform.eql.retrieval.records.ValueTree;
import ua.com.fielden.platform.eql.retrieval.records.YieldedColumn;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.utils.EntityUtils;

public final class EntityResultTreeBuilder {
    public static <E extends AbstractEntity<?>> EntityTree<E> build(final Class<E> resultType, final List<YieldedColumn> sortedYields, final EqlDomainMetadata md) {
        return buildEntityTree(resultType, sortedYields, -1, md).tree();
    }
    
    private static <E extends AbstractEntity<?>> EntityTreeResult<E> buildEntityTree(
            final Class<E> resultType, 
            final List<YieldedColumn> yieldedColumns, 
            final Integer initialIndex,
            final EqlDomainMetadata md) {
        
        final List<QueryResultLeaf> leaves = new ArrayList<>();
        final Map<String /*composite property name*/, EntityTree<? extends AbstractEntity<?>>> entityTrees = new HashMap<>();
        final Map<String /*composite value property name*/, ValueTree> valueTrees = new HashMap<>();
        
        Integer localIndex = initialIndex;
        
        String currentGroup = null;
        Class<? extends AbstractEntity<?>> currentResultType = null;
        ComponentTypePropInfo<?> currentComponentInfo = null;
        List<YieldedColumn> currentGroupDetails = new ArrayList<>();
        final QuerySourceInfo<?> querySourceInfo = resultType.equals(EntityAggregates.class) ? null : md.getQuerySourceInfo(resultType);
        
        for (final YieldedColumn yc : yieldedColumns) {
            if (yc.name().contains(".")) {
                final int firstDotIndex = yc.name().indexOf(".");
                final String propGroup = yc.name().substring(0, firstDotIndex);
                final String remainingProp = yc.name().substring(firstDotIndex + 1);
                
                if (propGroup.equals(currentGroup)) {
                    currentGroupDetails.add(new YieldedColumn(remainingProp, yc.propType(), yc.column()));
                } else { // no explicit group header -- either composite value type or EA in SE result yields 
                    if (currentGroup != null) {
                        // finalise current group
                        if (currentResultType != null) {
                            final var entityTreeResult = buildEntityTree(currentResultType, currentGroupDetails, localIndex, md);
                            entityTrees.put(currentGroup, entityTreeResult.tree());
                            localIndex = entityTreeResult.updatedIndex();
                        } else if (currentComponentInfo != null){ // composites
                            final var valueTreeResult = buildValueTree(currentComponentInfo, currentGroupDetails, localIndex);
                            valueTrees.put(currentGroup, valueTreeResult.tree());
                            localIndex = valueTreeResult.updatedIndex();
                        } else {
                            throw new IllegalStateException("Incorrect state 1: " + yc.name());
                        }

                        // "restart" current group
                        currentResultType = null;
                        currentComponentInfo = null;
                        currentGroupDetails = new ArrayList<>();
                    }

                    currentGroup = propGroup;

                    if (resultType == EntityAggregates.class) {
                        currentResultType = EntityAggregates.class;
                        currentGroupDetails.add(new YieldedColumn(remainingProp, yc.propType(), yc.column())); 
                    } else {
                        final AbstractPropInfo<?> propInfo = querySourceInfo.getProps().get(currentGroup);
                        if (propInfo != null) {
                            if (EntityUtils.isEntityType(propInfo.javaType())) {
                                currentResultType = (Class<? extends AbstractEntity<?>>) propInfo.javaType();
                                currentGroupDetails.add(new YieldedColumn(remainingProp, yc.propType(), yc.column())); 
                            } else {
                                currentComponentInfo = (ComponentTypePropInfo<?>) propInfo;
                                currentGroupDetails.add(new YieldedColumn(remainingProp, yc.propType(), yc.column()));
                            }
                        } else {
                            throw new IllegalStateException("Can't find prop metadata: " + yc.name());
                        }
                    } 
                }
                
            } else {
                if (currentGroup != null) {
                    // finalise last group data
                    if (currentResultType != null) {
                        final var t2 = buildEntityTree(currentResultType, currentGroupDetails, localIndex, md);
                        entityTrees.put(currentGroup, t2.tree());
                        localIndex = t2.updatedIndex();
                    } else if (currentComponentInfo != null){ // composites
                        final ValueTreeResult t2 = buildValueTree(currentComponentInfo, currentGroupDetails, localIndex);
                        valueTrees.put(currentGroup, t2.tree());
                        localIndex = t2.updatedIndex();
                    } else {
                        throw new IllegalStateException("Incorrect state 2: " + yc.name());
                    }

                    // "restart" current group
                    currentResultType = null;
                    currentComponentInfo = null;
                    currentGroupDetails = new ArrayList<>();

                }

                currentGroup = yc.name();
                
                // can be either ET prop, or primitive prop
                if (yc.propType() != null && EntityUtils.isPersistedEntityType(yc.propType().javaType())) {
                    currentResultType = (Class<? extends AbstractEntity<?>>) yc.propType().javaType();
                    currentGroupDetails.add(new YieldedColumn(ID, LONG_PROP_TYPE, yc.column()));
                } else {
                    currentGroup = null; // no group is actually created for simple prop
                    localIndex = localIndex + 1;
                    
                    if (querySourceInfo == null) { // the case of EntityAggregates
                        final Object derivedHibType = yc.propType() == null ? null : yc.propType().hibType(); // taking actual original prop hibType (if available)
                        leaves.add(new QueryResultLeaf(localIndex, yc.name(), new HibernateScalar(yc.column(), getHibTypeAsType(derivedHibType)), getHibTypeAsUserType(derivedHibType)));
                    } else { 
                        final AbstractPropInfo<?> propInfo = querySourceInfo.getProps().get(yc.name());
                        if (propInfo != null) {
                            final Object declaredHibType = propInfo.hibType;
                            leaves.add(new QueryResultLeaf(localIndex, yc.name(), new HibernateScalar(yc.column(), getHibTypeAsType(declaredHibType)), getHibTypeAsUserType(declaredHibType)));
                        } else {
                            final Object deducedHibType = yc.propType() == null ? null : hibTypeFromJavaType(yc.propType().javaType());
                            leaves.add(new QueryResultLeaf(localIndex, yc.name(), new HibernateScalar(yc.column(), getHibTypeAsType(deducedHibType)), getHibTypeAsUserType(deducedHibType)));
                        }
                    }
                }
            }
        }
        
        // finalise last group data
        if (currentGroup != null) { //last prop wasn't simple prop
            if (currentResultType != null) {
                final var entityTreeResult = buildEntityTree(currentResultType, currentGroupDetails, localIndex, md);
                entityTrees.put(currentGroup, entityTreeResult.tree());
                localIndex = entityTreeResult.updatedIndex();
            } else  if (currentComponentInfo != null) { // composites
                final ValueTreeResult valueTreeResult = buildValueTree(currentComponentInfo, currentGroupDetails, localIndex);
                valueTrees.put(currentGroup, valueTreeResult.tree());
                localIndex = valueTreeResult.updatedIndex();
            } else {
                throw new IllegalStateException("Incorrect state 3: " + currentGroup);
            }
        }
        
        return new EntityTreeResult<E>(new EntityTree<E>(resultType, unmodifiableList(leaves), unmodifiableMap(entityTrees), unmodifiableMap(valueTrees)), localIndex);
    }

    private static ValueTreeResult buildValueTree(
            final ComponentTypePropInfo<?> propInfo,
            final List<YieldedColumn> properties,
            final Integer initialIndex) {
        
        final List<QueryResultLeaf> singles = new ArrayList<>();
        Integer localIndex = initialIndex;
        
        for (final YieldedColumn prop : properties) {
            localIndex = localIndex + 1;
            final Object declaredHibType = propInfo.getProps().get(prop.name()).hibType;
            singles.add(new QueryResultLeaf(localIndex, prop.name(), new HibernateScalar(prop.column(), getHibTypeAsType(declaredHibType)), getHibTypeAsUserType(declaredHibType)));
        }
        
        return new ValueTreeResult(new ValueTree((ICompositeUserTypeInstantiate) propInfo.hibType, unmodifiableList(singles)), localIndex);
    }
    
    public static Object hibTypeFromJavaType(final Class<?> type) {
        // TODO need to have the same logic as in EqlEntityMetadataGenerator.getHibernateType (i.e. use hibTypeDefaults)
        
        if (Date.class.equals(type)) {
            return DateTimeType.INSTANCE; 
        } else if (BigDecimal.class.equals(type)) {
            return BigDecimalType.INSTANCE; 
        } else if (Long.class.equals(type)) {
            return LongType.INSTANCE; 
        } else if (Integer.class.equals(type) || int.class.equals(type)){
            return IntegerType.INSTANCE; 
        } else if (String.class.equals(type)){
            return StringType.INSTANCE; 
        } else if (isEntityType(type)) {
                return LongType.INSTANCE; 
        } else if (type == boolean.class || Boolean.class.equals(type)){
                return YesNoType.INSTANCE;
        } else {
            return null;
        }
    }
    
    private static IUserTypeInstantiate getHibTypeAsUserType(final Object hibType) {
        return hibType instanceof IUserTypeInstantiate ? (IUserTypeInstantiate) hibType : null;
    }
    
    private static Type getHibTypeAsType(final Object hibType) {
        return hibType instanceof Type ? (Type) hibType : null;
    }
    
    private record ValueTreeResult(ValueTree tree, int updatedIndex) {}
    
    private record EntityTreeResult<E extends AbstractEntity<?>>(EntityTree<E> tree, int updatedIndex) {}
}