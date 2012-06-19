package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.EntityMetadata;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class TypeBasedSource extends AbstractSource {
    private final boolean generated;
    private EntityMetadata entityMetadata;

    public TypeBasedSource(final EntityMetadata entityMetadata, final String alias, final DomainMetadataAnalyser domainMetadataAnalyser) {
	this(entityMetadata, alias, false, domainMetadataAnalyser);
    }

    public TypeBasedSource(final EntityMetadata entityMetadata, final String alias, final boolean generated, final DomainMetadataAnalyser domainMetadataAnalyser) {
	super(alias, domainMetadataAnalyser);
	this.generated = generated;
	this.entityMetadata = entityMetadata;//getDomainMetadataAnalyser().getEntityMetadata(sourceType());
	if (entityMetadata == null) {
	    throw new IllegalStateException("Missing entity persistence metadata for entity type: " + sourceType());
	}
    }

    @Override
    public void populateSourceItems(final boolean parentLeftJoinLegacy) {
	for (final PropertyMetadata ppi : entityMetadata.getProps().values()) {
	    // if parent nullability = false then take the one from ppi, else true
	    sourceItems.put(ppi.getName(), new ResultQueryYieldDetails(ppi.getName(), ppi.getJavaType(), ppi.getHibType(), (ppi.getColumn() != null ? ppi.getColumn().getName()
		    : null), ppi.isNullable() || parentLeftJoinLegacy, ppi.getYieldDetailType()));
	}
    }

    @Override
    protected Pair<PurePropInfo, PurePropInfo> lookForProp(final String dotNotatedPropName) {
	final PropertyMetadata finalPropInfo = getDomainMetadataAnalyser().getPropPersistenceInfoExplicitly(sourceType(), dotNotatedPropName);

	if (finalPropInfo != null) {
	    final boolean finalPropNullability = getDomainMetadataAnalyser().isNullable(sourceType(), dotNotatedPropName);
	    final PurePropInfo ppi = new PurePropInfo(finalPropInfo.getName(), finalPropInfo.getJavaType(), finalPropInfo.getHibType(), finalPropNullability || isNullable());
	    ppi.setExpressionModel(finalPropInfo.getExpressionModel());
	    return new Pair<PurePropInfo, PurePropInfo>(ppi, ppi);
	} else {
	    final PropertyMetadata propInfo = getDomainMetadataAnalyser().getInfoForDotNotatedProp(sourceType(), dotNotatedPropName);
	    if (propInfo == null) {
		return null;
	    } else {
		final boolean propNullability = getDomainMetadataAnalyser().isNullable(sourceType(), dotNotatedPropName);
		final String onePartProp = EntityUtils.splitPropByFirstDot(dotNotatedPropName).getKey();
		final PropertyMetadata explicitPartPropInfo = getDomainMetadataAnalyser().getPropPersistenceInfoExplicitly(sourceType(), onePartProp);
		final String twoPartProp = onePartProp + "." + EntityUtils.splitPropByFirstDot(EntityUtils.splitPropByFirstDot(dotNotatedPropName).getValue()).getKey();
		final PropertyMetadata explicitPartPropInfo2 = getDomainMetadataAnalyser().getPropPersistenceInfoExplicitly(sourceType(), twoPartProp);
		if (explicitPartPropInfo2 != null) {
			final boolean explicitPropNullability = true;
			final PurePropInfo ppi = new PurePropInfo(dotNotatedPropName, propInfo.getJavaType(), propInfo.getHibType(), propNullability || isNullable());
			ppi.setExpressionModel(propInfo.getExpressionModel());
			return new Pair<PurePropInfo, PurePropInfo>( //
			new PurePropInfo(explicitPartPropInfo2.getName(), explicitPartPropInfo2.getJavaType(), explicitPartPropInfo2.getHibType(), explicitPropNullability || isNullable()), //
			ppi);
		}

		final boolean explicitPropNullability = getDomainMetadataAnalyser().isNullable(sourceType(), EntityUtils.splitPropByFirstDot(dotNotatedPropName).getKey());
		final PurePropInfo ppi = new PurePropInfo(dotNotatedPropName, propInfo.getJavaType(), propInfo.getHibType(), propNullability || isNullable());
		ppi.setExpressionModel(propInfo.getExpressionModel());
		return new Pair<PurePropInfo, PurePropInfo>( //
		new PurePropInfo(explicitPartPropInfo.getName(), explicitPartPropInfo.getJavaType(), explicitPartPropInfo.getHibType(), explicitPropNullability || isNullable()), //
		ppi);
	    }
	}
    }

    @Override
    public boolean generated() {
	return generated;
    }

    @Override
    public Class sourceType() {
	return entityMetadata.getType();
    }

    @Override
    public String sql() {
	return entityMetadata.getTable() + " AS " + sqlAlias + "/*" + (alias == null ? " " : alias) + "*/";
    }

    @Override
    public List<EntValue> getValues() {
	return Collections.emptyList();
    }

    @Override
    public String toString() {
        return sourceType().getSimpleName() + "-table AS " + getAlias() + " /*" + (generated ? " GEN " : "") + "*/";
    }


    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
	result = prime * result + ((entityMetadata == null) ? 0 : entityMetadata.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof TypeBasedSource)) {
	    return false;
	}
	final TypeBasedSource other = (TypeBasedSource) obj;
	if (getAlias() == null) {
	    if (other.getAlias() != null) {
		return false;
	    }
	} else if (!getAlias().equals(other.getAlias())) {
	    return false;
	}
	if (entityMetadata == null) {
	    if (other.entityMetadata != null) {
		return false;
	    }
	} else if (!entityMetadata.equals(other.entityMetadata)) {
	    return false;
	}
	return true;
    }

}