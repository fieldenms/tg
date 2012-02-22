package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntQuerySourceFromEntityType extends AbstractEntQuerySource {
    private final Class<? extends AbstractEntity> entityType;
    private final boolean generated;

    public EntQuerySourceFromEntityType(final Class<? extends AbstractEntity> entityType, final String alias, final MappingsGenerator mappingsGenerator) {
    	this(entityType, alias, false, mappingsGenerator);
    }

    public EntQuerySourceFromEntityType(final Class<? extends AbstractEntity> entityType, final String alias, final boolean generated, final MappingsGenerator mappingsGenerator) {
	super(alias, mappingsGenerator);
	this.entityType = entityType;
	this.generated = generated;

	for (final PropertyPersistenceInfo ppi : getMappingsGenerator().getEntityPPIs(sourceType())) {
	    sourceItems.put(ppi.getName(), ppi);
	}
    }

    @Override
    protected Pair<PurePropInfo, PurePropInfo> lookForProp(final String dotNotatedPropName) {
	final PropertyPersistenceInfo finalPropInfo = getMappingsGenerator().getPropPersistenceInfoExplicitly(entityType, dotNotatedPropName);
	if (finalPropInfo != null) {
	    final PurePropInfo ppi = new PurePropInfo(finalPropInfo.getName(), finalPropInfo.getJavaType(), finalPropInfo.getHibType());
	    return new Pair<PurePropInfo, PurePropInfo>(ppi, ppi);
	} else {
	    final PropertyPersistenceInfo propInfo = getMappingsGenerator().getInfoForDotNotatedProp(entityType, dotNotatedPropName);
	    if (propInfo == null) {
		return null;
	    } else {
		final PropertyPersistenceInfo explicitPartPropInfo = getMappingsGenerator().getPropPersistenceInfoExplicitly(entityType, EntityUtils.splitPropByFirstDot(dotNotatedPropName).getKey());
		return new Pair<PurePropInfo, PurePropInfo>( //
		new PurePropInfo(explicitPartPropInfo.getName(), explicitPartPropInfo.getJavaType(), explicitPartPropInfo.getHibType()), //
		new PurePropInfo(dotNotatedPropName, propInfo.getJavaType(), propInfo.getHibType()));
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
	return getMappingsGenerator().getTableClause(sourceType()) + " AS " + sqlAlias + "/*" + (alias == null ? " " : alias) + "*/";
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
	if (!(obj instanceof EntQuerySourceFromEntityType)) {
	    return false;
	}
	final EntQuerySourceFromEntityType other = (EntQuerySourceFromEntityType) obj;
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