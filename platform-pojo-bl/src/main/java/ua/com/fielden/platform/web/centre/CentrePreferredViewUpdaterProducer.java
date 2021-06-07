package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

public class CentrePreferredViewUpdaterProducer extends DefaultEntityProducerWithContext<CentrePreferredViewUpdater> {

    @Inject
    public CentrePreferredViewUpdaterProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentrePreferredViewUpdater.class, companionFinder);
    }

    @Override
    protected CentrePreferredViewUpdater provideDefaultValues(final CentrePreferredViewUpdater entity) {
        if (selectionCritNotEmpty()) {
            entity.setCriteriaEntityHolder(selectionCrit().centreContextHolder());
        }
        return entity;
    }
}
