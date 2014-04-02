package ua.com.fielden.platform.dao.eql;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.reflection.Finder;
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

    public <ET extends AbstractEntity<?>> EntityMetadata<ET> getEntityMetadata(final Class<ET> entityType) {
        if (entityType == null || !AbstractEntity.class.isAssignableFrom(entityType) || EntityAggregates.class.equals(entityType)) {
            return null;
        }

        final EntityMetadata<ET> existing = entityMetadataMap.get(entityType);

        if (existing != null) {
            return existing;
        } else {
            try {
                final EntityMetadata<ET> newOne = domainMetadata.generateEntityMetadata(entityType);
                entityMetadataMap.put(entityType, newOne);
                return newOne;
            } catch (final Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage() + "entityType = " + entityType);
            }
        }
    }

    /**
     * Retrieves persistence info for entity property, which is explicitly persisted within this entity type.
     * 
     * @param entityType
     * @param propName
     * @return
     */
    public <ET extends AbstractEntity<?>> PropertyMetadata getPropPersistenceInfoExplicitly(final Class<ET> entityType, final String propName) {
        final EntityMetadata<ET> map = getEntityMetadata(entityType);
        return map != null ? map.getProps().get(propName) : null;
    }

    /**
     * Retrieves persistence info for entity property or its nested subproperty.
     * 
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

    public boolean isNullable(final Class<? extends AbstractEntity<?>> entityType, final String dotNotatedPropName) {
        final PropertyMetadata simplePropInfo = getPropPersistenceInfoExplicitly(entityType, dotNotatedPropName);
        if (simplePropInfo != null) {
            return simplePropInfo.isNullable();
        } else {
            final Pair<String, String> propSplit = EntityUtils.splitPropByFirstDot(dotNotatedPropName);
            final PropertyMetadata firstPropInfo = getPropPersistenceInfoExplicitly(entityType, propSplit.getKey());
            if (firstPropInfo != null && firstPropInfo.getJavaType() != null) {
                return isNullable(firstPropInfo.getJavaType(), propSplit.getValue()) || firstPropInfo.isNullable();
            } else {
                throw new IllegalArgumentException("Couldn't determine nullability for prop [" + dotNotatedPropName + "] in type [" + entityType + "]");
            }
        }
    }

    public Collection<PropertyMetadata> getPropertyMetadatasForEntity(final Class<? extends AbstractEntity<?>> entityType) {
        final EntityMetadata epm = getEntityMetadata(entityType);
        if (epm == null) {
            throw new IllegalStateException("Missing ppi map for entity type: " + entityType);
        }
        return epm.getProps().values();
    }

    public DomainMetadata getDomainMetadata() {
        return domainMetadata;
    }

    public Set<String> getLeafPropsFromFirstLevelProps(final String parentProp, final Class<? extends AbstractEntity<?>> entityType, final Set<String> firstLevelProps) {
        final Set<String> result = new HashSet<String>();

        for (final String prop : firstLevelProps) {
            final PropertyMetadata propMetadata = getPropPersistenceInfoExplicitly(entityType, prop);
            if (propMetadata.isEntityOfPersistedType()) {
                final Set<String> keyProps = new HashSet<String>(Finder.getFieldNames(Finder.getKeyMembers(propMetadata.getJavaType())));
                if (keyProps.size() > 1) {
                    result.addAll(getLeafPropsFromFirstLevelProps(prop, propMetadata.getJavaType(), keyProps));
                } else {
                    result.add((parentProp != null ? (parentProp + ".") : "") + prop);
                }
            } else {
                result.add((parentProp != null ? (parentProp + ".") : "") + prop);
            }
        }

        return result;
    }
}