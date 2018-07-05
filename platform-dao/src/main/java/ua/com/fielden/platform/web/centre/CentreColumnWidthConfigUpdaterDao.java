package ua.com.fielden.platform.web.centre;

import java.util.HashMap;
import java.util.function.Consumer;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * DAO implementation for companion object {@link ICentreColumnWidthConfigUpdater}.
 *
 * @author Developers
 *
 */
@EntityType(CentreColumnWidthConfigUpdater.class)
public class CentreColumnWidthConfigUpdaterDao extends CommonEntityDao<CentreColumnWidthConfigUpdater> implements ICentreColumnWidthConfigUpdater {
    private final ICriteriaEntityRestorer criteriaEntityRestorer;

    @Inject
    public CentreColumnWidthConfigUpdaterDao(final IFilter filter, final EntityFactory factory, final ICriteriaEntityRestorer criteriaEntityRestorer) {
        super(filter);
        this.criteriaEntityRestorer = criteriaEntityRestorer;
    }

    @Override
    @SessionRequired
    public CentreColumnWidthConfigUpdater save(final CentreColumnWidthConfigUpdater action) {
        // retrieve criteria entity
        final EnhancedCentreEntityQueryCriteria<?, ?> criteriaEntityBeingUpdated = criteriaEntityRestorer.restoreCriteriaEntity(action.getCriteriaEntityHolder());
        final Class<?> root = criteriaEntityBeingUpdated.getEntityClass();
        final Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreColumnWidthsAdjuster = criteriaEntityBeingUpdated.centreColumnWidthsAdjuster();
        
        // use centreColumnWidthsAdjuster to update column widths in PREVIOUSLY_RUN and FRESH centre managers; commit them to the database
        centreColumnWidthsAdjuster.accept(centreManager -> {
            action.getColumnParameters().entrySet().forEach(entry -> {
                if (entry.getValue().containsKey("width")) {
                    centreManager.getSecondTick().setWidth(root, entry.getKey(), entry.getValue().get("width"));
                }
                if (entry.getValue().containsKey("growFactor")) {
                    centreManager.getSecondTick().setGrowFactor(root, entry.getKey(), entry.getValue().get("growFactor"));
                }
            });
        });
        
        action.setColumnParameters(new HashMap<>());
        action.setCentreChanged(criteriaEntityBeingUpdated.centreChangedGetter().get()); // centre will be changed after this action; changes can be discarded using DISCARD button on selection criteria
        return super.save(action);
    }
}
