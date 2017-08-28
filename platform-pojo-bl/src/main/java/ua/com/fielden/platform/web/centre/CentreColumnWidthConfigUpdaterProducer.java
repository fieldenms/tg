package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

public class CentreColumnWidthConfigUpdaterProducer extends DefaultEntityProducerWithContext<CentreColumnWidthConfigUpdater> {

    @Inject
    public CentreColumnWidthConfigUpdaterProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreColumnWidthConfigUpdater.class, companionFinder);
    }

    @Override
    protected CentreColumnWidthConfigUpdater provideDefaultValues(final CentreColumnWidthConfigUpdater entity) {
        if (selectionCritNotEmpty()) {
            entity.setCriteriaEntityHolder(selectionCrit().centreContextHolder());
        }
        return entity;
    }
}
