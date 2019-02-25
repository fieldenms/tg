package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.entity.CollectionModificationUtils.validateAction;
import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.applyNewOrderVisibilityAndSorting;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * DAO implementation for companion object {@link ICentreConfigUpdater}.
 *
 * @author Developers
 *
 */
@EntityType(CentreConfigUpdater.class)
public class CentreConfigUpdaterDao extends CommonEntityDao<CentreConfigUpdater> implements ICentreConfigUpdater {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;

    @Inject
    public CentreConfigUpdaterDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }

    @Override
    @SessionRequired
    public CentreConfigUpdater save(final CentreConfigUpdater action) {
        final T2<CentreConfigUpdater, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>> actionAndCriteriaBeingUpdated = validateAction(action, this, String.class, new CentreConfigUpdaterController(criteriaEntityRestorer));
        final CentreConfigUpdater actionToSave = actionAndCriteriaBeingUpdated._1;

        // retrieve criteria entity
        final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntityBeingUpdated = actionAndCriteriaBeingUpdated._2;
        final Class<?> root = criteriaEntityBeingUpdated.getEntityClass();

        // use centreAdjuster to update all centre managers ('fresh' and 'saved') with columns visibility / order / sorting information; also commit them to the database
        criteriaEntityBeingUpdated.adjustCentre(centreManager -> {
            applyNewOrderVisibilityAndSorting(centreManager.getSecondTick(), root, actionToSave.getChosenIds(), actionToSave.getSortingVals());
        });

        // in case where sorting has been changed from previous value, we need to trigger running from client-side using 'sortingChanged' parameter
        actionToSave.setSortingChanged(actionToSave.getProperty("sortingVals").isChangedFromOriginal());
        if (!actionToSave.isSortingChanged()) {
            // in case where sorting has not been changed from previous value (and re-running will not occur), we need to send 'centreChanged' parameter and bind it to 'Show selection criteria' button (orange or black)
            actionToSave.setCentreChanged(criteriaEntityBeingUpdated.isCentreChanged());
        }

        // we need to be able to continue 'change sort/order/visibility' activities after successful save -- all essential properties should be reset to reflect 'newly applied' 'sort/order/visibility' inside original values
        actionToSave.getProperty("sortingVals").resetState();
        actionToSave.getProperty("chosenIds").resetState();
        return super.save(actionToSave);
    }

}