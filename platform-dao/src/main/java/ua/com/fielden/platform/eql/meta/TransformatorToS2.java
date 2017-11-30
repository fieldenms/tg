package ua.com.fielden.platform.eql.meta;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.dao.DomainMetadata.getBooleanValue;
import static ua.com.fielden.platform.eql.meta.MetadataGenerator.createYieldAllQueryModel;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.google.common.cache.Cache;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage1.elements.EntParam1;
import ua.com.fielden.platform.eql.stage1.elements.EntProp1;
import ua.com.fielden.platform.eql.stage1.elements.EntValue1;
import ua.com.fielden.platform.eql.stage1.elements.IQrySource1;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnPersistentTypeWithCalcProps;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage1.elements.QrySource1BasedOnSyntheticType;
import ua.com.fielden.platform.eql.stage2.elements.EntProp2;
import ua.com.fielden.platform.eql.stage2.elements.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.EntValue2;
import ua.com.fielden.platform.eql.stage2.elements.Expression2;
import ua.com.fielden.platform.eql.stage2.elements.IQrySource2;
import ua.com.fielden.platform.eql.stage2.elements.QrySource2BasedOnPersistentType;
import ua.com.fielden.platform.eql.stage2.elements.QrySource2BasedOnPersistentTypeWithCalcProps;
import ua.com.fielden.platform.eql.stage2.elements.QrySource2BasedOnSubqueries;
import ua.com.fielden.platform.eql.stage2.elements.QrySource2BasedOnSyntheticType;
import ua.com.fielden.platform.eql.stage2.elements.Yield2;

public class TransformatorToS2 {
    private List<Map<IQrySource1<? extends IQrySource2>, SourceInfo>> sourceMap = new ArrayList<>();
    private final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo;
    private final Cache<IQrySource1<? extends IQrySource2>, IQrySource2> sourcesCache;
    private final Map<String, Object> paramValues = new HashMap<>();
    private final IFilter filter;
    private final String username;
    private final EntQueryGenerator entQueryGenerator1;

    public TransformatorToS2(final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo, final Map<String, Object> paramValues, final IFilter filter, final String username, final EntQueryGenerator entQueryGenerator1) {
        this(newBuilder().build(), domainInfo, paramValues, filter, username, entQueryGenerator1);
    }

    protected TransformatorToS2(final Cache<IQrySource1<? extends IQrySource2>, IQrySource2> sourcesCache, final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> domainInfo, final Map<String, Object> paramValues, final IFilter filter, final String username, final EntQueryGenerator entQueryGenerator1) {
        this.sourcesCache = sourcesCache; //must reference the passed-in argument in order to update the referenced cache if necessary.
        this.domainInfo = new HashMap<>(domainInfo);
        sourceMap.add(new HashMap<IQrySource1<? extends IQrySource2>, SourceInfo>());
        this.paramValues.putAll(paramValues);
        this.filter = filter;
        this.username = username;
        this.entQueryGenerator1 = entQueryGenerator1;
    }

    static class SourceInfo {
        private final IQrySource2 source;
        private final EntityInfo<?> entityInfo;
        private final boolean aliasingAllowed;
        private final String alias;

        public SourceInfo(final IQrySource2 source, final EntityInfo<?> entityInfo, final boolean aliasingAllowed, final String alias) {
            this.source = source;
            this.entityInfo = entityInfo;
            this.aliasingAllowed = aliasingAllowed;
            this.alias = alias;
        }

        SourceInfo produceNewWithoutAliasing() {
            return new SourceInfo(source, entityInfo, false, alias);
        }
    }

    // TODO EQL
    //    protected List<ISingleOperand1<? extends ISingleOperand2>> getModelForArrayParam(final TokenCategory cat, final Object value) {
    //	final List<ISingleOperand1<? extends ISingleOperand2>> result = new ArrayList<>();
    //	final Object paramValue = getParamValue((String) value);
    //
    //	if (!(paramValue instanceof List)) {
    //	    result.add(getModelForSingleOperand(cat, value));
    //	} else {
    //	    for (final Object singleValue : (List<Object>) paramValue) {
    //		result.add(getModelForSingleOperand((cat == IPARAM ? IVAL : VAL), singleValue));
    //	    }
    //	}
    //	return result;
    //    }

    protected Object getParamValue(final String paramName) {
        if (paramValues.containsKey(paramName)) {
            return preprocessValue(paramValues.get(paramName));
        } else {
            return null; //TODO think through
            //throw new RuntimeException("No value has been provided for parameter with name [" + paramName + "]");
        }
    }

    private Object preprocessValue(final Object value) {
        if (value != null && (value.getClass().isArray() || value instanceof Collection<?>)) {
            final List<Object> values = new ArrayList<Object>();
            for (final Object object : (Iterable) value) {
                final Object furtherPreprocessed = preprocessValue(object);
                if (furtherPreprocessed instanceof List) {
                    values.addAll((List) furtherPreprocessed);
                } else {
                    values.add(furtherPreprocessed);
                }
            }
            return values;
        } else {
            return convertValue(value);
        }
    }

    /** Ensures that values of boolean types are converted properly. */
    private Object convertValue(final Object value) {
        if (value instanceof Boolean) {
            return getBooleanValue((Boolean) value);
        }
        return value;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (final Map<IQrySource1<? extends IQrySource2>, SourceInfo> item : sourceMap) {
            sb.append("-----------------------------\n");
            for (final SourceInfo subitem : item.values()) {
                sb.append("---");
                sb.append(subitem.source.sourceType().getSimpleName());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void addSource(final IQrySource1<? extends IQrySource2> source) {
        final IQrySource2 transformedSource = transformSource(source);
        if (EntityAggregates.class.equals(transformedSource.sourceType())) {
            final EntityInfo<EntityAggregates> entAggEntityInfo = new EntityInfo<>(EntityAggregates.class, null);
            for (final Yield2 yield : ((QrySource2BasedOnSubqueries) transformedSource).getYields().getYields()) {
                final AbstractPropInfo aep = AbstractEntity.class.isAssignableFrom(yield.javaType())
                        ? new EntityTypePropInfo(yield.getAlias(), entAggEntityInfo, domainInfo.get(yield.javaType()), null)
                        : new PrimTypePropInfo(yield.getAlias(), entAggEntityInfo, yield.javaType(), null);
                entAggEntityInfo.getProps().put(yield.getAlias(), aep);
            }
            getCurrentQueryMap().put(source, new SourceInfo(transformedSource, entAggEntityInfo, true, source.getAlias()));
        } else {
            getCurrentQueryMap().put(source, new SourceInfo(transformedSource, domainInfo.get(transformedSource.sourceType()), true, source.getAlias()));
        }
    }

    public TransformatorToS2 produceBasedOn() {
        final TransformatorToS2 result = new TransformatorToS2(sourcesCache, domainInfo, paramValues, filter, username, entQueryGenerator1);
        result.sourceMap.addAll(sourceMap);

        return result;
    }

    public TransformatorToS2 produceNewOne() {
        final TransformatorToS2 result = new TransformatorToS2(sourcesCache, domainInfo, paramValues, filter, username, entQueryGenerator1);
        return result;
    }

    public TransformatorToS2 produceOneForCalcPropExpression(final IQrySource2 source) {
        final TransformatorToS2 result = new TransformatorToS2(sourcesCache, domainInfo, paramValues, filter, username, entQueryGenerator1);
        for (final Map<IQrySource1<? extends IQrySource2>, SourceInfo> item : sourceMap) {
            for (final Entry<IQrySource1<? extends IQrySource2>, SourceInfo> mapItem : item.entrySet()) {
                if (mapItem.getValue().source.equals(source)) {
                    final Map<IQrySource1<? extends IQrySource2>, SourceInfo> newMap = new HashMap<>();
                    newMap.put(mapItem.getKey(), mapItem.getValue().produceNewWithoutAliasing());
                    result.sourceMap.add(newMap);
                    return result;
                }
            }

        }

        throw new IllegalStateException("Should not reach here!");
    }

    private IQrySource2 transformSource(final IQrySource1<? extends IQrySource2> qrySourceStage1) {
        return ofNullable(sourcesCache.getIfPresent(qrySourceStage1)).orElseGet(() -> {
            final IQrySource2 result;
            if (qrySourceStage1 instanceof QrySource1BasedOnPersistentType) {
                final QrySource1BasedOnPersistentType qrySource = (QrySource1BasedOnPersistentType) qrySourceStage1;
                result = new QrySource2BasedOnPersistentType(qrySource.sourceType());
            } else if (qrySourceStage1 instanceof QrySource1BasedOnPersistentTypeWithCalcProps) {
                final QrySource1BasedOnPersistentTypeWithCalcProps qrySource = (QrySource1BasedOnPersistentTypeWithCalcProps) qrySourceStage1;
                sourcesCache.put(qrySourceStage1, new QrySource2BasedOnPersistentType(qrySource.sourceType()));
                result = new QrySource2BasedOnPersistentTypeWithCalcProps(qrySource.sourceType(), qrySourceStage1.getAlias(), extractQueryModels(Stream.of(createYieldAllQueryModel(qrySource.sourceType()))).get(0));
            } else if (qrySourceStage1 instanceof QrySource1BasedOnSyntheticType) {
                final QrySource1BasedOnSyntheticType qrySource = (QrySource1BasedOnSyntheticType) qrySourceStage1;
                result = new QrySource2BasedOnSyntheticType(qrySource.sourceType(), qrySourceStage1.getAlias(), extractQueryModels(getEntityModelsOfQueryBasedEntityType(qrySource.sourceType()).stream()));
            } else {
                final QrySource1BasedOnSubqueries qrySource = (QrySource1BasedOnSubqueries) qrySourceStage1;
                result = new QrySource2BasedOnSubqueries(qrySourceStage1.getAlias(), extractQueryModels(qrySource));
            }
            sourcesCache.put(qrySourceStage1, result);
            return result;
        });
    }

    private <T extends AbstractEntity<?>> List<EntQuery2> extractQueryModels(final Stream<EntityResultQueryModel<T>> stream) {
        final EntQueryGenerator gen = new EntQueryGenerator();
        return stream.map(q -> gen.generateEntQueryAsSourceQuery(q, empty())).map(q -> q.transform(produceNewOne())).collect(toList());
    }

    private List<EntQuery2> extractQueryModels(final QrySource1BasedOnSubqueries qrySource) {
        return qrySource.getModels().stream().map(q -> q.transform(produceNewOne())).collect(toList());
    }

    private Map<IQrySource1<? extends IQrySource2>, SourceInfo> getCurrentQueryMap() {
        return sourceMap.get(0);
    }

    public IQrySource2 getTransformedSource(final IQrySource1<? extends IQrySource2> originalSource) {
        return getCurrentQueryMap().get(originalSource).source;
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
                return generateTransformedProp(resolution);
            }
        }

        throw new IllegalStateException("Can't resolve property [" + originalProp.getName() + "].");
    }

    public EntValue2 getTransformedParamToValue(final EntParam1 originalParam) {
        return new EntValue2(getParamValue(originalParam.getName()), originalParam.isIgnoreNull());
    }

    public EntValue2 getTransformedValue(final EntValue1 originalValue) {
        return new EntValue2(preprocessValue(originalValue.getValue()), originalValue.isIgnoreNull());
    }

    private EntProp2 generateTransformedProp(final PropResolution resolution) {
        return new EntProp2(resolution.entProp.getName(), resolution.source, resolution.resolution);
    }

    public static class PropResolution {
        private final boolean aliased;

        public PropResolution(final boolean aliased, final IQrySource2 source, final AbstractPropInfo resolution, final EntProp1 entProp) {
            this.aliased = aliased;
            this.source = source;
            this.resolution = resolution;
            this.entProp = entProp;
        }

        private final IQrySource2 source;
        private final AbstractPropInfo resolution;
        private final EntProp1 entProp;
    }

    private PropResolution resolvePropAgainstSource(final SourceInfo source, final EntProp1 entProp) {
        final AbstractPropInfo asIsResolution = source.entityInfo.resolve(entProp.getName());
        if (source.alias != null && source.aliasingAllowed && entProp.getName().startsWith(source.alias + ".")) {
            final String aliasLessPropName = entProp.getName().substring(source.alias.length() + 1);
            final AbstractPropInfo aliasLessResolution = source.entityInfo.resolve(aliasLessPropName);
            if (aliasLessResolution != null) {
                if (asIsResolution == null) {
                    return new PropResolution(true, source.source, aliasLessResolution, entProp);
                } else {
                    throw new IllegalStateException("Ambiguity while resolving prop [" + entProp.getName() + "]. Both [" + entProp.getName() + "] and [" + aliasLessPropName
                            + "] are resolvable against given source.");
                }
            }
        }
        return asIsResolution != null ? new PropResolution(false, source.source, asIsResolution, entProp) : null;
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
            throw new IllegalStateException("Ambiguity while resolving prop [" + entProp.getName() + "]");
        }

        return result.size() == 1 ? result.get(0) : null;
    }

    public IFilter getFilter() {
        return filter;
    }

    public String getUsername() {
        return username;
    }

    public EntQueryGenerator getEntQueryGenerator1() {
        return entQueryGenerator1;
    }
}