package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;
import static ua.com.fielden.platform.dao.PropertyCategory.ENTITY_AS_KEY;
import static ua.com.fielden.platform.dao.PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.dao.PropertyCategory.UNION_ENTITY_DETAILS;
import static ua.com.fielden.platform.dao.PropertyCategory.UNION_ENTITY_HEADER;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_BY;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_DATE;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_TRANSACTION_GUID;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.entity.query.fluent.fetch.MSG_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.Collection;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.fetch;

public class EntityRetrievalModel<T extends AbstractEntity<?>> extends AbstractRetrievalModel<T> implements IRetrievalModel<T> {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final Collection<PropertyMetadata> propsMetadata;
    private final boolean isSyntheticEntity;

    public EntityRetrievalModel(final fetch<T> originalFetch, final DomainMetadataAnalyser domainMetadataAnalyser) {
        super(originalFetch, domainMetadataAnalyser);
        this.propsMetadata = domainMetadataAnalyser.getPropertyMetadatasForEntity(getEntityType());
        isSyntheticEntity = isSyntheticEntityType(getEntityType());

        switch (originalFetch.getFetchCategory()) {
        case ALL_INCL_CALC:
            includeAllFirstLevelPropsInclCalc();
            break;
        case ALL:
            includeAllFirstLevelProps();
            break;
        case DEFAULT:
            includeAllFirstLevelPrimPropsAndKey();
            break;
        case KEY_AND_DESC:
            includeKeyAndDescOnly();
            break;
        case ID_AND_VERSTION:
            includeIdAndVersionOnly();
            break;
        case ID_ONLY:
            includeIdOly();
            break;
        case NONE:
        	break;
        default:
            throw new IllegalStateException("Unknown fetch category [" + originalFetch.getFetchCategory() + "]");
        }

        for (final String propName : originalFetch.getExcludedProps()) {
            without(propName);
        }

        for (final String propName : originalFetch.getIncludedProps()) {
            with(propName, false);
        }

        for (final Entry<String, fetch<? extends AbstractEntity<?>>> entry : originalFetch.getIncludedPropsWithModels().entrySet()) {
            final PropertyMetadata ppi = domainMetadataAnalyser.getInfoForDotNotatedProp(getEntityType(), entry.getKey());

            if (ppi.isUnionEntity()) {
                for (final PropertyMetadata pmd : ppi.getComponentTypeSubprops()) {
                    with(pmd.getName(), false);
                }
            }

            with(entry.getKey(), entry.getValue());
        }

        populateProxies();
    }

    private void populateProxies() {
        for (final PropertyMetadata ppi : propsMetadata) {
            // FIXME the following condition needs to be revisited as part of EQL 3 implementation
            final String name = ppi.getName();
            if (!ID.equals(name) &&
                !(KEY.equals(name) && !ppi.affectsMapping()) &&
                !ppi.isCollection() &&
                !name.contains(".") &&
                !containsProp(name) &&
                (isSyntheticEntity || !ppi.isSynthetic())) {
                getProxiedProps().add(name);
            }
        }
    }

    private void includeAllCompositeKeyMembers() {
        for (final PropertyMetadata ppi : propsMetadata) {
            if (ppi.isEntityMemberOfCompositeKey()) {
                with(ppi.getName(), false);
            } else if (ppi.isPrimitiveMemberOfCompositeKey()) {
                with(ppi.getName(), true);
            }
        }
    }

    private void includeAllUnionEntityKeyMembers() {
        for (final PropertyMetadata ppi : propsMetadata) {
            if (ppi.isEntityOfPersistedType()) {
                with(ppi.getName(), false);
            }
        }
    }

    private void includeAllFirstLevelPrimPropsAndKey() {
        for (final PropertyMetadata ppi : propsMetadata) {
            if (!ppi.isCalculated()/* && !ppi.isSynthetic()*/) {
                logger.debug("adding not calculated prop to fetch model: " + ppi.getName());
                final boolean skipEntities = !(ppi.getType() == ENTITY_MEMBER_OF_COMPOSITE_KEY ||
                        ppi.getType() == ENTITY_AS_KEY ||
                        ppi.getType() == UNION_ENTITY_DETAILS ||
                        ppi.getType() == UNION_ENTITY_HEADER);
                with(ppi.getName(), skipEntities);
            }
        }
        includeLastUpdatedByGroupOfProperties();        
    }

    private void includeLastUpdatedByGroupOfProperties() {
        if (AbstractPersistentEntity.class.isAssignableFrom(getEntityType())) {
            with(LAST_UPDATED_BY, true);
            with(LAST_UPDATED_DATE, true);
            with(LAST_UPDATED_TRANSACTION_GUID, true);
        }
    }

    private void includeKeyAndDescOnly() {
        if (isPersistedEntityType(getEntityType())) {
            includeIdAndVersionOnly();
        } else if (isSyntheticBasedOnPersistentEntityType(getEntityType())) {
            with(ID, true);
        } else if (isUnionEntityType(getEntityType())) {
            includeAllFirstLevelProps();
        }

        with(KEY, true);

        if (hasDescProperty(getEntityType())) {
            with(DESC, true);
        }
    }

    private void includeAllFirstLevelProps() {
        for (final PropertyMetadata ppi : propsMetadata) {
            if (ppi.isUnionEntity()) {
                with(ppi.getName(), fetchAll(ppi.getJavaType()));
            } else if (!ppi.isCalculated() && !ppi.isCollection()) {
                with(ppi.getName(), false);
            }
        }
    }

    private void includeAllFirstLevelPropsInclCalc() {
        for (final PropertyMetadata ppi : propsMetadata) {
            if (ppi.isUnionEntity()) {
                with(ppi.getName(), fetchAll(ppi.getJavaType()));
            } else if (!ppi.isCollection()) {
                with(ppi.getName(), false);
            }
        }
    }

    private void includeIdOly() {
        getPrimProps().add(ID);
    }

    private void includeIdAndVersionOnly() {
        if (isEntityType(getKeyType(getEntityType()))) {
            with(ID, true);
        } else if (isPersistedEntityType(getEntityType())) {
            with(ID, true);
            with(VERSION, true);
            if (isActivatableEntityType(getEntityType())) {
                with(ACTIVE, true);
                with(REF_COUNT, true);
            }
            includeLastUpdatedByGroupOfProperties();
        }
    }

    private void with(final String propName, final boolean skipEntities) {
        final PropertyMetadata ppi = getPropMetadata(propName);
        final Class propType = ppi.getJavaType();
        if (propName.equals(KEY) && ppi.isVirtual()) {
            includeAllCompositeKeyMembers();
        } else if (propName.equals(KEY) && isUnionEntityType(getEntityType())) {
            getPrimProps().add(KEY);
            includeAllUnionEntityKeyMembers();
        } else {
            if (AbstractEntity.class.isAssignableFrom(propType)/* && !ppi.isId()*/) {
                if (!skipEntities) {
                    addEntityPropsModel(propName, fetch(propType));
                } else if (ppi.affectsMapping()) {
                    addEntityPropsModel(propName, fetchIdOnly(propType));
                }
            } else if (ppi.isUnionEntity()) {
                System.out.println("                   " + ppi.getName());
            } else {
                final String singleSubpropertyOfCompositeUserTypeProperty = ppi.getSinglePropertyOfCompositeUserType();
                if (singleSubpropertyOfCompositeUserTypeProperty != null) {
                    getPrimProps().add(propName + "." + singleSubpropertyOfCompositeUserTypeProperty);
                }
                getPrimProps().add(propName);
            }
        }
    }

    private void addEntityPropsModel(final String propName, final fetch<?> model) {
        final fetch<?> existingFetch = getEntityProps().get(propName);
        getEntityProps().put(propName, existingFetch != null ? existingFetch.unionWith(model) : model);
    }

    private void with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
        final Class<?> propType = getPropMetadata(propName).getJavaType();

        if (propType != fetchModel.getEntityType()) {
            throw new EqlException(format(MSG_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES, propType, propName, getEntityType(), fetchModel.getEntityType())); 
        }

        addEntityPropsModel(propName, fetchModel);
    }
}