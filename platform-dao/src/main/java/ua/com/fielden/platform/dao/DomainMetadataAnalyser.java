package ua.com.fielden.platform.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class DomainMetadataAnalyser {
    private final Map<Class<? extends AbstractEntity<?>>, EntityMetadata> entityMetadataMap = new HashMap<Class<? extends AbstractEntity<?>>, EntityMetadata>();

    private final DomainMetadata domainMetadata;

    public DomainMetadataAnalyser(final DomainMetadata domainMetadata) {
	super();
	this.domainMetadata = domainMetadata;
	entityMetadataMap.putAll(domainMetadata.getEntityMetadataMap());
    }

    public EntityMetadata getEntityMetadata(final Class<? extends AbstractEntity<?>> entityType) {
	if (entityType == null || !AbstractEntity.class.isAssignableFrom(entityType) || EntityAggregates.class.equals(entityType)) {
	    return null;
	}

	final EntityMetadata existing = entityMetadataMap.get(entityType);

	if (existing != null) {
	    return existing;
	} else {
	    try {
		final EntityMetadata newOne = domainMetadata.generateEntityMetadata(entityType);
		entityMetadataMap.put(entityType, newOne);
		return newOne;
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new RuntimeException(e.getMessage() + "entityType = " + entityType);
		//return null;
	    }
	}
    }

    /**
     * Retrieves persistence info for entity property, which is explicitly persisted within this entity type.
     * @param entityType
     * @param propName
     * @return
     */
    public PropertyMetadata getPropPersistenceInfoExplicitly(final Class<? extends AbstractEntity<?>> entityType, final String propName) {
	final EntityMetadata map = getEntityMetadata(entityType);
	return map != null ? map.getProps().get(propName) : null;
    }


    /**
     * Retrieves persistence info for entity property or its nested subproperty.
     * @param entityType
     * @param propName
     * @return
     */
    public PropertyMetadata getInfoForDotNotatedProp(final Class<? extends AbstractEntity<?>> entityType, final String dotNotatedPropName) {
	final PropertyMetadata simplePropInfo = getPropPersistenceInfoExplicitly(entityType, dotNotatedPropName);
	if (simplePropInfo != null) {
	    return simplePropInfo;
	} else {
	    final Pair<String, String> propSplit = EntityUtils.splitPropByFirstDot(dotNotatedPropName);
	    final PropertyMetadata firstPropInfo = getPropPersistenceInfoExplicitly(entityType, propSplit.getKey());
	    if (firstPropInfo != null && firstPropInfo.getJavaType() != null) {
		return getInfoForDotNotatedProp(firstPropInfo.getJavaType(), propSplit.getValue());
	    } else {
		return null;
	    }
	}
    }

    public boolean isNullable (final Class<? extends AbstractEntity<?>> entityType, final String dotNotatedPropName) {
	final PropertyMetadata simplePropInfo = getPropPersistenceInfoExplicitly(entityType, dotNotatedPropName);
	if (simplePropInfo != null) {
	    return simplePropInfo.isNullable();
	} else {
	    final Pair<String, String> propSplit = EntityUtils.splitPropByFirstDot(dotNotatedPropName);
	    final PropertyMetadata firstPropInfo = getPropPersistenceInfoExplicitly(entityType, propSplit.getKey());
	    if (firstPropInfo != null && firstPropInfo.getJavaType() != null) {
		return isNullable(firstPropInfo.getJavaType(), propSplit.getValue()) || firstPropInfo.isNullable();
	    } else {
		throw new IllegalArgumentException("Couldn't determine nullability for prop [" + dotNotatedPropName + "] in type [" + entityType + "]" );
	    }
	}
    }

    public Collection<PropertyMetadata> getEntityPPIs(final Class<? extends AbstractEntity<?>> entityType) {
	final EntityMetadata epm = getEntityMetadata(entityType);
	if (epm == null) {
	    throw new IllegalStateException("Missing ppi map for entity type: " + entityType);
	}
	return epm.getProps().values();
    }

    public DomainMetadata getDomainMetadata() {
        return domainMetadata;
    }
}