package ua.com.fielden.platform.entity;

import static java.lang.Class.forName;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.security.tokens.TokenUtils.authoriseOpening;

import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_master.exceptions.SimpleMasterException;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.centre.CentreContext;

public class EntityManipulationActionProducer<T extends AbstractEntityManipulationAction> extends DefaultEntityProducerWithContext<T> {
    private final Logger logger = getLogger(getClass());

    private final IAuthorisationModel authorisation;
    private final ISecurityTokenProvider securityTokenProvider;

    @Inject
    public EntityManipulationActionProducer(final EntityFactory factory, final Class<T> entityType, final ICompanionObjectFinder companionFinder, final IAuthorisationModel authorisation, final ISecurityTokenProvider securityTokenProvider) {
        super(factory, entityType, companionFinder);
        this.authorisation = authorisation;
        this.securityTokenProvider = securityTokenProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T provideDefaultValues(final T entity) {
        if (contextNotEmpty()) {
            final Supplier<? extends Class<AbstractEntity<?>>> determineTypeFrom = () -> chosenEntityType().orElseGet(() -> { // if it is empty
                if (selectionCrit() != null) { // use selection criteria type as a fallback
                    return (Class<AbstractEntity<?>>) selectionCrit().getEntityClass();
                }
                final String rootEntityTypeName = (String) getContext().getCustomObject().get("@@rootEntityType"); // then try auxiliary root entity type, if present
                try {
                    return !isEmpty(rootEntityTypeName) ? (Class<AbstractEntity<?>>) forName(rootEntityTypeName) : null; // otherwise return 'null'
                } catch (final ClassNotFoundException ex) {
                    logger.error(format("Could not find class [%s].", rootEntityTypeName), ex);
                    return null; // in case of unrecognised type return 'null'
                }
            });
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
            ).map(entityType -> {
                authoriseOpening(entityType.getSimpleName(), authorisation, securityTokenProvider).ifFailure(Result::throwRuntime);
                return entity.setEntityTypeForEntityMaster(entityType);
            })
            .orElseThrow(() -> new SimpleMasterException(format("Please add selection criteria or current entity to the context of the functional entity with type: %s", entity.getType().getName())));
        }
        return entity;
    }

}