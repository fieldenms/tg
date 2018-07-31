package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.web.centre.CentreConfigUtils.prepareDefaultCentre;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link CentreConfigNewAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigNewActionProducer extends DefaultEntityProducerWithContext<CentreConfigNewAction> {
    
    @Inject
    public CentreConfigNewActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigNewAction.class, companionFinder);
    }
    
    @Override
    protected CentreConfigNewAction provideDefaultValues(final CentreConfigNewAction entity) {
        if (contextNotEmpty()) {
            entity.setCustomObject(prepareDefaultCentre(selectionCrit()));
        }
        return entity;
    }
    
}