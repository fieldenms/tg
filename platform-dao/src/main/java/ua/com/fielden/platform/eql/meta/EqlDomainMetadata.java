package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.eql.meta.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.eql.meta.utils.TopologicalSort.sortTopologically;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation.TableStructForBatchInsertion;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.query.AbstractPropInfo;
import ua.com.fielden.platform.eql.meta.query.ComponentTypePropInfo;
import ua.com.fielden.platform.eql.meta.query.EntityTypePropInfo;
import ua.com.fielden.platform.eql.meta.query.PrimTypePropInfo;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.meta.query.UnionTypePropInfo;
import ua.com.fielden.platform.eql.meta.utils.DependentCalcPropsOrder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage2.operands.queries.SourceQuery2;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

public class EqlDomainMetadata {

    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> querySourceInfoMap;
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, List<SourceQuery1>> seModels;
    private final ConcurrentMap<String, List<String>> entityTypesDependentCalcPropsOrder = new ConcurrentHashMap<>();
    private final EntQueryGenerator gen;
    public final EqlEntityMetadataHolder entityMetadataHolder;
    public final DbVersion dbVersion;

    public EqlDomainMetadata(//
            final Map<Class<?>, Class<?>> hibTypesDefaults, //
            final Injector hibTypesInjector, //
            final List<Class<? extends AbstractEntity<?>>> entityTypes, //
            final DbVersion dbVersion) {
        this.dbVersion = dbVersion;
        this.entityMetadataHolder = new EqlEntityMetadataHolder(entityTypes, new EqlEntityMetadataGenerator(hibTypesDefaults, hibTypesInjector, dbVersion));
        this.seModels = new ConcurrentHashMap<>(entityTypes.size());
        this.gen = new EntQueryGenerator(null, null, null, emptyMap());

        querySourceInfoMap = entityMetadataHolder.entityPropsMetadata().entrySet().stream().collect(Collectors.toConcurrentMap(k -> k.getKey(), k -> new QuerySourceInfo<>(k.getKey(), true))); 
        querySourceInfoMap.values().stream().forEach(ei -> addProps(ei, querySourceInfoMap, entityMetadataHolder.entityPropsMetadata().get(ei.javaType()).props()));
        
        // generating models and dependencies info for SE types (UE types also as they are implicit SE types)
        final Map<Class<? extends AbstractEntity<?>>, Set<Class<? extends AbstractEntity<?>>>> seDependencies = new HashMap<>();
        for (final EqlEntityMetadata<?> el : entityMetadataHolder.entityPropsMetadata().values()) {
            if (el.typeInfo.category == QUERY_BASED) {
                final T2<List<SourceQuery1>, Set<Class<? extends AbstractEntity<?>>>> res = generateModelsAndDependenciesForSyntheticType(el.typeInfo);
                seModels.put(el.entityType, res._1);
                seDependencies.put(el.entityType, res._2.stream().filter(cl -> EntityUtils.isSyntheticEntityType(cl)).collect(Collectors.toSet()));
            }
        }

        for (final Class<? extends AbstractEntity<?>> seType : sortTopologically(seDependencies)) {
            try {
                final QuerySourceInfo<? extends AbstractEntity<?>> enhancedQuerySourceInfo = generateEnhancedQuerySourceInfoForSyntheticType(seType, seModels.get(seType));
                querySourceInfoMap.put(enhancedQuerySourceInfo.javaType(), enhancedQuerySourceInfo);
            } catch (final Exception e) {
                e.printStackTrace();
                throw new EqlMetadataGenerationException("Couldn't generate enhanced entity info for synthetic entity [" + seType + "] due to: " + e);
            }
        }

        for (final QuerySourceInfo<?> querySourceInfo : querySourceInfoMap.values()) {
            entityTypesDependentCalcPropsOrder.put(querySourceInfo.javaType().getName(), DependentCalcPropsOrder.orderDependentCalcProps(this, gen, querySourceInfo));
        }
    }
    
    private <T extends AbstractEntity<?>> T2<List<SourceQuery1>, Set<Class<? extends AbstractEntity<?>>>> generateModelsAndDependenciesForSyntheticType(final EntityTypeInfo<T> entityTypeInfo) {
        final List<SourceQuery1> queries = entityTypeInfo.entityModels.stream().map(model -> gen.generateAsUncorrelatedSourceQuery(model)).collect(toList());
        final Set<Class<? extends AbstractEntity<?>>> dependencies = queries.stream().map(qry -> qry.collectEntityTypes()).flatMap(Set::stream).collect(Collectors.toSet());
        return T2.t2(queries, dependencies);
    }

    /**
     * Only properties that are present in SE yields are preserved.
     * 
     * @param parentInfo
     * @return
     */
    private <T extends AbstractEntity<?>> QuerySourceInfo<?> generateEnhancedQuerySourceInfoForSyntheticType(final Class<? extends AbstractEntity<?>> actualType, final List<SourceQuery1> queries) {
        final TransformationContext1 context = new TransformationContext1(this);
        final List<SourceQuery2> transformedQueries = queries.stream().map(m -> m.transform(context)).collect(toList());
        return Source1BasedOnSubqueries.produceQuerySourceInfoForEntityType(this, transformedQueries, actualType, true);
    }

    public List<String> getCalcPropsOrder(final Class<? extends AbstractEntity<?>> entityType) {
        // it's assumed that there will be no generated types with newly added dependent calc props
        return entityTypesDependentCalcPropsOrder.get(DynamicEntityClassLoader.getOriginalType(entityType).getName());
    }
     
    private <T extends AbstractEntity<?>> void addProps(final QuerySourceInfo<T> querySourceInfo, final Map<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> allEntitiesInfo, final Collection<EqlPropertyMetadata> entityPropsMetadatas) {
        for (final EqlPropertyMetadata el : entityPropsMetadatas) {
            if (!el.critOnly) {
                final String name = el.name;
                final Class<?> javaType = el.javaType;
                final Object hibType = el.hibType;
                final ExpressionModel expr = el.expressionModel;

                if (isUnionEntityType(javaType)) {
                    final SortedMap<String, AbstractPropInfo<?>> subprops = new TreeMap<>();
                    for (final EqlPropertyMetadata sub : el.subitems()) {
                        if (sub.expressionModel == null) {
                            subprops.put(sub.name, new EntityTypePropInfo<>(sub.name, allEntitiesInfo.get(sub.javaType), sub.hibType, false, null, sub.implicit));
                        } else {
                            final ExpressionModel subExpr = sub.expressionModel;
                            if (EntityUtils.isEntityType(sub.javaType)) {
                                subprops.put(sub.name, new EntityTypePropInfo<>(sub.name, allEntitiesInfo.get(sub.javaType), sub.hibType, false, subExpr, sub.implicit));
                            } else {
                                subprops.put(sub.name, new PrimTypePropInfo<>(sub.name, sub.javaType, sub.hibType, subExpr, sub.implicit));
                            }
                        }
                    }
                    querySourceInfo.addProp(new UnionTypePropInfo(name, javaType, hibType, subprops));
                } else if (isPersistedEntityType(javaType)) {
                    querySourceInfo.addProp(new EntityTypePropInfo<>(name, allEntitiesInfo.get(javaType), hibType, el.required, expr, el.implicit));
                    //                } else if (ID.equals(name)){
                    //                    querySourceInfo.addProp(new EntityTypePropInfo(name, allEntitiesInfo.get(querySourceInfo.javaType()), hibType, required, expr));
                } else {
                    if (el.subitems().isEmpty()) {
                        querySourceInfo.addProp(new PrimTypePropInfo<>(name, javaType, hibType, expr, el.implicit));
                    } else {
                        final ComponentTypePropInfo<?> propTpi = new ComponentTypePropInfo<>(name, javaType, hibType);
                        for (final EqlPropertyMetadata sub : el.subitems()) {
                            final ExpressionModel subExpr = sub.expressionModel;
                            propTpi.addProp(new PrimTypePropInfo<>(sub.name, sub.javaType, sub.hibType, subExpr, sub.implicit));
                        }
                        querySourceInfo.addProp(propTpi);
                    }
                }
            }
        }
    }

    public TableStructForBatchInsertion getTableForEntityType(final Class<? extends AbstractEntity<?>> entityType) {
        return entityMetadataHolder.getTableStructsForBatchInsertion(entityType);
    }

    public Map<Class<? extends AbstractEntity<?>>, EqlEntityMetadata<?>> entityPropsMetadata() {
        return entityMetadataHolder.entityPropsMetadata();
    }

    public QuerySourceInfo<?> getQuerySourceInfo(final Class<? extends AbstractEntity<?>> type) {
        final QuerySourceInfo<?> existing = querySourceInfoMap.get(type);
        if (existing != null) {
            return existing;
        }
        
        final QuerySourceInfo<?> created = new QuerySourceInfo<>(type, true);
        final List<EqlPropertyMetadata> propsMetadatas = entityMetadataHolder.obtainEqlEntityMetadata(type).props();
        addProps(created, querySourceInfoMap, propsMetadatas);

        return created;
    }

    public QuerySourceInfo<?> getEnhancedQuerySourceInfo(final Class<? extends AbstractEntity<?>> type) {
        final QuerySourceInfo<?> existing = querySourceInfoMap.get(type);
        if (existing != null) {
            return existing;
        }
        
        final EntityTypeInfo<?> eti = getEntityTypeInfo(type);
        if (eti.category == QUERY_BASED) {
            return generateEnhancedQuerySourceInfoForSyntheticType(type, seModels.get(DynamicEntityClassLoader.getOriginalType(type)));
        } else {
            return getQuerySourceInfo(type);
        }
    }
}