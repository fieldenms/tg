package ua.com.fielden.platform.eql.meta;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTENT;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PURE;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.UNION;
import static ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.eql.meta.DomainMetadataUtils.getOriginalEntityTypeFullName;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.generateTable;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.generateTableWithPropColumnInfo;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.utils.DependentCalcPropsOrder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage1.sources.YieldInfoNode;
import ua.com.fielden.platform.eql.stage1.sources.YieldInfoNodesGenerator;
import ua.com.fielden.platform.eql.stage2.etc.Yields2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.sources.Source2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.sources.enhance.PathsToTreeTransformer;
import ua.com.fielden.platform.eql.stage3.Table;
import ua.com.fielden.platform.utils.EntityUtils;

public class EqlDomainMetadata {

    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EqlEntityMetadata> entityPropsMetadata;
    private final ConcurrentMap<String, Table> tables = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TableStructForBatchInsertion> tableStructsForBatchInsertion = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo;
    private final ConcurrentMap<String, List<String>> entityTypesDependentCalcPropsOrder = new ConcurrentHashMap<>();
    private final EntQueryGenerator gen;
    private final EqlEntityMetadataGenerator eemg;
    public final DbVersion dbVersion;

    public EqlDomainMetadata(//
            final Map<Class<?>, Class<?>> hibTypesDefaults, //
            final Injector hibTypesInjector, //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final DbVersion dbVersion) {
        this.dbVersion = dbVersion;
        this.eemg = new EqlEntityMetadataGenerator(hibTypesDefaults, hibTypesInjector, entityTypes, dbVersion);
        this.entityPropsMetadata = new ConcurrentHashMap<>(entityTypes.size());
        this.gen = new EntQueryGenerator(null, null, null, emptyMap());

        entityTypes.parallelStream().forEach(entityType -> {
            try {
                final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo = getEntityTypeInfo(entityType);
                if (parentInfo.category != PURE) {
                    final EqlEntityMetadata em = eemg.generate(parentInfo, entityType);
                    entityPropsMetadata.put(entityType, em);
                    if (parentInfo.category == PERSISTENT) {
                        tables.put(entityType.getName(), generateTable(parentInfo.tableName, em.propsList()));
                        tableStructsForBatchInsertion.put(entityType.getName(), generateTableWithPropColumnInfo(parentInfo.tableName, em.propsList()));
                    }
                }
            } catch (final Exception ex) {
                throw new EqlMetadataGenerationException("Couldn't generate persistence metadata for entity [" + entityType + "].", ex);
            }
        });

        domainInfo = entityPropsMetadata.entrySet().stream().collect(Collectors.toConcurrentMap(k -> k.getKey(), k -> new EntityInfo<>(k.getKey(), k.getValue().typeInfo.category))); 
        domainInfo.values().stream().forEach(ei -> addProps(ei, domainInfo, entityPropsMetadata.get(ei.javaType()).props()));

        for (final EqlEntityMetadata el : entityPropsMetadata.values()) {
            if (el.typeInfo.category == QUERY_BASED) {
                try {
                    final EntityInfo<? extends AbstractEntity<?>> enhancedEntityInfo = generateEnhancedEntityInfoForSyntheticType(el.typeInfo, el.typeInfo.entityType);
                    domainInfo.put(enhancedEntityInfo.javaType(), enhancedEntityInfo);
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new EqlMetadataGenerationException("Couldn't generate enhanced entity info for synthetic entity [" + el.typeInfo.entityType + "] due to: " + e);
                }
            }
        }

        for (final EntityInfo<?> entityInfo : domainInfo.values()) {
            if (entityInfo.getCategory() != UNION) {
                entityTypesDependentCalcPropsOrder.put(entityInfo.javaType().getName(), DependentCalcPropsOrder.orderDependentCalcProps(this, gen, entityInfo));
            }
        }
        
        validateCalcProps();
    }

    /**
     * Only properties that are present in SE yields are preserved.
     * 
     * @param parentInfo
     * @return
     */
    private <T extends AbstractEntity<?>> EntityInfo<?> generateEnhancedEntityInfoForSyntheticType(final EntityTypeInfo<T> parentInfo, final Class<? extends AbstractEntity<?>> actualType) {
        final TransformationContext1 context = new TransformationContext1(this);
        final Yields2 yields = gen.generateAsSyntheticEntityQuery(parentInfo.entityModels.get(0), parentInfo.entityType).transform(context).yields;
        final Map<String, YieldInfoNode> yieldInfoNodes = YieldInfoNodesGenerator.generate(yields.getYields());
        return Source1BasedOnSubqueries.produceEntityInfoForDefinedEntityType(this, yieldInfoNodes, actualType/*parentInfo.entityType*/);
    }

    private void validateCalcProps() {
        final PathsToTreeTransformer p2tt = new PathsToTreeTransformer(this, gen);
        for (final EntityInfo<?> et : domainInfo.values()) {
            if (et.getCategory() != UNION) {
                final Source2BasedOnPersistentType source = new Source2BasedOnPersistentType(et.javaType(), et, gen.nextSourceId() /*"dummy_id"*/); //TODO analyze
                for (final AbstractPropInfo<?> prop : et.getProps().values()) {
                    if (prop.expression != null && !prop.name.equals(KEY)) {
                       try {
                            p2tt.transform(setOf(new Prop2(source, asList(prop))));    
                        } catch (final Exception e) {
                            throw new EqlException("There is an error in expression of calculated property [" + et.javaType().getSimpleName() + ":" + prop.name + "]: " + e.getMessage());
                        }
                    } else if (prop.hasExpression() && prop instanceof ComponentTypePropInfo) {
                        for (final AbstractPropInfo<?> subprop : ((ComponentTypePropInfo<?>) prop).getProps().values()) {
                            if (subprop.expression != null) {
                                try {
                                    p2tt.transform(setOf(new Prop2(source, asList(prop, subprop))));    
                                } catch (final Exception e) {
                                    throw new EqlException("There is an error in expression of calculated property [" + et.javaType().getSimpleName() + ":" + prop.name  + "." + subprop.name + "]: " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public List<String> getCalcPropsOrder(final String entityTypeName) {
        // it's assumed that there will be no generated types with newly added dependent calc props
        return entityTypesDependentCalcPropsOrder.get(getOriginalEntityTypeFullName(entityTypeName));
    }
     
    private <T extends AbstractEntity<?>> void addProps(final EntityInfo<T> entityInfo, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> allEntitiesInfo, final Collection<EqlPropertyMetadata> entityPropsMetadatas) {
        for (final EqlPropertyMetadata el : entityPropsMetadatas) {
            if (!el.critOnly) {
                final String name = el.name;
                final Class<?> javaType = el.javaType;
                final Object hibType = el.hibType;
                final ExpressionModel expr = el.expressionModel;

                if (isUnionEntityType(javaType)) {
                    final EntityInfo<? extends AbstractUnionEntity> ef = new EntityInfo<>((Class<? extends AbstractUnionEntity>) javaType, UNION);
                    for (final EqlPropertyMetadata sub : el.subitems()) {
                        if (sub.expressionModel == null) {
                            ef.addProp(new EntityTypePropInfo(sub.name, allEntitiesInfo.get(sub.javaType), sub.hibType, false, null, sub.implicit));
                        } else {
                            final ExpressionModel subExpr = sub.expressionModel;
                            if (EntityUtils.isEntityType(sub.javaType)) {
                                ef.addProp(new EntityTypePropInfo(sub.name, allEntitiesInfo.get(sub.javaType), sub.hibType, false, subExpr, sub.implicit));
                            } else {
                                ef.addProp(new PrimTypePropInfo(sub.name, sub.hibType, sub.javaType, subExpr, sub.implicit));
                            }

                        }
                    }
                    entityInfo.addProp(new UnionTypePropInfo(name, ef, hibType, false));
                } else if (isPersistedEntityType(javaType)) {
                    entityInfo.addProp(new EntityTypePropInfo(name, allEntitiesInfo.get(javaType), hibType, el.required, expr, el.implicit));
                    //                } else if (ID.equals(name)){
                    //                    entityInfo.addProp(new EntityTypePropInfo(name, allEntitiesInfo.get(entityInfo.javaType()), hibType, required, expr));
                } else {
                    if (el.subitems().isEmpty()) {
                        entityInfo.addProp(new PrimTypePropInfo(name, hibType, javaType, expr, el.implicit));
                    } else {
                        final ComponentTypePropInfo propTpi = new ComponentTypePropInfo(name, javaType, hibType);
                        for (final EqlPropertyMetadata sub : el.subitems()) {
                            final ExpressionModel subExpr = sub.expressionModel;
                            propTpi.addProp(new PrimTypePropInfo(sub.name, sub.hibType, sub.javaType, subExpr, sub.implicit));
                        }
                        entityInfo.addProp(propTpi);
                    }
                }
            }
        }
    }

    public Map<String, Table> getTables() {
        return unmodifiableMap(tables);
    }

    public TableStructForBatchInsertion getTableForEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        return tableStructsForBatchInsertion.get(entityType.getName());
    }

    public Map<Class<? extends AbstractEntity<?>>, EqlEntityMetadata> entityPropsMetadata() {
        return unmodifiableMap(entityPropsMetadata);
    }

    public EntityInfo<?> getEntityInfo(final Class<? extends AbstractEntity<?>> type) {
        final EntityInfo<?> existing = domainInfo.get(type);
        if (existing != null) {
            return existing;
        }
        final EntityTypeInfo<?> eti = getEntityTypeInfo(type);
        final List<EqlPropertyMetadata> propsMetadatas = eemg.generate(eti, type).propsList();
        //entityPropsMetadata.put(type, t2(eti.category, propsMetadatas));
        final EntityInfo<?> created = new EntityInfo<>(type, eti.category);
        //domainInfo.put(type, created);
        addProps(created, domainInfo, propsMetadatas);

        return created;
    }

    public EntityInfo<?> getEnhancedEntityInfo(final Class<? extends AbstractEntity<?>> type) {
        final EntityInfo<?> existing = domainInfo.get(type);
        if (existing != null) {
            return existing;
        }

        final EntityTypeInfo<?> eti = getEntityTypeInfo(type);
        if (eti.category == QUERY_BASED) {
            return generateEnhancedEntityInfoForSyntheticType(eti, type);
        } else {
            return getEntityInfo(type);
        }
    }

}