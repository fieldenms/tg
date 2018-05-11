package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager.DEFAULT_CONFIG_TITLE;
import static ua.com.fielden.platform.error.Result.failure;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link CentreConfigDeleteAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigDeleteActionProducer extends DefaultEntityProducerWithContext<CentreConfigDeleteAction> {
    
    @Inject
    public CentreConfigDeleteActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigDeleteAction.class, companionFinder);
    }
    
    @Override
    protected CentreConfigDeleteAction provideDefaultValues(final CentreConfigDeleteAction entity) {
        if (contextNotEmpty()) {
            if (chosenPropertyRepresentsThisColumn()) {
                throw failure(DEFAULT_CONFIG_TITLE + " could not be deleted.");
            } else {
                selectionCrit().centreDeleter().accept(chosenProperty());
            }
        }
        return entity;
    }
    
}