package ua.com.fielden.platform.web.ref_hierarchy;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.web.centre.CentreContext;

import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyLevel.REFERENCE_INSTANCE;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.*;

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
    protected ReferenceHierarchy provideDefaultValues(final ReferenceHierarchy action) {
        final AbstractEntity<?> referenceEntity = extractReferenceEntity(action);
        if (referenceEntity != null) {
            // we need to be smart about getting the type as it may be a synthetic entity that represents a persistent entity
            // and so, we really need to handle this case here
            final Class<? extends AbstractEntity<?>> entityType;
            if (isPersistedEntityType(referenceEntity.getType())) {
                entityType = referenceEntity.getType();
                action.setRefEntityType(entityType.getName());
            } else if (isSyntheticBasedOnPersistentEntityType(referenceEntity.getType())) {
                entityType = (Class<? extends AbstractEntity<?>>) referenceEntity.getType().getSuperclass();
                action.setRefEntityType(entityType.getName());
            } else {
                throw new ReflectionException(ERR_UNSUPPORTED_ENTITY_TYPE.formatted(referenceEntity.getType().getSimpleName()));
            }
            final fetch fetchModel = fetchKeyAndDescOnly(entityType);
            final AbstractEntity<?> refetchedEntity = co(entityType).findById(referenceEntity.getId(), hasDescProperty(entityType) ? fetchModel.with(DESC) : fetchModel);
            action.setRefEntityId(refetchedEntity.getId());
            action.setLoadedHierarchyLevel(REFERENCE_INSTANCE);
            action.setTitle(refetchedEntity.getKey() + (StringUtils.isEmpty(refetchedEntity.getDesc()) ? "" : ": " + refetchedEntity.getDesc()));
        }
        return action;
    }

    private AbstractEntity<?> extractReferenceEntity(final ReferenceHierarchy action) {
        // Computation has the highest precedence if it is present
        if (computation().isPresent()) {
            final Object computed = computation().get().apply(action, (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext());
            // computation function should return an entity
            if (computed instanceof AbstractEntity<?>) {
                return (AbstractEntity<?>) computed;
            } else {
                throw failuref(ERR_COMPUTED_VALUE_HAS_WRONG_TYPE);
            }
        } else if (currentEntityNotEmpty()) {
            return currentEntity();
        } else if (selectedEntitiesNotEmpty()) {
            return selectedEntities().getFirst();
        } else if (masterEntityNotEmpty()) {
            if (chosenPropertyEmpty()) {
                return masterEntity();
            } else {
                // If the master entity is present and a chosen property is of an entity type, then return entity even if it is invalid.
                // Otherwise, inform that only an existent entity can be used for reference hierarchy.
                // If the property editor is not of an entity type, then the master entity is used for the reference hierarchy.
                final var chosenPropName = chosenProperty();
                final Class<?> propType = determinePropertyType(masterEntity().getType(), chosenPropName);
                if (propType != null && !AbstractEntity.class.isAssignableFrom(propType)) {
                    return masterEntity();
                } else if (masterEntity().getProperty(chosenPropName).getLastAttemptedValue() == null ||
                           ((AbstractEntity<?>) masterEntity().getProperty(chosenPropName).getLastAttemptedValue()).getId() == null) {
                    throw failure(ERR_ENTITY_EDITOR_VALUE_SHOULD_EXIST);
                }
                return (AbstractEntity<?>) masterEntity().getProperty(chosenPropName).getLastAttemptedValue();
            }
        }
        return null;
    }

}
