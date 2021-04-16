package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.security.tokens.TokenUtils.authoriseOpening;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.traversePropPath;
import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.CentreContext;

public class EntityManipulationActionProducer<T extends AbstractEntityManipulationAction> extends DefaultEntityProducerWithContext<T> {

    @Inject
    public EntityManipulationActionProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder) {
        super(factory, entityType, companionFinder);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T provideDefaultValues(final T entity) {
        if (contextNotEmpty()) {
            final AbstractEntity<?> currEntity = currentEntity();
            final EnhancedCentreEntityQueryCriteria<?, ?> selCrit = selectionCrit();
            final Class<AbstractEntity<?>> entityType =
                computation().map( computation -> {
                        final Object computed = computation.apply(entity, (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext());
                        // it is by convention that a computational context may return custom entity type of tg-entity-master to be displayed
                        // if the type of the result if either Class or T2 representing a tuple of Type (Class) and ID (Long)
                        if (computed instanceof Class) {
                            return (Class<AbstractEntity<?>>) computed;
                        } else if (computed instanceof T2) {
                            final T2<Class<AbstractEntity<?>>, Long> typeAndId = (T2<Class<AbstractEntity<?>>, Long>) computed;
                            return typeAndId._1;
                        } else {
                            return determineEntityType(currEntity, selCrit, chosenProperty());
                        }
                    })
                .orElse(determineEntityType(currEntity, selCrit, chosenProperty()));

            if (entityType == null) {
                throw new IllegalStateException("Please add selection criteria or current entity to the context of the functional entity with type: " + entity.getType().getName());
            } else {
                authoriseOpening(entityType.getSimpleName(), authorisation, securityTokenProvider).ifFailure(Result::throwRuntime);
                entity.setEntityTypeForEntityMaster(entityType);
            }
        }
        return entity;
    }

    /**
     * Determines the precise type based on {@code currEntity} and {@code selCrit} to determine what Entity Master should be displayed:
     * <ul>
     * <li>a) {@code currEntity} depending on whether it is {@link EntityNewAction} or {@link EntityEditAction} (refer {@link #determineBaseEntityType(Class)}, or
     * <li>b) {@code selCrit}.
     * </ul>
     * @param currEntity
     * @param selCrit
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Class<AbstractEntity<?>> determineEntityType(final AbstractEntity<?> currEntity, final EnhancedCentreEntityQueryCriteria<?, ?> selCrit, final String chosenProperty) {
        return traversePropPath(currEntity, chosenProperty)
                .findFirst()
                .map(t2 -> t2._1)
                .map(propPath -> determineBaseEntityType(getOriginalType(determinePropertyType(currEntity.getType(), dslName(propPath)))))
                .orElseGet(() -> {
                    return currEntity != null ? determineBaseEntityType(getOriginalType(currEntity.getType())) :
                        selCrit != null ? (Class<AbstractEntity<?>>) selCrit.getEntityClass() : null;
                });

    }

    /**
     * Returns the base type of {@code entityType} if it is a synthetic entity based on a persistent entity.
     * Otherwise, returns {@code entityType}.
     *
     * @param entityType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Class<AbstractEntity<?>> determineBaseEntityType(final Class<AbstractEntity<?>> entityType) {
        if (isSyntheticBasedOnPersistentEntityType(entityType)) {
            // for the cases where EntityEditAction is used for opening SyntheticBasedOnPersistentEntity we explicitly use base type;
            // however this is not the case for StandardActions.EDIT_ACTION because of computation existence that returns entityType.
            return (Class<AbstractEntity<?>>) entityType.getSuperclass();
        }
        return entityType;
    }
}