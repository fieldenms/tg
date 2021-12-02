package ua.com.fielden.platform.entity;

import java.util.function.Supplier;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_master.exceptions.SimpleMasterException;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Abstract producer for entities those extend the {@link EntityEditAction}
 *
 * @author TG Team
 *
 * @param <T>
 */
public class AbstractEntityEditActionProducer<T extends EntityEditAction> extends EntityManipulationActionProducer<T> {
    static final String NOTHING_TO_OPEN_MSG = "There is nothing to open.";
    private static final Supplier<? extends RuntimeException> NOTHING_TO_OPEN_EXCEPTION_SUPPLIER = () -> new SimpleMasterException(NOTHING_TO_OPEN_MSG);

    public AbstractEntityEditActionProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder, final IAuthorisationModel authorisation, final ISecurityTokenProvider securityTokenProvider) {
        super(factory, entityType, companionFinder, authorisation, securityTokenProvider);
    }

    @Override
    protected T provideDefaultValues(final T entity) {
        final T editedEntity = super.provideDefaultValues(entity);
        if (contextNotEmpty()) {
            // in a polymorphic UI case, IDs may come from a computational context
            // it is by convention that a computational context may return a value of type T2 representing a tuple of Type (Class) and ID (Long)
            final Long id = computation()
                .map(computation -> computation.apply(entity, (CentreContext<AbstractEntity<?>, AbstractEntity<?>>) getContext()))
                .filter(computed -> computed instanceof T2)
                .map(computed -> ((T2<Class<AbstractEntity<?>>, Long>) computed)._2)
                .orElseGet(() -> chosenEntityId(editedEntity.getEntityTypeAsClass())
                    .orElseThrow(NOTHING_TO_OPEN_EXCEPTION_SUPPLIER)
                );
            editedEntity.setEntityId(id.toString());
        }
        return editedEntity;
    }

}