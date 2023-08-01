package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTENT;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PURE;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.generateTable;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.generateTableWithPropColumnInfo;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion;
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.utils.DependentCalcPropsOrder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage2.operands.queries.SourceQuery2;
import ua.com.fielden.platform.eql.stage3.Table;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

public class EqlDomainMetadata {

    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EqlEntityMetadata<?>> entityPropsMetadata;
    private final ConcurrentMap<String, Table> tables = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TableStructForBatchInsertion> tableStructsForBatchInsertion = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, List<SourceQuery1>> seModels;
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
        this.seModels = new ConcurrentHashMap<>(entityTypes.size());
        this.gen = new EntQueryGenerator(null, null, null, emptyMap());

        entityTypes.parallelStream().forEach(entityType -> {
            try {
                final EntityTypeInfo<? extends AbstractEntity<?>> parentInfo = getEntityTypeInfo(entityType);
                if (parentInfo.category != PURE) {
                    final EqlEntityMetadataPair<? extends AbstractEntity<?>> pd = eemg.generate(getEntityTypeInfo(entityType), entityType);
                    entityPropsMetadata.put(entityType, pd.eqlEntityMetadata());
                    if (parentInfo.category == PERSISTENT) {
                        tables.put(entityType.getName(), generateTable(parentInfo.tableName, pd.eqlEntityMetadata().props()));
                        tableStructsForBatchInsertion.put(entityType.getName(), generateTableWithPropColumnInfo(parentInfo.tableName, pd.eqlEntityMetadata().props()));
                    }
                }
            } catch (final Exception ex) {
                throw new EqlMetadataGenerationException("Couldn't generate persistence metadata for entity [" + entityType + "].", ex);
            }
        });

        domainInfo = entityPropsMetadata.entrySet().stream().collect(Collectors.toConcurrentMap(k -> k.getKey(), k -> new EntityInfo<>(k.getKey(), true))); 
        domainInfo.values().stream().forEach(ei -> addProps(ei, domainInfo, entityPropsMetadata.get(ei.javaType()).props()));

        
        // generating models and dependencies info for SE types (UE types also as they are implicit SE types)
        final Map<Class<? extends AbstractEntity<?>>, Set<Class<? extends AbstractEntity<?>>>> seDependencies = new HashMap<>();
        for (final EqlEntityMetadata el : entityPropsMetadata.values()) {
            if (el.typeInfo.category == QUERY_BASED) {
                final T2<List<SourceQuery1>, Set<Class<? extends AbstractEntity<?>>>> res = generateModelsAndDependenciesForSyntheticType(el.typeInfo);
                seModels.put(el.entityType, res._1);
                seDependencies.put(el.entityType, res._2.stream().filter(cl -> EntityUtils.isSyntheticEntityType(cl)).collect(Collectors.toSet()));
            }
        }

        for (Class<? extends AbstractEntity<?>> seType : sortTopologically(seDependencies)) {
            try {
                final EntityInfo<? extends AbstractEntity<?>> enhancedEntityInfo = generateEnhancedEntityInfoForSyntheticType(seType, seModels.get(seType));
                domainInfo.put(enhancedEntityInfo.javaType(), enhancedEntityInfo);
            } catch (final Exception e) {
                e.printStackTrace();
                throw new EqlMetadataGenerationException("Couldn't generate enhanced entity info for synthetic entity [" + seType + "] due to: " + e);
            }
        }

        for (final EntityInfo<?> entityInfo : domainInfo.values()) {
            entityTypesDependentCalcPropsOrder.put(entityInfo.javaType().getName(), DependentCalcPropsOrder.orderDependentCalcProps(this, gen, entityInfo));
        }
    }
    
    private static List<Class<? extends AbstractEntity<?>>> sortTopologically(final Map<Class<? extends AbstractEntity<?>>, Set<Class<? extends AbstractEntity<?>>>> mapOfDependencies) {
        final List<Class<? extends AbstractEntity<?>>> sorted = new ArrayList<>();

        while (!mapOfDependencies.isEmpty()) {
            Class<? extends AbstractEntity<?>> nextSorted = null;
            // let's find the first item without dependencies and regard it as "sorted"
            for (final Entry<Class<? extends AbstractEntity<?>>, Set<Class<? extends AbstractEntity<?>>>> el : mapOfDependencies.entrySet()) {
                if (el.getValue().isEmpty()) {
                    nextSorted = el.getKey();
                    break;
                }
            }

            sorted.add(nextSorted);
            mapOfDependencies.remove(nextSorted); // removing "sorted" item from map of remaining items

            // removing "sorted" item from dependencies of remaining items 
            for (final Entry<Class<? extends AbstractEntity<?>>, Set<Class<? extends AbstractEntity<?>>>> el : mapOfDependencies.entrySet()) {
                el.getValue().remove(nextSorted);
            }
        }

        return sorted;
    }
    
    private <T extends AbstractEntity<?>> T2<List<SourceQuery1>, Set<Class<? extends AbstractEntity<?>>>> generateModelsAndDependenciesForSyntheticType(final EntityTypeInfo<T> parentInfo) {
        final List<SourceQuery1> queries = parentInfo.entityModels.stream().map(model -> gen.generateAsUncorrelatedSourceQuery(model)).collect(toList());
        final Set<Class<? extends AbstractEntity<?>>> result = queries.stream().map(qry -> qry.collectEntityTypes()).flatMap(Set::stream).collect(Collectors.toSet());
        return T2.t2(queries, result);
    }

    /**
     * Only properties that are present in SE yields are preserved.
     * 
     * @param parentInfo
     * @return
     */
    private <T extends AbstractEntity<?>> EntityInfo<?> generateEnhancedEntityInfoForSyntheticType(final Class<? extends AbstractEntity<?>> actualType, final List<SourceQuery1> queries) {
        final TransformationContext1 context = new TransformationContext1(this);
        final List<SourceQuery2> transformedQueries = queries.stream().map(m -> m.transform(context)).collect(toList());
        return Source1BasedOnSubqueries.produceEntityInfoForEntityType(this, transformedQueries, actualType, true);
    }

    public List<String> getCalcPropsOrder(final Class<? extends AbstractEntity<?>> entityType) {
        // it's assumed that there will be no generated types with newly added dependent calc props
        return entityTypesDependentCalcPropsOrder.get(DynamicEntityClassLoader.getOriginalType(entityType).getName());
    }
     
    private <T extends AbstractEntity<?>> void addProps(final EntityInfo<T> entityInfo, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> allEntitiesInfo, final Collection<EqlPropertyMetadata> entityPropsMetadatas) {
        for (final EqlPropertyMetadata el : entityPropsMetadatas) {
            if (!el.critOnly) {
                final String name = el.name;
                final Class<?> javaType = el.javaType;
                final Object hibType = el.hibType;
                final ExpressionModel expr = el.expressionModel;

                if (isUnionEntityType(javaType)) {
                    final EntityInfo<? extends AbstractUnionEntity> ef = new EntityInfo<>((Class<? extends AbstractUnionEntity>) javaType, false); // TODO need to move props to holder and not create EntityInfo for this
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

    public Map<Class<? extends AbstractEntity<?>>, EqlEntityMetadata<?>> entityPropsMetadata() {
        return unmodifiableMap(entityPropsMetadata);
    }

    public EntityInfo<?> getEntityInfo(final Class<? extends AbstractEntity<?>> type) {
        final EntityInfo<?> existing = domainInfo.get(type);
        if (existing != null) {
            return existing;
        }
        final EntityTypeInfo<?> eti = getEntityTypeInfo(type);
        
        final List<EqlPropertyMetadata> propsMetadatas = eemg.generate(getEntityTypeInfo(type), type).eqlEntityMetadata().props();
        //entityPropsMetadata.put(type, t2(eti.category, propsMetadatas));
        final EntityInfo<?> created = new EntityInfo<>(type, true);
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
            return generateEnhancedEntityInfoForSyntheticType(type, seModels.get(DynamicEntityClassLoader.getOriginalType(type)));
        } else {
            return getEntityInfo(type);
        }
    }
}