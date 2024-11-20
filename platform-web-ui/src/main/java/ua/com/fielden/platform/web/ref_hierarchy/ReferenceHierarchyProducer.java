package ua.com.fielden.platform.web.ref_hierarchy;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyLevel.REFERENCE_INSTANCE;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * The producer for {@link ReferenceHierarchy} entity used to initialize {@link ReferenceHierarchy} instance with data that was calculated based on data received from client.
 *
 * @author TG Team
 *
 */
public class ReferenceHierarchyProducer extends DefaultEntityProducerWithContext<ReferenceHierarchy> {

    public static final String ERR_ENTITY_EDITOR_VALUE_SHOULD_EXIST = "Please enter an existent entity or choose one from the drop-down list.";
    public static final String ERR_UNSUPPORTED_ENTITY_TYPE = "Unsupported entity type [%s] for Reference Hierarchy.";
    public static final String ERR_COMPUTED_VALUE_HAS_WRONG_TYPE = "The computed value should be of an entity type.";

    @Inject
    public ReferenceHierarchyProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, ReferenceHierarchy.class, companionFinder);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected ReferenceHierarchy provideDefaultValues(final ReferenceHierarchy entity) {
        final AbstractEntity<?> selectedEntity = extractReferenceEntity(entity);
        if (selectedEntity != null) {
            // we need to be smart about getting the type as it may be a synthetic entity that represents a persistent entity
            // and so, we really need to handle this case here
            final Class<? extends AbstractEntity<?>> entityType;
            if (isPersistedEntityType(selectedEntity.getType())) {
                entityType = selectedEntity.getType();
                entity.setRefEntityType(entityType.getName());
            } else if (isSyntheticBasedOnPersistentEntityType(selectedEntity.getType())) {
                entityType = (Class<? extends AbstractEntity<?>>) selectedEntity.getType().getSuperclass();
                entity.setRefEntityType(entityType.getName());
            } else {
                throw new ReflectionException(format(ERR_UNSUPPORTED_ENTITY_TYPE, selectedEntity.getType().getSimpleName()));
            }
            final fetch fetchModel = fetchKeyAndDescOnly(entityType);
            final AbstractEntity<?> refetchedEntity = co(entityType).findById(selectedEntity.getId(), hasDescProperty(entityType) ? fetchModel.with("desc") : fetchModel);
            entity.setRefEntityId(refetchedEntity.getId());
            entity.setLoadedHierarchyLevel(REFERENCE_INSTANCE);
            entity.setActiveOnly(true);
            entity.setTitle(refetchedEntity.getKey() + (StringUtils.isEmpty(refetchedEntity.getDesc()) ? "" : ": " + refetchedEntity.getDesc()));
        }
        return entity;
    }

    private AbstractEntity<?> extractReferenceEntity(final ReferenceHierarchy entity) {
        //Computation has the highest precedence if it is present
        if (computation().isPresent()) {
            final Object computed = computation().get().apply(entity, (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext());
            // computation function should return an entity
            if (computed instanceof AbstractEntity<?>) {
                return (AbstractEntity<?>) computed;
            } else {
                throw failuref(ERR_COMPUTED_VALUE_HAS_WRONG_TYPE);
            }
        } else if (currentEntityNotEmpty()) {
            return currentEntity();
        } else if (selectedEntitiesNotEmpty()) {
            return selectedEntities().get(0);
        } else if (masterEntityNotEmpty()) {
            if (chosenPropertyEmpty()) {
                return masterEntity();
            } else {
                //If master entity is present and chosen property is of an entity type then return entity even if it is invalid,
                //otherwise inform that only an existent entity can be used for reference hierarchy.
                //If property editor is not of an entity type then use master entity for reference hierarchy.
                final Class<?> propType = determinePropertyType(masterEntity().getType(), chosenProperty());
                if (!AbstractEntity.class.isAssignableFrom(propType)) {
                    return masterEntity();
                } else if (masterEntity().getProperty(chosenProperty()).getLastAttemptedValue() == null ||
                        ((AbstractEntity<?>) masterEntity().getProperty(chosenProperty()).getLastAttemptedValue()).getId() == null) {
                    throw failuref(ERR_ENTITY_EDITOR_VALUE_SHOULD_EXIST);
                }
                return (AbstractEntity<?>) masterEntity().getProperty(chosenProperty()).getLastAttemptedValue();
            }
        }
        return null;
    }
}
