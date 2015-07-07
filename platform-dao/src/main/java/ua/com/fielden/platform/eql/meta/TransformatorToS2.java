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
import ua.com.fielden.platform.eql.s1.elements.EntParam1;
import ua.com.fielden.platform.eql.s1.elements.EntProp1;
import ua.com.fielden.platform.eql.s1.elements.EntQuery1;
import ua.com.fielden.platform.eql.s1.elements.EntValue1;
import ua.com.fielden.platform.eql.s1.elements.ISource1;
import ua.com.fielden.platform.eql.s1.elements.QueryBasedSource1;
import ua.com.fielden.platform.eql.s1.elements.TypeBasedSource1;
import ua.com.fielden.platform.eql.s2.elements.EntProp2;
import ua.com.fielden.platform.eql.s2.elements.EntQuery2;
import ua.com.fielden.platform.eql.s2.elements.EntValue2;
import ua.com.fielden.platform.eql.s2.elements.Expression2;
import ua.com.fielden.platform.eql.s2.elements.ISource2;
import ua.com.fielden.platform.eql.s2.elements.QueryBasedSource2;
import ua.com.fielden.platform.eql.s2.elements.TypeBasedSource2;
import ua.com.fielden.platform.eql.s2.elements.Yield2;

public class TransformatorToS2 {
    private final Map<Class<? extends AbstractEntity<?>>, EntityInfo> metadata;
    private final Map<String, Object> paramValues = new HashMap<>();
    private final DomainMetadata domainData;
    private final SourcesStack sourcesStack = new SourcesStack();

    public TransformatorToS2(final Map<Class<? extends AbstractEntity<?>>, EntityInfo> metadata, final Map<String, Object> paramValues, final DomainMetadata domainData) {
        this.metadata = metadata;
        this.paramValues.putAll(paramValues);
        this.domainData = domainData;
    }

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

    public void transformAndAccumulateSource(final ISource1<? extends ISource2> source) {
        final ISource2 transformedSource = transformSource(source);
        final SourceInfo sourceInfo = new SourceInfo(transformedSource, getEntityInfoForSource(transformedSource), true, source.getAlias());
        sourcesStack.accumulateTransformedSource(source, sourceInfo);
    }
    
    private EntityInfo getEntityInfoForSource(ISource2 transformedSource) {
        if (!EntityAggregates.class.equals(transformedSource.sourceType())) {
            return metadata.get(transformedSource.sourceType());
        } else {
            final EntityInfo entAggEntityInfo = new EntityInfo(EntityAggregates.class, null);
            for (final Yield2 yield : ((QueryBasedSource2) transformedSource).getYields().getYields()) {
                final AbstractPropInfo aep = AbstractEntity.class.isAssignableFrom(yield.javaType()) ? new EntityTypePropInfo(yield.getAlias(), entAggEntityInfo, metadata.get(yield.javaType()), null)
                        : new PrimTypePropInfo(yield.getAlias(), entAggEntityInfo, yield.javaType(), null);
                entAggEntityInfo.getProps().put(yield.getAlias(), aep);
            }
            return entAggEntityInfo;
        }
    }

    public TransformatorToS2 produceBasedOn() {
        final TransformatorToS2 result = new TransformatorToS2(metadata, paramValues, domainData);
        result.sourcesStack.getSourcesList().addAll(sourcesStack.getSourcesList());

        return result;
    }

    public TransformatorToS2 produceNewOne() {
        return new TransformatorToS2(metadata, paramValues, domainData);
    }

    public TransformatorToS2 produceOneForCalcPropExpression(final ISource2 source) {
        final TransformatorToS2 result = new TransformatorToS2(metadata, paramValues, domainData);
        for (final Map<ISource1<? extends ISource2>, SourceInfo> item : sourcesStack.getSourcesList()) {
            for (final Entry<ISource1<? extends ISource2>, SourceInfo> mapItem : item.entrySet()) {
                if (mapItem.getValue().getSource().equals(source)) {
                    final Map<ISource1<? extends ISource2>, SourceInfo> newMap = new HashMap<>();
                    newMap.put(mapItem.getKey(), mapItem.getValue().produceNewWithoutAliasing());
                    result.sourcesStack.add(newMap);
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

    public ISource2 getTransformedSource(final ISource1<? extends ISource2> originalSource) {
        return sourcesStack.getTransformedSource(originalSource);
    }

    public EntProp2 getTransformedProp(final EntProp1 originalProp) {
        final Iterator<Map<ISource1<? extends ISource2>, SourceInfo>> it = sourcesStack.getSourcesList().iterator();
        if (originalProp.isExternal()) {
            it.next();
        }

        for (; it.hasNext();) {
            final Map<ISource1<? extends ISource2>, SourceInfo> item = it.next();
            final PropResolution resolution = (new PropResolver()).resolveProp(item.values(), originalProp);
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
        ResolutionPath resolutionPath = resolution.getResolution();
        final AbstractPropInfo propInfo = resolutionPath.getFinalMember();
        final Expression2 expr = propInfo.getExpression() != null ? propInfo.getExpression().transform(this.produceOneForCalcPropExpression(resolution.getSource())) : null;
        return new EntProp2(resolution.getEntProp().getName(), resolution.getSource(), resolution.getResolution(), expr);
    }
}