package ua.com.fielden.platform.entity.query;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
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
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo.getEntityTypeInfo;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.ENTITY_AS_KEY;
import static ua.com.fielden.platform.entity.query.metadata.PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.Collection;
import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;
import ua.com.fielden.platform.types.tuples.T2;

public class EntityRetrievalModel<T extends AbstractEntity<?>> extends AbstractRetrievalModel<T> {
    private final Logger logger = getLogger(this.getClass());
    private final Collection<PropertyMetadata> propsMetadata;
    private final EntityTypeInfo<? super T> entityTypeInfo;

    public EntityRetrievalModel(final fetch<T> originalFetch, final DomainMetadataAnalyser domainMetadataAnalyser) {
        this(originalFetch, domainMetadataAnalyser, true);
    }

    EntityRetrievalModel(final fetch<T> originalFetch, final DomainMetadataAnalyser domainMetadataAnalyser, final boolean topLevel) {
        super(originalFetch, domainMetadataAnalyser, topLevel);
        this.propsMetadata = domainMetadataAnalyser.getPropertyMetadatasForEntity(getEntityType());
        entityTypeInfo = getEntityTypeInfo(getEntityType());
        
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
        case ID_AND_VERSION:
            includeIdAndVersionOnly();
            break;
        case ID_ONLY:
            includeIdOnly();
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
                    (entityTypeInfo.category == QUERY_BASED || !ppi.isSynthetic())) {
                getProxiedProps().add(name);
            }
        }
    }

    private void includeAllCompositeKeyMembers() {
        for (final T2<String, Class<?>> compositeKeyMember : entityTypeInfo.compositeKeyMembers) {
            with(compositeKeyMember._1, !isEntityType(compositeKeyMember._2));
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
            if (!ppi.isCalculated()/* && !ppi.isSynthetic()*/ && !ppi.isCollection() && !ppi.isPure()) {
                final boolean skipEntities = !(ppi.getCategory() == ENTITY_MEMBER_OF_COMPOSITE_KEY || ppi.getCategory() == ENTITY_AS_KEY); 
                with(ppi.getName(), skipEntities);
            }
        }
    }

    private void includeLastUpdatedByGroupOfProperties() {
        if (AbstractPersistentEntity.class.isAssignableFrom(getEntityType())) {
            with(LAST_UPDATED_BY, true);
            addPrimProp(LAST_UPDATED_DATE);
            addPrimProp(LAST_UPDATED_TRANSACTION_GUID);
        }
    }

    private void includeKeyAndDescOnly() {
        if (isPersistedEntityType(getEntityType())) {
            includeIdAndVersionOnly();
        } else if (isSyntheticBasedOnPersistentEntityType(getEntityType())) {
            addPrimProp(ID);
        }

        with(KEY, true);

        if (hasDescProperty(getEntityType())) {
            addPrimProp(DESC);
        }
    }

    private void includeAllFirstLevelProps() {
        for (final PropertyMetadata ppi : propsMetadata) {
            if (!ppi.isCalculated() /*&& !ppi.isCalculatedCompositeUserTypeHeader() */&& !ppi.isCollection() && !ppi.isPure()) {
                with(ppi.getName(), false);
            }
        }
    }

    private void includeAllFirstLevelPropsInclCalc() {
        for (final PropertyMetadata ppi : propsMetadata) {
            if (!ppi.isCollection() && !ppi.isPure()) {
                with(ppi.getName(), false);
            }
        }
    }

    private void includeIdOnly() {
        addPrimProp(ID);
    }

    private void includeIdAndVersionOnly() {
        if (isPersistedEntityType(getEntityType())) {
            addPrimProp(ID);
            addPrimProp(VERSION);
            if (isActivatableEntityType(getEntityType())) {
                addPrimProp(ACTIVE);
                addPrimProp(REF_COUNT);
            }
            includeLastUpdatedByGroupOfProperties();
        } else if (isEntityType(getKeyType(getEntityType()))) {
            addPrimProp(ID);
        }
    }

    private void with(final String propName, final boolean skipEntities) {
        final PropertyMetadata ppi = getPropMetadata(propName);
        if (ppi == null) {
            addPrimProp(propName); // for situations, where PropertyMetadata is missing, but this is considered legal -- just add it as primitive property
            return;
        }
        final Class propType = ppi.getJavaType();

        if (ppi.isCompositeKeyExpression()) {
            includeAllCompositeKeyMembers();
        } else if (propName.equals(KEY) && isUnionEntityType(getEntityType())) {
            addPrimProp(KEY);
            includeAllUnionEntityKeyMembers();
        } else {
            if (isEntityType(propType) && !PropertyDescriptor.class.equals(propType)/* && !ppi.isId()*/) {
                if (!skipEntities) {
                    if (ppi.isUnionEntity()) {
                        with(propName, fetchAll(propType));
                    } else {
                        with(propName, fetch(propType));
                    }
                } else if (ppi.affectsMapping()) {
                    with(propName, fetchIdOnly(propType));
                }
            } else {
                final String singleSubpropertyOfCompositeUserTypeProperty = ppi.getSinglePropertyOfCompositeUserType();
                if (singleSubpropertyOfCompositeUserTypeProperty != null) {
                    addPrimProp(propName + "." + singleSubpropertyOfCompositeUserTypeProperty);
                }
                addPrimProp(propName);
            }
        }
    }

    private void with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
        final PropertyMetadata ppi = getDomainMetadataAnalyser().getInfoForDotNotatedProp(getEntityType(), propName);

        if (ppi.getJavaType() != fetchModel.getEntityType()) {
            throw new EqlException(format(MSG_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES, ppi.getJavaType(), propName, getEntityType(), fetchModel.getEntityType()));
        }
        
        if (ppi.isUnionEntity()) {
            for (final PropertyMetadata pmd : ppi.getComponentTypeSubprops()) {
                addPrimProp(pmd.getName()); // is added here as primitive prop only to avoid its removal in EntQuery.adjustAccordingToFetchModel
            }
        }
        
        final EntityRetrievalModel<?> existingFetch = getRetrievalModels().get(propName);
        fetch<?> finalFetch = existingFetch != null ? existingFetch.originalFetch.unionWith(fetchModel) : fetchModel;
        addEntityPropFetchModel(propName, new EntityRetrievalModel<>(finalFetch, getDomainMetadataAnalyser(), false));
    }
    
    public boolean isFetchIdOnly() {
        return getPrimProps().size() == 1 && getRetrievalModels().size() == 0 && containsProp(ID);
    }
}