package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

import java.util.Map;

/**
 * A producer for new instances of entity {@link CentreColumnWidthConfigUpdater}.
 *
 * @author TG Team
 *
 */
public class CentreColumnWidthConfigUpdaterProducer extends DefaultEntityProducerWithContext<CentreColumnWidthConfigUpdater> {

    @Inject
    public CentreColumnWidthConfigUpdaterProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreColumnWidthConfigUpdater.class, companionFinder);
    }

    @Override
    protected CentreColumnWidthConfigUpdater provideDefaultValues(final CentreColumnWidthConfigUpdater action) {
        if (selectionCritNotEmpty()) {
            // retrieve criteria entity
            final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntityBeingUpdated = selectionCrit();
            final Class<?> root = criteriaEntityBeingUpdated.getEntityClass();
            // use centreColumnWidthsAdjuster to update column widths in PREVIOUSLY_RUN and FRESH centre managers; commit them to the database
            criteriaEntityBeingUpdated.adjustColumnWidths(centreManager -> {
                final Map<String, Map<String, Integer>> columnParameters = (Map<String, Map<String, Integer>>) getContext().getCustomObject().get("columnParameters");
                columnParameters.entrySet().forEach(entry -> {
                    if (entry.getValue().containsKey("width")) {
                        centreManager.getSecondTick().setWidth(root, entry.getKey(), entry.getValue().get("width"));
                    }
                    if (entry.getValue().containsKey("growFactor")) {
                        centreManager.getSecondTick().setGrowFactor(root, entry.getKey(), entry.getValue().get("growFactor"));
                    }
                });
            });
            // centre will be changed after this action; changes can be discarded using DISCARD button on selection criteria
            final boolean centreChanged = criteriaEntityBeingUpdated.isCentreChanged();
            action.setCentreChanged(centreChanged);
            action.setCentreDirty(criteriaEntityBeingUpdated.isCentreDirty(centreChanged));
        }
        return action;
    }
    
}