package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PERSISTENT;
import static ua.com.fielden.platform.eql.meta.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.eql.meta.EntityCategory.UNION;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.eql.meta.utils.TopologicalSort.sortTopologically;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.ArrayList;
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

import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForComponentType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForEntityType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForPrimType;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForUnionType;
import ua.com.fielden.platform.eql.meta.utils.DependentCalcPropsOrder;
import ua.com.fielden.platform.eql.stage0.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.TransformationContext1;
import ua.com.fielden.platform.eql.stage1.operands.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage1.sources.Source1BasedOnQueries;
import ua.com.fielden.platform.eql.stage2.operands.queries.SourceQuery2;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

public class QuerySourceInfoProvider {
    private static final Logger LOGGER = getLogger(QuerySourceInfoProvider.class);
    
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> declaredQuerySourceInfoMap; // by "declared" here it is meant that all (as defined by EqlEntityMetadata) properties are included (even those with no way of getting data from db for them)
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> modelledQuerySourceInfoMap; // by "modelled" here it is meant that only properties with db data are included (table/query column, calculated expression)
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, List<SourceQuery1>> seModels;
    private final ConcurrentMap<String, List<String>> entityTypesDependentCalcPropsOrder = new ConcurrentHashMap<>();
    public final EqlEntityMetadataHolder entityMetadataHolder;

    public QuerySourceInfoProvider(final EqlEntityMetadataHolder entityMetadataHolder) {
        this.entityMetadataHolder = entityMetadataHolder;
        declaredQuerySourceInfoMap = new ConcurrentHashMap<>(entityMetadataHolder.entityPropsMetadata().size());
        modelledQuerySourceInfoMap = new ConcurrentHashMap<>(entityMetadataHolder.entityPropsMetadata().size());
        seModels = new ConcurrentHashMap<>(entityMetadataHolder.entityPropsMetadata().size());
        
        final EntQueryGenerator gen = new EntQueryGenerator(null, null, null, emptyMap());

        declaredQuerySourceInfoMap.putAll(entityMetadataHolder.entityPropsMetadata().entrySet().stream().collect(toConcurrentMap(k -> k.getKey(), k -> new QuerySourceInfo<>(k.getKey(), true)))); 
        declaredQuerySourceInfoMap.values().stream().forEach(ei -> ei.addProps(generateQuerySourceItems(declaredQuerySourceInfoMap, entityMetadataHolder.entityPropsMetadata().get(ei.javaType()).props())));
        
        modelledQuerySourceInfoMap.putAll(entityMetadataHolder.entityPropsMetadata().entrySet().stream().
                filter(e -> e.getValue().typeInfo.category == PERSISTENT || e.getValue().typeInfo.category == UNION).collect(toConcurrentMap(k -> k.getKey(), k -> new QuerySourceInfo<>(k.getKey(), true)))); 
        modelledQuerySourceInfoMap.values().stream().forEach(ei -> ei.addProps(generateQuerySourceItems(modelledQuerySourceInfoMap, entityMetadataHolder.entityPropsMetadata().get(ei.javaType()).props())));

        // generating models and dependencies info for SE types (there is no need to include UE types here as their models are implicitly generated and have no interdependencies)
        final Map<Class<? extends AbstractEntity<?>>, Set<Class<? extends AbstractEntity<?>>>> seDependencies = new HashMap<>();
        for (final EqlEntityMetadata<?> el : entityMetadataHolder.entityPropsMetadata().values()) {
            if (el.typeInfo.category == QUERY_BASED) {
                final T2<List<SourceQuery1>, Set<Class<? extends AbstractEntity<?>>>> res = generateModelsAndDependenciesForSyntheticType(el.typeInfo, gen);
                seModels.put(el.entityType, res._1);
                seDependencies.put(el.entityType, res._2.stream().filter(cl -> isSyntheticEntityType(cl)).collect(Collectors.toSet()));
            }
        }

        for (final Class<? extends AbstractEntity<?>> seType : sortTopologically(seDependencies)) {
            try {
                final QuerySourceInfo<? extends AbstractEntity<?>> modelledQuerySourceInfo = generateModelledQuerySourceInfoForSyntheticType(seType, seModels.get(seType));
                modelledQuerySourceInfoMap.put(modelledQuerySourceInfo.javaType(), modelledQuerySourceInfo);
            } catch (final Exception e) {
                final var msg = "Could not generate modelled entity info for synthetic entity [" + seType + "]";
                LOGGER.error(msg, e);
                throw new EqlMetadataGenerationException(msg);
            }
        }

        for (final QuerySourceInfo<?> querySourceInfo : modelledQuerySourceInfoMap.values()) {
            //if (isPersistedEntityType(querySourceInfo.javaType())) { // currently it's assumed that SE's don't have calc props with interdependencies.
                entityTypesDependentCalcPropsOrder.put(querySourceInfo.javaType().getName(), DependentCalcPropsOrder.orderDependentCalcProps(this, gen, querySourceInfo));
            //}
        }
    }
    
    private static <T extends AbstractEntity<?>> T2<List<SourceQuery1>, Set<Class<? extends AbstractEntity<?>>>> generateModelsAndDependenciesForSyntheticType(final EntityTypeInfo<T> entityTypeInfo, final EntQueryGenerator gen) {
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
    private <T extends AbstractEntity<?>> QuerySourceInfo<?> generateModelledQuerySourceInfoForSyntheticType(final Class<? extends AbstractEntity<?>> entityType, final List<SourceQuery1> queries) {
        final TransformationContext1 context = new TransformationContext1(this, false);
        final List<SourceQuery2> transformedQueries = queries.stream().map(m -> m.transform(context)).collect(toList());
        return Source1BasedOnQueries.produceQuerySourceInfoForEntityType(this, transformedQueries, entityType, true);
    }

    public List<String> getCalcPropsOrder(final Class<? extends AbstractEntity<?>> entityType) {
        // it's assumed that there will be no generated types with newly added dependent calc props
        return entityTypesDependentCalcPropsOrder.get(getOriginalType(entityType).getName());
    }
     
    private List<AbstractQuerySourceItem<?>> generateQuerySourceItems(final Map<Class<? extends AbstractEntity<?>>, QuerySourceInfo<?>> allQuerySourceInfos, final Collection<EqlPropertyMetadata> entityPropsMetadatas) {
        final List<AbstractQuerySourceItem<?>> props = new ArrayList<>();
        for (final EqlPropertyMetadata el : entityPropsMetadatas) {
            if (!el.critOnly) {
                final String name = el.name;
                final Class<?> javaType = el.javaType;
                final Object hibType = el.hibType;
                final ExpressionModel expr = el.expressionModel;

                if (isUnionEntityType(javaType)) {
                    final SortedMap<String, AbstractQuerySourceItem<?>> subprops = new TreeMap<>();
                    for (final EqlPropertyMetadata sub : el.subitems()) {
                        if (sub.expressionModel == null) {
                            subprops.put(sub.name, new QuerySourceItemForEntityType<>(sub.name, allQuerySourceInfos.get(sub.javaType), sub.hibType, false, null, sub.implicit));
                        } else {
                            final ExpressionModel subExpr = sub.expressionModel;
                            if (EntityUtils.isEntityType(sub.javaType)) {
                                subprops.put(sub.name, new QuerySourceItemForEntityType<>(sub.name, allQuerySourceInfos.get(sub.javaType), sub.hibType, false, subExpr, sub.implicit));
                            } else {
                                subprops.put(sub.name, new QuerySourceItemForPrimType<>(sub.name, sub.javaType, sub.hibType, subExpr, sub.implicit));
                            }
                        }
                    }
                    props.add(new QuerySourceItemForUnionType<>(name, ((Class<? extends AbstractUnionEntity>) javaType), hibType, subprops));
                } else if (isPersistedEntityType(javaType)) {
                    props.add(new QuerySourceItemForEntityType<>(name, allQuerySourceInfos.get(javaType), hibType, el.required, expr, el.implicit));
                    //                } else if (ID.equals(name)){
                    //                    querySourceInfo.addProp(new EntityTypePropInfo(name, allEntitiesInfo.get(querySourceInfo.javaType()), hibType, required, expr));
                } else {
                    if (el.subitems().isEmpty()) {
                        props.add(new QuerySourceItemForPrimType<>(name, javaType, hibType, expr, el.implicit));
                    } else {
                        final QuerySourceItemForComponentType<?> propTpi = new QuerySourceItemForComponentType<>(name, javaType, hibType);
                        for (final EqlPropertyMetadata sub : el.subitems()) {
                            final ExpressionModel subExpr = sub.expressionModel;
                            propTpi.addSubitem(new QuerySourceItemForPrimType<>(sub.name, sub.javaType, sub.hibType, subExpr, sub.implicit));
                        }
                        props.add(propTpi);
                    }
                }
            }
        }
        return props;
    }

    private QuerySourceInfo<?> generateDeclaredQuerySourceInfo(final Class<? extends AbstractEntity<?>> type) {
        final List<EqlPropertyMetadata> propsMetadatas = entityMetadataHolder.obtainEqlEntityMetadata(type).props();
        return new QuerySourceInfo<>(type, true, generateQuerySourceItems(declaredQuerySourceInfoMap, propsMetadatas));
    }
    
    private QuerySourceInfo<?> generateModelledQuerySourceInfoForPersistentType(final Class<? extends AbstractEntity<?>> type) {
        final List<EqlPropertyMetadata> propsMetadatas = entityMetadataHolder.obtainEqlEntityMetadata(type).props();
        return new QuerySourceInfo<>(type, true, generateQuerySourceItems(modelledQuerySourceInfoMap, propsMetadatas));
    }

    public QuerySourceInfo<?> getDeclaredQuerySourceInfo(final Class<? extends AbstractEntity<?>> type) {
        final QuerySourceInfo<?> existing = declaredQuerySourceInfoMap.get(type);
        return existing != null ? existing : generateDeclaredQuerySourceInfo(type);
    }

    public QuerySourceInfo<?> getModelledQuerySourceInfo(final Class<? extends AbstractEntity<?>> type) {
        final QuerySourceInfo<?> existing = modelledQuerySourceInfoMap.get(type);
        if (existing != null) {
            return existing;
        }
        
        final EntityTypeInfo<?> eti = getEntityTypeInfo(type);
        if (eti.category == QUERY_BASED) {
            return generateModelledQuerySourceInfoForSyntheticType(type, seModels.get(getOriginalType(type)));
        } else {
            return generateModelledQuerySourceInfoForPersistentType(type);
        }
    }
}