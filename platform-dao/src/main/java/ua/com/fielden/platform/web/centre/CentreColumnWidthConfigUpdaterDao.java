package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;

/** 
 * DAO implementation for companion object {@link ICentreColumnWidthConfigUpdater}.
 * 
 * @author Developers
 *
 */
@EntityType(CentreColumnWidthConfigUpdater.class)
public class CentreColumnWidthConfigUpdaterDao extends CommonEntityDao<CentreColumnWidthConfigUpdater> implements ICentreColumnWidthConfigUpdater {
    
    @Inject
    public CentreColumnWidthConfigUpdaterDao(final IFilter filter, final EntityFactory factory) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    public CentreColumnWidthConfigUpdater save(final CentreColumnWidthConfigUpdater action) {
        final EnhancedCentreEntityQueryCriteria criteriaEntityBeingUpdated = action.getContext().getSelectionCrit();
        final ICentreDomainTreeManagerAndEnhancer cdtmae = (ICentreDomainTreeManagerAndEnhancer) criteriaEntityBeingUpdated.freshCentreSupplier().get();
        
        final Class<?> root = criteriaEntityBeingUpdated.getEntityClass();
        final String property = action.getPropName();
        final Integer newWidth = action.getNewWidth();
        
        cdtmae.getSecondTick().setWidth(root, property, newWidth);
        // cdtmae.getRepresentation().getSecondTick().setWidthByDefault(root, property, width);
        
        return super.save(action);
    }
}
