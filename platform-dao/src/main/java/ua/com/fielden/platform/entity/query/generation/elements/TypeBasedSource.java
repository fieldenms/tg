package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.dao.DomainPersistenceMetadataAnalyser;
import ua.com.fielden.platform.dao.EntityPersistenceMetadata;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class TypeBasedSource extends AbstractSource {
    private final Class<? extends AbstractEntity<?>> entityType;
    private final boolean generated;
    private EntityPersistenceMetadata entityPersistenceMetadata;

    public TypeBasedSource(final Class<? extends AbstractEntity<?>> entityType, final String alias, final DomainPersistenceMetadataAnalyser domainPersistenceMetadataAnalyser) {
	this(entityType, alias, false, domainPersistenceMetadataAnalyser);
    }

    public TypeBasedSource(final Class<? extends AbstractEntity<?>> entityType, final String alias, final boolean generated, final DomainPersistenceMetadataAnalyser domainPersistenceMetadataAnalyser) {
	super(alias, domainPersistenceMetadataAnalyser);
	if (entityType == null) {
	    throw new IllegalArgumentException("Missing entity type!");
	}
	this.entityType = entityType;
	this.generated = generated;
    }

    @Override
    public void populateSourceItems(final boolean parentLeftJoinLegacy) {
	entityPersistenceMetadata = getDomainPersistenceMetadataAnalyser().getEntityPersistenceMetadate(sourceType());

	for (final PropertyPersistenceInfo ppi : entityPersistenceMetadata.getProps().values()) {
		// if parent nullability = false then take the one from ppi, else true
	    sourceItems.put(ppi.getName(), new ResultQueryYieldDetails(ppi.getName(), ppi.getJavaType(), ppi.getHibType(), (ppi.getColumn() != null ? ppi.getColumn().getName() : null), ppi.isNullable() || parentLeftJoinLegacy));
	}
    }

    @Override
    protected Pair<PurePropInfo, PurePropInfo> lookForProp(final String dotNotatedPropName) {
	final PropertyPersistenceInfo finalPropInfo = getDomainPersistenceMetadataAnalyser().getPropPersistenceInfoExplicitly(entityType, dotNotatedPropName);

	if (finalPropInfo != null) {
	    final boolean finalPropNullability = getDomainPersistenceMetadataAnalyser().isNullable(entityType, dotNotatedPropName);
	    final PurePropInfo ppi = new PurePropInfo(finalPropInfo.getName(), finalPropInfo.getJavaType(), finalPropInfo.getHibType(), finalPropNullability || isNullable());
	    ppi.setExpressionModel(finalPropInfo.getExpressionModel());
	    return new Pair<PurePropInfo, PurePropInfo>(ppi, ppi);
	} else {
	    final PropertyPersistenceInfo propInfo = getDomainPersistenceMetadataAnalyser().getInfoForDotNotatedProp(entityType, dotNotatedPropName);
	    if (propInfo == null) {
		return null;
	    } else {
		final boolean propNullability = getDomainPersistenceMetadataAnalyser().isNullable(entityType, dotNotatedPropName);
		final String onePartProp = EntityUtils.splitPropByFirstDot(dotNotatedPropName).getKey();
		final PropertyPersistenceInfo explicitPartPropInfo = getDomainPersistenceMetadataAnalyser().getPropPersistenceInfoExplicitly(entityType, onePartProp);
		final String twoPartProp = onePartProp + "." + EntityUtils.splitPropByFirstDot(EntityUtils.splitPropByFirstDot(dotNotatedPropName).getValue()).getKey();
		final PropertyPersistenceInfo explicitPartPropInfo2 = getDomainPersistenceMetadataAnalyser().getPropPersistenceInfoExplicitly(entityType, twoPartProp);
		if (explicitPartPropInfo2 != null) {
			final boolean explicitPropNullability = true;
			final PurePropInfo ppi = new PurePropInfo(dotNotatedPropName, propInfo.getJavaType(), propInfo.getHibType(), propNullability || isNullable());
			ppi.setExpressionModel(propInfo.getExpressionModel());
			return new Pair<PurePropInfo, PurePropInfo>( //
			new PurePropInfo(explicitPartPropInfo2.getName(), explicitPartPropInfo2.getJavaType(), explicitPartPropInfo2.getHibType(), explicitPropNullability || isNullable()), //
			ppi);
		}

		final boolean explicitPropNullability = getDomainPersistenceMetadataAnalyser().isNullable(entityType, EntityUtils.splitPropByFirstDot(dotNotatedPropName).getKey());
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
	return entityType;
    }

    @Override
    public String sql() {
	return entityPersistenceMetadata.getTable() + " AS " + sqlAlias + "/*" + (alias == null ? " " : alias) + "*/";
    }

    @Override
    public List<EntValue> getValues() {
	return Collections.emptyList();
    }

    @Override
    public String toString() {
        return entityType.getSimpleName() + "-table AS " + getAlias() + " /*" + (generated ? " GEN " : "") + "*/";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
	result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
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
	if (entityType == null) {
	    if (other.entityType != null) {
		return false;
	    }
	} else if (!entityType.equals(other.entityType)) {
	    return false;
	}
	return true;
    }
}