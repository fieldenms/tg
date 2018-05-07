package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link CentreConfigCopyAction}.
 *
 * @author TG Team
 *
 */
public class CentreConfigCopyActionProducer extends DefaultEntityProducerWithContext<CentreConfigCopyAction> {
    
    @Inject
    public CentreConfigCopyActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigCopyAction.class, companionFinder);
    }
    
    @Override
    protected CentreConfigCopyAction provideDefaultValues(final CentreConfigCopyAction entity) {
        // TODO take chosenProperty and init 'key'
        // TODO after that init some initial value for 'title'
        return super.provideDefaultValues(entity);
    }
    
}