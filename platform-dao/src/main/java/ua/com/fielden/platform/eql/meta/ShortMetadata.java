package ua.com.fielden.platform.eql.meta;

import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.UNION;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.metadata.EntityCategory;
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.builders.StandAloneExpressionBuilder;
import ua.com.fielden.platform.eql.stage1.elements.operands.Expression1;
import ua.com.fielden.platform.utils.IDates;

public class ShortMetadata {
    private final LongMetadata lmg;
    private final IDates dates;
    private final IFilter filter;
    private final String username;
    private final Map<String, Object> paramValues = new HashMap<>();

    public ShortMetadata(final LongMetadata lmg, final IFilter filter, final String username, final IDates dates, final Map<String, Object> paramValues) {
        this.lmg = lmg;
        this.filter = filter;
        this.dates = dates;
        this.username = username;
        this.paramValues.putAll(paramValues);
    }
    
    protected final EntQueryGenerator qb() {
        return new EntQueryGenerator(lmg.dbVersion, filter, username, dates, paramValues);
    }

    public final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> generate(final Set<Class<? extends AbstractEntity<?>>> entities) throws Exception {
        final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> result = entities.stream()
                .filter(t -> isPersistedEntityType(t) || isSyntheticEntityType(t) || isSyntheticBasedOnPersistentEntityType(t)/* || isUnionEntityType(t)*/)
                .collect(toMap(k -> k, k -> new EntityInfo<>(k, determineCategory(k))));
        result.values().stream().forEach(ei -> addProps(ei, result));
        return result;
    }
    
    private <ET extends AbstractEntity<?>> EntityCategory determineCategory(final Class<ET> entityType) {
        final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo = new EntityTypeInfo<>(entityType);
        return parentInfo.category;
    }

    private <T extends AbstractEntity<?>> void addProps(final EntityInfo<T> entityInfo, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> allEntitiesInfo) {
        try {
            for (final Entry<String, LongPropertyMetadata> el : lmg.getEntityPropsMetadata().get(entityInfo.javaType()).entrySet()) {
                final String name = el.getKey();
                final boolean required = !el.getValue().nullable;
                final Class<?> javaType = el.getValue().javaType;
                final Object hibType = el.getValue().hibType;
                final ExpressionModel expressionModel = el.getValue().expressionModel;
                final Expression1 expr = expressionModel == null ? null : (Expression1) (new StandAloneExpressionBuilder(qb(), expressionModel)).getResult().getValue();
                
                
                if (isUnionEntityType(javaType)) {
                    final EntityInfo<? extends AbstractUnionEntity> ef = new EntityInfo<>((Class<? extends AbstractUnionEntity>)javaType, UNION);
                    for (final LongPropertyMetadata sub : el.getValue().subitems()) {
                        if (sub.expressionModel == null) {
                            ef.addProp(new EntityTypePropInfo(sub.name, allEntitiesInfo.get(sub.javaType), sub.hibType, false, null));
                        } else {
                            final ExpressionModel subExpressionModel = sub.expressionModel;
                            final Expression1 subExpr = subExpressionModel == null ? null : (Expression1) (new StandAloneExpressionBuilder(qb(), subExpressionModel)).getResult().getValue();
                            ef.addProp(new PrimTypePropInfo(sub.name, sub.hibType, sub.javaType, subExpr));
                        }
                    }
                    entityInfo.addProp(new UnionTypePropInfo(name, ef, hibType, required));
                } else if (isPersistedEntityType(javaType)) {
                    entityInfo.addProp(new EntityTypePropInfo(name, allEntitiesInfo.get(javaType), hibType, required, expr));
                } else if (ID.equals(name)){
                    entityInfo.addProp(new EntityTypePropInfo(name, allEntitiesInfo.get(entityInfo.javaType()), hibType, required, expr));
                } else {
                    if (el.getValue().subitems().isEmpty()) {
                        entityInfo.addProp(new PrimTypePropInfo(name, hibType, javaType, expr));    
                    } else {
                        final ComponentTypePropInfo propTpi = new ComponentTypePropInfo(name, javaType, hibType);
                        for (final LongPropertyMetadata sub : el.getValue().subitems()) {
                            final ExpressionModel subExpressionModel = sub.expressionModel;
                            final Expression1 subExpr = subExpressionModel == null ? null : (Expression1) (new StandAloneExpressionBuilder(qb(), subExpressionModel)).getResult().getValue();
                            propTpi.addProp(new PrimTypePropInfo(sub.name, sub.hibType, sub.javaType, subExpr));
                        }
                        entityInfo.addProp(propTpi);
                    }
                }
            }
        } catch (final Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}