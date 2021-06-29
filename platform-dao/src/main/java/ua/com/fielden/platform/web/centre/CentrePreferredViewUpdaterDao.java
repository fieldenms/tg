package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

@EntityType(CentrePreferredViewUpdater.class)
public class CentrePreferredViewUpdaterDao extends CommonEntityDao<CentrePreferredViewUpdater> implements CentrePreferredViewUpdaterCo {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;

    @Inject
    public CentrePreferredViewUpdaterDao(final IFilter filter, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }

    @Override
    @SessionRequired
    public CentrePreferredViewUpdater save(final CentrePreferredViewUpdater action) {
        // retrieve criteria entity
        final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntityBeingUpdated = criteriaEntityRestorer.restoreCriteriaEntity(action.getCriteriaEntityHolder());
        // use adjust centre to update preferred view
        criteriaEntityBeingUpdated.adjustCentre(centreManager ->
            centreManager.setPreferredView(action.getPreferredView())
        );
        action.setCentreDirty(criteriaEntityBeingUpdated.isCentreDirty());
        return super.save(action);
    }
}
