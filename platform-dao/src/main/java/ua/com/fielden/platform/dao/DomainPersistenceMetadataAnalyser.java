package ua.com.fielden.platform.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class DomainPersistenceMetadataAnalyser {
    private final Map<Class<? extends AbstractEntity<?>>, EntityMetadata> hibTypeInfosMap = new HashMap<Class<? extends AbstractEntity<?>>, EntityMetadata>();

    private final DomainPersistenceMetadata domainPersistenceMetadata;

    public DomainPersistenceMetadataAnalyser(final DomainPersistenceMetadata domainPersistenceMetadata) {
	super();
	this.domainPersistenceMetadata = domainPersistenceMetadata;
	hibTypeInfosMap.putAll(domainPersistenceMetadata.getHibTypeInfosMap());
    }

    private EntityMetadata getEntityPersistenceMetadata(final Class<? extends AbstractEntity<?>> entityType) {
	if (entityType == null || !AbstractEntity.class.isAssignableFrom(entityType) || EntityAggregates.class.equals(entityType)) {
	    return null;
	}

	final EntityMetadata existing = hibTypeInfosMap.get(entityType);

	if (existing != null) {
	    return existing;
	} else {
	    try {
		final EntityMetadata newOne = domainPersistenceMetadata.generateEntityPersistenceMetadata(entityType);
		hibTypeInfosMap.put(entityType, newOne);
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
	final EntityMetadata map = getEntityPersistenceMetadata(entityType);
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
	final EntityMetadata epm = getEntityPersistenceMetadata(entityType);
	if (epm == null) {
	    throw new IllegalStateException("Missing ppi map for entity type: " + entityType);
	}
	return epm.getProps().values();
    }

    public EntityMetadata getEntityPersistenceMetadate(final Class<? extends AbstractEntity<?>> entityType) {
	final EntityMetadata epm = getEntityPersistenceMetadata(entityType);
	if (epm == null) {
	    throw new IllegalStateException("Missing entity persistence metadata for entity type: " + entityType);
	}
	return epm;
    }

    public DomainPersistenceMetadata getDomainPersistenceMetadata() {
        return domainPersistenceMetadata;
    }
}