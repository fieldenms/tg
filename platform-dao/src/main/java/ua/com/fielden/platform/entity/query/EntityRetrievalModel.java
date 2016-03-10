package ua.com.fielden.platform.entity.query;

import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyCategory;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;

public class EntityRetrievalModel<T extends AbstractEntity<?>> extends AbstractRetrievalModel<T> implements IRetrievalModel<T> {
    transient private final Logger logger = Logger.getLogger(this.getClass());
    
    public EntityRetrievalModel(final fetch<T> originalFetch, final DomainMetadataAnalyser domainMetadataAnalyser) {
        super(originalFetch, domainMetadataAnalyser);

        switch (originalFetch.getFetchCategory()) {
        case ALL:
            includeAllFirstLevelProps();
            break;
        case ALL_INCL_CALC:
            includeAllFirstLevelPropsInclCalc();
            break;
        case MINIMAL:
            includeAllFirstLevelPrimPropsAndKey();
            break;
        case KEY_AND_DESC:
            includeKeyAndDescOnly();
            break;
        case NONE:
            if (isPersistedEntityType(getEntityType())) {
                includeIdAndVersionOnly();
            } else if (isEntityType(getKeyType(getEntityType()))) {
                with(ID, true);
            }
            break;
        default:
            throw new IllegalStateException("Unknown fetch category [" + originalFetch.getFetchCategory() + "]");
        }

        for (final String propName : originalFetch.getExcludedProps()) {
            without(propName);
        }

        for (final String propName : originalFetch.getIncudedProps()) {
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
        for (final PropertyMetadata ppi : getDomainMetadataAnalyser().getPropertyMetadatasForEntity(getEntityType())) {
            // FIXME the following condition needs to be revisited as part of EQL 3 implementation
            final String name = ppi.getName();
            if (!ID.equals(name) && 
                !KEY.equals(name) && 
                !ppi.isCollection() &&
                !name.endsWith(".amount") &&
                !ppi.isSynthetic() &&
                !containsProp(name)) {
                
                if (ppi.isEntityOfPersistedType()) {
                    if (ppi.isCalculated()) {
                        getProxiedPropsWithoutId().add(name);
                    } else {
                        getProxiedProps().add(name);
                    }
                } else {
                    getProxiedPrimProps().add(name);
                }
            }
        }
    }

    private void includeAllCompositeKeyMembers() {
        for (final PropertyMetadata ppi : getDomainMetadataAnalyser().getPropertyMetadatasForEntity(getEntityType())) {
            if (ppi.isEntityMemberOfCompositeKey()) {
                with(ppi.getName(), false);
            } else if (ppi.isPrimitiveMemberOfCompositeKey()) {
                with(ppi.getName(), true);
            }
        }
    }

    private void includeAllUnionEntityKeyMembers() {
        for (final PropertyMetadata ppi : getDomainMetadataAnalyser().getPropertyMetadatasForEntity(getEntityType())) {
            if (ppi.isEntityOfPersistedType()) {
                with(ppi.getName(), false);
            }
        }
    }

    private void includeAllFirstLevelPrimPropsAndKey() {
        for (final PropertyMetadata ppi : getDomainMetadataAnalyser().getPropertyMetadatasForEntity(getEntityType())) {
            if (!ppi.isCalculated()/* && !ppi.isSynthetic()*/) {
                logger.debug("adding not calculated prop to fetch model: " + ppi.getName());
                final boolean skipEntities = !(ppi.getType().equals(PropertyCategory.ENTITY_MEMBER_OF_COMPOSITE_KEY) ||
                        ppi.getType().equals(PropertyCategory.ENTITY_AS_KEY) ||
                        ppi.getType().equals(PropertyCategory.UNION_ENTITY_DETAILS) ||
                        ppi.getType().equals(PropertyCategory.UNION_ENTITY_HEADER));
                with(ppi.getName(), skipEntities);
            }
        }
    }

    private void includeKeyAndDescOnly() {
        if (isPersistedEntityType(getEntityType())) {
            includeIdAndVersionOnly();
        }

        if (isUnionEntityType(getEntityType())) {
            includeAllFirstLevelProps();
        }

        with(KEY, true);

        if (hasDescProperty(getEntityType())) {
            with(DESC, true);
        }
    }

    private void includeAllFirstLevelProps() {
        for (final PropertyMetadata ppi : getDomainMetadataAnalyser().getPropertyMetadatasForEntity(getEntityType())) {
            if (ppi.isUnionEntity()) {
                with(ppi.getName(), fetchAll(ppi.getJavaType()));
            } else if (!ppi.isCalculated() && !ppi.isCollection()) {
                with(ppi.getName(), false);
            }
        }
    }

    private void includeAllFirstLevelPropsInclCalc() {
        for (final PropertyMetadata ppi : getDomainMetadataAnalyser().getPropertyMetadatasForEntity(getEntityType())) {
            if (ppi.isUnionEntity()) {
                with(ppi.getName(), fetchAll(ppi.getJavaType()));
            } else if (!ppi.isCollection()) {
                with(ppi.getName(), false);
            }
        }
    }

    private void includeIdAndVersionOnly() {
        with(ID, true);
        with(VERSION, true);
    }

    private void with(final String propName, final boolean skipEntities) {
        final PropertyMetadata ppi = getPropMetadata(propName);
        final Class propType = ppi.getJavaType();
        if (propName.equals("key") && ppi.isVirtual()) {
            includeAllCompositeKeyMembers();
        } else if (propName.equals("key") && isUnionEntityType(getEntityType())) {
            getPrimProps().add("key");
            includeAllUnionEntityKeyMembers();
        } else {
            if (AbstractEntity.class.isAssignableFrom(propType)/* && !ppi.isId()*/) {
                if (!skipEntities) {
                    addEntityPropsModel(propName, fetch(propType));
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
        final Class propType = getPropMetadata(propName).getJavaType();

        if (propType != fetchModel.getEntityType()) {
            throw new IllegalArgumentException("Mismatch between actual type [" + propType + "] of property [" + propName + "] in entity type [" + getEntityType()
                    + "] and its fetch model type [" + fetchModel.getEntityType() + "]!");
        }

        if (AbstractEntity.class.isAssignableFrom(fetchModel.getEntityType())) {
            addEntityPropsModel(propName, fetchModel);
        } else {
            throw new IllegalArgumentException(propName + " has fetch model for type " + fetchModel.getEntityType().getName() + ". Fetch model with entity type is required.");
        }
    }
}