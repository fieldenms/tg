package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.traversePropPath;
import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;

import java.util.function.Supplier;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
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
            final Supplier<? extends Class<AbstractEntity<?>>> determineTypeFrom = () -> determineEntityType(currentEntity(), chosenProperty(), selectionCrit());
            ofNullable(
                computation()
                .map(computation -> {
                    final Object computed = computation.apply(entity, (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext());
                    // it is by convention that a computational context may return custom entity type of tg-entity-master to be displayed
                    // if the type of the result if either Class or T2 representing a tuple of Type (Class) and ID (Long)
                    if (computed instanceof Class) {
                        return (Class<AbstractEntity<?>>) computed;
                    } else if (computed instanceof T2) {
                        final T2<Class<AbstractEntity<?>>, Long> typeAndId = (T2<Class<AbstractEntity<?>>, Long>) computed;
                        return typeAndId._1;
                    } else {
                        return determineTypeFrom.get();
                    }
                })
                .orElseGet(determineTypeFrom)
            ).map(entityType -> entity.setEntityTypeForEntityMaster(entityType))
             .orElseThrow(() -> new SimpleMasterException(format("Please add selection criteria or current entity to the context of the functional entity with type: %s", entity.getType().getName())));
        }
        return entity;
    }

    /**
     * Determines the precise type based on {@code currentEntity}, {@code chosenProperty} and {@code selectionCrit}.
     * 
     * @param currentEntity
     * @param chosenProperty
     * @param selectionCrit
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Class<AbstractEntity<?>> determineEntityType(final AbstractEntity<?> currentEntity, final String chosenProperty, final EnhancedCentreEntityQueryCriteria<?, ?> selectionCrit) {
        return traversePropPath(currentEntity, chosenProperty) // traverse entity-typed paths and values
            .findFirst() // find first (most full) pair, if any
            .map(pathAndValueOpt -> determineActualEntityType(currentEntity.getType(), pathAndValueOpt._1)) // take the path only and determine actual entity type from that path
            .orElseGet(() -> { // if it is empty
                return selectionCrit != null ? (Class<AbstractEntity<?>>) selectionCrit.getEntityClass() : null; // use selection criteria type as a fallback (or otherwise return 'null')
            });
    }

    /**
     * Determines actual (i.e. not generated / synthetic) type from entity-typed property path ({@code entityTypedPropPath}) in root entity type ({@code rootType}).
     * 
     * @param rootType -- root entity type
     * @param entityTypedPropPath -- dot-notated entity-typed property path defined in {@code rootType}; "" is supported meaning root type itself; the path can be taken from {@link EntityUtils#traversePropPath(AbstractEntity, String)}
     * @return
     */
    public static Class<AbstractEntity<?>> determineActualEntityType(final Class<? extends AbstractEntity<?>> rootType, final String entityTypedPropPath) {
        return determineBaseEntityType(getOriginalType(determinePropertyType(rootType, dslName(entityTypedPropPath))));
    }

    /**
     * Returns the base type of {@code entityType} if it is a synthetic entity based on a persistent entity.
     * Otherwise, returns {@code entityType}.
     *
     * @param entityType
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Class<AbstractEntity<?>> determineBaseEntityType(final Class<AbstractEntity<?>> entityType) {
        if (isSyntheticBasedOnPersistentEntityType(entityType)) {
            // for the cases where EntityEditAction is used for opening SyntheticBasedOnPersistentEntity we explicitly use base type;
            // however this is not the case for StandardActions.EDIT_ACTION because of computation existence that returns entityType.
            return (Class<AbstractEntity<?>>) entityType.getSuperclass();
        }
        return entityType;
    }
}