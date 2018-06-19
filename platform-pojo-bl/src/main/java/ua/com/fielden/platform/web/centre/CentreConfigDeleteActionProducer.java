package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.web.centre.CentreConfigDeleteAction.DeleteKind.DELETE;

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
            entity.setDeleteKind(chosenProperty());
            
            if (DELETE.name().equals(entity.getDeleteKind())) {
                selectionCrit().centreDeleter().run();
                entity.setPreferredConfig(selectionCrit().preferredConfigSupplier().get().orElse(""));
            } else {
                selectionCrit().defaultCentreClearer().run();
                entity.setPreferredConfig("");
            }
        }
        return entity;
    }
    
}