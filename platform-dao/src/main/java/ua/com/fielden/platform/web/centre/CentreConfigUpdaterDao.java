package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.entity.CollectionModificationUtils.validateAction;
import static ua.com.fielden.platform.web.centre.CentreConfigUpdaterUtils.applyNewOrderVisibilityAndSorting;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * DAO implementation for companion object {@link CentreConfigUpdaterCo}.
 *
 * @author TG Team
 *
 */
@EntityType(CentreConfigUpdater.class)
public class CentreConfigUpdaterDao extends CommonEntityDao<CentreConfigUpdater> implements CentreConfigUpdaterCo {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;

    @Inject
    public CentreConfigUpdaterDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }

    @Override
    // @SessionRequired -- avoid transaction here; see EntityCentreConfigDao for more details
    public CentreConfigUpdater save(final CentreConfigUpdater action) {
        final T2<CentreConfigUpdater, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>> actionAndCriteriaBeingUpdated = validateAction(action, this, String.class, new CentreConfigUpdaterController(criteriaEntityRestorer));
        final CentreConfigUpdater actionToSave = actionAndCriteriaBeingUpdated._1;

        // retrieve criteria entity
        final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntityBeingUpdated = actionAndCriteriaBeingUpdated._2;
        final Class<?> root = criteriaEntityBeingUpdated.getEntityClass();

        // use centreAdjuster to update centre managers ('fresh' and 'previously_run') with columns visibility / order / sorting / pageCapacity / visibleRowsCount / numberOfHeaderLines information; also commit them to the database
        criteriaEntityBeingUpdated.adjustCentre(centreManager -> {
            applyNewOrderVisibilityAndSorting(centreManager.getSecondTick(), root, actionToSave.getChosenIds(), actionToSave.getSortingVals());
            centreManager.getSecondTick().setPageCapacity(actionToSave.getPageCapacity());
            centreManager.getSecondTick().setVisibleRowsCount(actionToSave.getVisibleRowsCount());
            centreManager.getSecondTick().setNumberOfHeaderLines(actionToSave.getNumberOfHeaderLines());
        });

        // in case where sorting or pageCapacity has been changed from previous value, we need to trigger running from client-side using 'triggerRerun' property
        actionToSave.setTriggerRerun(actionToSave.getProperty("pageCapacity").isChangedFromOriginal() || actionToSave.getProperty("sortingVals").isChangedFromOriginal());
        if (!actionToSave.isTriggerRerun()) {
            // in case where neither sorting nor pageCapacity has changed from previous value (and re-running will not occur), we need to send 'centreDirty' parameter and bind it to SAVE button disablement
            actionToSave.setCentreDirty(criteriaEntityBeingUpdated.isCentreDirty());
        }

        // we need to be able to continue 'change pageCapacity/sort/order/visibility' activities after successful save -- all essential properties should be reset to reflect 'newly applied' 'pageCapacity/sort/order/visibility' inside original values
        actionToSave.getProperty("pageCapacity").resetState();
        actionToSave.getProperty("sortingVals").resetState();
        actionToSave.getProperty("chosenIds").resetState();
        return super.save(actionToSave);
    }

}