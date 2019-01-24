package ua.com.fielden.platform.eql.meta;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.cache.Cache;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.elements.EntProp1;
import ua.com.fielden.platform.eql.stage1.elements.EntQuery1;
import ua.com.fielden.platform.eql.stage1.elements.EntValue1;
import ua.com.fielden.platform.eql.stage1.elements.IQrySource1;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnPersistentTypeWithCalcProps;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnSyntheticType;
import ua.com.fielden.platform.eql.stage2.elements.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.EntValue2;
import ua.com.fielden.platform.eql.stage2.elements.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.elements.QrySource2BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;

public class TransformatorToS2 {
    private List<Map<IQrySource1<? extends IQrySource2>, SourceInfo>> sourceMap = new ArrayList<>();
    private final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo;
    private final Cache<IQrySource1<? extends IQrySource2>, IQrySource2> sourcesCache;

    public TransformatorToS2(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        this(newBuilder().build(), domainInfo);
    }

    protected TransformatorToS2(final Cache<IQrySource1<? extends IQrySource2>, IQrySource2> sourcesCache, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo) {
        this.sourcesCache = sourcesCache; //must reference the passed-in argument in order to update the referenced cache if necessary.
        this.domainInfo = new HashMap<>(domainInfo);
        sourceMap.add(new HashMap<IQrySource1<? extends IQrySource2>, SourceInfo>());
    }
    
    static class SourceInfo {
        private final IQrySource2 source;
        private final EntityInfo<?> entityInfo;
        private final String alias;

        public SourceInfo(final IQrySource2 source, final EntityInfo<?> entityInfo, final String alias) {
            this.source = source;
            this.entityInfo = entityInfo;
            this.alias = alias;
        }
    }

    private EntityInfo<?> produceEntityInfoFrom(final IQrySource2 transformedSource) {
        if (!EntityAggregates.class.equals(transformedSource.sourceType())) {
            return domainInfo.get(transformedSource.sourceType());
        } else {
            final EntityInfo<EntityAggregates> entAggEntityInfo = new EntityInfo<>(EntityAggregates.class, null);
            for (final Yield2 yield : ((QrySource2BasedOnSubqueries) transformedSource).getYields().getYields()) {
                final AbstractPropInfo<?, ?> aep = isEntityType(yield.javaType())
                        ? new EntityTypePropInfo(yield.getAlias(), domainInfo.get(yield.javaType()), entAggEntityInfo)
                        : new PrimTypePropInfo(yield.getAlias(), yield.javaType(), entAggEntityInfo);
                entAggEntityInfo.addProp(aep);
            }
            return entAggEntityInfo;
        }
    }

    public TransformatorToS2 produceBasedOn() {
        final TransformatorToS2 result = new TransformatorToS2(sourcesCache, domainInfo);
        result.sourceMap.addAll(sourceMap);
        return result;
    }

    public TransformatorToS2 produceNewOne() {
        return new TransformatorToS2(sourcesCache, domainInfo);
    }

    private IQrySource2 transformSource(final IQrySource1<? extends IQrySource2> qrySourceStage1) {
        if (qrySourceStage1 instanceof QrySource1BasedOnPersistentTypeWithCalcProps || qrySourceStage1 instanceof QrySource1BasedOnSyntheticType) {
            throw new EqlException("Not supported yet.");
        }
        
        return ofNullable(sourcesCache.getIfPresent(qrySourceStage1)).orElseGet(() -> {
            
            final IQrySource2 result;
            
            if (qrySourceStage1 instanceof QrySource1BasedOnPersistentType) {
                final QrySource1BasedOnPersistentType qrySource = (QrySource1BasedOnPersistentType) qrySourceStage1;
                result = new QrySource2BasedOnPersistentType(qrySource.sourceType());
            } else {
                final QrySource1BasedOnSubqueries qrySource = (QrySource1BasedOnSubqueries) qrySourceStage1;
                result = new QrySource2BasedOnSubqueries(extractQueryModels(qrySource));
            }
            
            sourcesCache.put(qrySourceStage1, result);

            return result;
        });
    }

    private List<EntQuery2> extractQueryModels(final QrySource1BasedOnSubqueries qrySource) {
        return qrySource.getModels().stream().map(q -> q.transform(produceNewOne())).collect(toList());
    }

    private Map<IQrySource1<? extends IQrySource2>, SourceInfo> getCurrentQueryMap() {
        return sourceMap.get(0);
    }

    public IQrySource2 getTransformedSource(final IQrySource1<? extends IQrySource2> originalSource) {
        final IQrySource2 transformedSource = transformSource(originalSource);
        getCurrentQueryMap().put(originalSource, new SourceInfo(transformedSource, produceEntityInfoFrom(transformedSource), originalSource.getAlias()));
        return transformedSource;
    }

    public EntProp2 getTransformedProp(final EntProp1 originalProp) {
        final Iterator<Map<IQrySource1<? extends IQrySource2>, SourceInfo>> it = sourceMap.iterator();
        if (originalProp.isExternal()) {
            it.next();
        }

        for (; it.hasNext();) {
            final Map<IQrySource1<? extends IQrySource2>, SourceInfo> item = it.next();
            final PropResolution resolution = resolveProp(item.values(), originalProp);
            if (resolution != null) {
                return new EntProp2(resolution.getAliaslessName(), resolution.getSource(), resolution.getType());
            }
        }

        throw new EqlStage1ProcessingException(format("Can't resolve property [%s].", originalProp.getName()));
    }

    public EntQuery2 getTransformedQuery(final EntQuery1 originalQuery) {
        final TransformatorToS2 localResolver = originalQuery.isSubQuery() ? produceBasedOn() : produceNewOne();

        final EntQueryBlocks2 entQueryBlocks = new EntQueryBlocks2(
                originalQuery.getSources().transform(localResolver), 
                originalQuery.getConditions().transform(localResolver), 
                originalQuery.getYields().transform(localResolver), 
                originalQuery.getGroups().transform(localResolver), 
                originalQuery.getOrderings().transform(localResolver));

        return new EntQuery2(entQueryBlocks, originalQuery.type(), originalQuery.getCategory(), originalQuery.getFetchModel());
    }
    
    private PropResolution resolvePropAgainstSource(final SourceInfo source, final EntProp1 entProp) {
        final AbstractPropInfo<?, ?> asIsResolution = source.entityInfo.resolve(entProp.getName());
        if (source.alias != null && entProp.getName().startsWith(source.alias + ".")) {
            final String aliasLessPropName = entProp.getName().substring(source.alias.length() + 1);
            final AbstractPropInfo<?, ?> aliasLessResolution = source.entityInfo.resolve(aliasLessPropName);
            if (aliasLessResolution != null) {
                if (asIsResolution == null) {
                    return new PropResolution(aliasLessPropName, source.source, aliasLessResolution.javaType());
                } else {
                    throw new EqlStage1ProcessingException(format("Ambiguity while resolving prop [%s]. Both [%s] and [%s] are resolvable against the given source.", entProp.getName(), entProp.getName(), aliasLessPropName));
                }
            }
        }
        return asIsResolution != null ? new PropResolution(entProp.getName(), source.source, asIsResolution.javaType()) : null;
    }

    private PropResolution resolveProp(final Collection<SourceInfo> sources, final EntProp1 entProp) {
        final List<PropResolution> result = new ArrayList<>();
        for (final SourceInfo pair : sources) {
            final PropResolution resolution = resolvePropAgainstSource(pair, entProp);
            if (resolution != null) {
                result.add(resolution);
            }
        }

        if (result.size() > 1) {
            throw new EqlStage1ProcessingException(format("Ambiguity while resolving prop [%s]", entProp.getName()));
        }

        return result.size() == 1 ? result.get(0) : null;
    }
}