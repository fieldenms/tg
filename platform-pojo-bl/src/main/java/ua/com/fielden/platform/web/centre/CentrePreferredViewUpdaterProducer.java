package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/**
 * A producer for new instances of entity {@link CentrePreferredViewUpdater}.
 *
 * @author TG Team
 *
 */
public class CentrePreferredViewUpdaterProducer extends DefaultEntityProducerWithContext<CentrePreferredViewUpdater> {

    @Inject
    public CentrePreferredViewUpdaterProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentrePreferredViewUpdater.class, companionFinder);
    }

    @Override
    protected CentrePreferredViewUpdater provideDefaultValues(final CentrePreferredViewUpdater action) {
        if (selectionCritNotEmpty()) {
            // retrieve criteria entity
            final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntityBeingUpdated = selectionCrit();
            // use adjust centre to update preferred view
            criteriaEntityBeingUpdated.adjustCentre(centreManager -> {
                final int preferredView = (Integer) getContext().getCustomObject().get("preferredView");
                action.setPreferredView(preferredView);
                centreManager.setPreferredView(preferredView);
            });
            action.setCentreDirty(criteriaEntityBeingUpdated.isCentreDirty());
        }
        return action;
    }

}