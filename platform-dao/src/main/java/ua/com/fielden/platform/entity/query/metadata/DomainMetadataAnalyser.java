package ua.com.fielden.platform.entity.query.metadata;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.eql.meta.EntityTypeInfo.getEntityTypeInfoPair;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class DomainMetadataAnalyser {
    private final Logger logger = getLogger(DomainMetadataAnalyser.class);
    private final Map<Class<? extends AbstractEntity<?>>, AbstractEntityMetadata> entityMetadataMap = new HashMap<>();
    private final DomainMetadata domainMetadata;

    public DomainMetadataAnalyser(final DomainMetadata domainMetadata) {
        this.domainMetadata = domainMetadata;
        entityMetadataMap.putAll(domainMetadata.getPersistedEntityMetadataMap());
        entityMetadataMap.putAll(domainMetadata.getModelledEntityMetadataMap());
        entityMetadataMap.putAll(domainMetadata.getPureEntityMetadataMap());
    }

    public <ET extends AbstractEntity<?>> AbstractEntityMetadata<ET> getEntityMetadata(final Class<ET> entityType) {
        if (entityType == null || !AbstractEntity.class.isAssignableFrom(entityType) || EntityAggregates.class.equals(entityType)) {
            return null;
        }

        final AbstractEntityMetadata<ET> existing = entityMetadataMap.get(entityType);

        if (existing != null) {
            return existing;
        } else {
            try {
                final AbstractEntityMetadata<ET> newOne;
                switch (getEntityTypeInfo(entityType).category) {
                case PERSISTENT:
                    newOne = domainMetadata.generatePersistedEntityMetadata(entityType, getEntityTypeInfo(entityType));
                    break;
                case QUERY_BASED:
                    newOne = domainMetadata.generateModelledEntityMetadata(getEntityTypeInfoPair(entityType));
                    break;
                case UNION:
                    newOne = domainMetadata.generateUnionedEntityMetadata(getEntityTypeInfoPair(entityType));
                    break;
                default:
                    newOne = domainMetadata.generatePureEntityMetadata(entityType);
                }

                entityMetadataMap.put(entityType, newOne);

                return newOne;
            } catch (final Exception ex) {
                final String msg = format("Error while building metadata for type [%s].", entityType.getName());
                logger.error(msg, ex);
                throw new EqlException(msg, ex);
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
        final AbstractEntityMetadata<ET> map = getEntityMetadata(entityType);
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

    public <ET extends AbstractEntity<?>> SortedMap<String, PropertyMetadata> getPropertyMetadatasForEntity(final Class<ET> entityType) {
        final AbstractEntityMetadata<ET> epm = getEntityMetadata(entityType);
        if (epm == null) {
            throw new IllegalStateException("Missing ppi map for entity type: " + entityType);
        }
        return Collections.unmodifiableSortedMap(epm.getProps());
    }
}