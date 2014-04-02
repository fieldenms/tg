package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.eql.s1.elements.EntParam1;
import ua.com.fielden.platform.eql.s1.elements.EntProp1;
import ua.com.fielden.platform.eql.s1.elements.EntQuery1;
import ua.com.fielden.platform.eql.s1.elements.EntValue1;
import ua.com.fielden.platform.eql.s1.elements.ISource1;
import ua.com.fielden.platform.eql.s1.elements.QueryBasedSource1;
import ua.com.fielden.platform.eql.s1.elements.TypeBasedSource1;
import ua.com.fielden.platform.eql.s1.processing.EntQueryGenerator1;
import ua.com.fielden.platform.eql.s2.elements.EntProp2;
import ua.com.fielden.platform.eql.s2.elements.EntQuery2;
import ua.com.fielden.platform.eql.s2.elements.EntValue2;
import ua.com.fielden.platform.eql.s2.elements.Expression2;
import ua.com.fielden.platform.eql.s2.elements.ISource2;
import ua.com.fielden.platform.eql.s2.elements.QueryBasedSource2;
import ua.com.fielden.platform.eql.s2.elements.TypeBasedSource2;
import ua.com.fielden.platform.eql.s2.elements.Yield2;

public class TransformatorToS2 {
    private List<Map<ISource1<? extends ISource2>, SourceInfo>> sourceMap = new ArrayList<>();
    private final Map<Class<? extends AbstractEntity<?>>, EntityInfo> metadata;
    private final Map<String, Object> paramValues = new HashMap<>();
    private final DomainMetadata domainData;
    private final IFilter filter;
    private final String username;
    private final EntQueryGenerator1 entQueryGenerator1;

    public TransformatorToS2(final Map<Class<? extends AbstractEntity<?>>, EntityInfo> metadata, final Map<String, Object> paramValues, final DomainMetadata domainData, final IFilter filter, final String username, final EntQueryGenerator1 entQueryGenerator1) {
        this.metadata = metadata;
        sourceMap.add(new HashMap<ISource1<? extends ISource2>, SourceInfo>());
        this.paramValues.putAll(paramValues);
        this.domainData = domainData;
        this.filter = filter;
        this.username = username;
        this.entQueryGenerator1 = entQueryGenerator1;
    }

    static class SourceInfo {
        private final ISource2 source;
        private final EntityInfo entityInfo;
        private final boolean aliasingAllowed;
        private final String alias;

        public SourceInfo(final ISource2 source, final EntityInfo entityInfo, final boolean aliasingAllowed, final String alias) {
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
            return domainData.getBooleanValue((Boolean) value);
        }
        return value;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (final Map<ISource1<? extends ISource2>, SourceInfo> item : sourceMap) {
            sb.append("-----------------------------\n");
            for (final SourceInfo subitem : item.values()) {
                sb.append("---");
                sb.append(subitem.source.sourceType().getSimpleName());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public void addSource(final ISource1<? extends ISource2> source) {
        final ISource2 transformedSource = transformSource(source);
        if (EntityAggregates.class.equals(transformedSource.sourceType())) {
            final EntityInfo entAggEntityInfo = new EntityInfo(EntityAggregates.class);
            for (final Yield2 yield : ((QueryBasedSource2) transformedSource).getYields().getYields()) {
                final AbstractPropInfo aep = AbstractEntity.class.isAssignableFrom(yield.javaType()) ? new EntityTypePropInfo(yield.getAlias(), entAggEntityInfo, metadata.get(yield.javaType()), null)
                        : new PrimTypePropInfo(yield.getAlias(), entAggEntityInfo, yield.javaType(), null);
                entAggEntityInfo.getProps().put(yield.getAlias(), aep);
            }

            getCurrentQueryMap().put(source, new SourceInfo(transformedSource, entAggEntityInfo, true, source.getAlias()));
        } else {
            getCurrentQueryMap().put(source, new SourceInfo(transformedSource, metadata.get(transformedSource.sourceType()), true, source.getAlias()));
        }
    }

    public TransformatorToS2 produceBasedOn() {
        final TransformatorToS2 result = new TransformatorToS2(metadata, paramValues, domainData, filter, username, entQueryGenerator1);
        result.sourceMap.addAll(sourceMap);

        return result;
    }

    public TransformatorToS2 produceNewOne() {
        final TransformatorToS2 result = new TransformatorToS2(metadata, paramValues, domainData, filter, username, entQueryGenerator1);
        return result;
    }

    public TransformatorToS2 produceOneForCalcPropExpression(final ISource2 source) {
        final TransformatorToS2 result = new TransformatorToS2(metadata, paramValues, domainData, filter, username, entQueryGenerator1);
        for (final Map<ISource1<? extends ISource2>, SourceInfo> item : sourceMap) {
            for (final Entry<ISource1<? extends ISource2>, SourceInfo> mapItem : item.entrySet()) {
                if (mapItem.getValue().source.equals(source)) {
                    final Map<ISource1<? extends ISource2>, SourceInfo> newMap = new HashMap<>();
                    newMap.put(mapItem.getKey(), mapItem.getValue().produceNewWithoutAliasing());
                    result.sourceMap.add(newMap);
                    return result;
                }
            }

        }

        throw new IllegalStateException("Should not reach here!");
    }

    private ISource2 transformSource(final ISource1<? extends ISource2> originalSource) {
        if (originalSource instanceof TypeBasedSource1) {
            final TypeBasedSource1 source = (TypeBasedSource1) originalSource;
            return new TypeBasedSource2(source.sourceType()/*, originalSource.getAlias(), source.getDomainMetadataAnalyser()*/);
        } else {
            final QueryBasedSource1 source = (QueryBasedSource1) originalSource;
            final List<EntQuery2> transformed = new ArrayList<>();
            for (final EntQuery1 entQuery : source.getModels()) {
                transformed.add(entQuery.transform(produceNewOne()));
            }

            return new QueryBasedSource2(originalSource.getAlias(), transformed.toArray(new EntQuery2[] {}));
        }
    }

    private Map<ISource1<? extends ISource2>, SourceInfo> getCurrentQueryMap() {
        return sourceMap.get(0);
    }

    public ISource2 getTransformedSource(final ISource1<? extends ISource2> originalSource) {
        return getCurrentQueryMap().get(originalSource).source;
    }

    public EntProp2 getTransformedProp(final EntProp1 originalProp) {
        final Iterator<Map<ISource1<? extends ISource2>, SourceInfo>> it = sourceMap.iterator();
        if (originalProp.isExternal()) {
            it.next();
        }

        for (; it.hasNext();) {
            final Map<ISource1<? extends ISource2>, SourceInfo> item = it.next();
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
        final AbstractPropInfo propInfo = resolution.resolution;
        final Expression2 expr = propInfo.getExpression() != null ? propInfo.getExpression().transform(this.produceOneForCalcPropExpression(resolution.source)) : null;
        return new EntProp2(resolution.entProp.getName(), resolution.source, resolution.resolution, expr);
    }

    public static class PropResolution {
        private final boolean aliased;

        public PropResolution(final boolean aliased, final ISource2 source, final AbstractPropInfo resolution, final EntProp1 entProp) {
            super();
            this.aliased = aliased;
            this.source = source;
            this.resolution = resolution;
            this.entProp = entProp;
        }

        private final ISource2 source;
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

    public EntQueryGenerator1 getEntQueryGenerator1() {
        return entQueryGenerator1;
    }
}