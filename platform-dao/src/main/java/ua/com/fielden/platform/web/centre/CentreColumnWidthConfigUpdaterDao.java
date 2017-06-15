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
        final String property = "waType";
        
        final int prevWidth = cdtmae.getSecondTick().getWidth(root, property);
        cdtmae.getSecondTick().setWidth(root, property, prevWidth + 40);
        // cdtmae.getRepresentation().getSecondTick().setWidthByDefault(root, property, width);
        
//        final List<Pair<String, Ordering>> orderedProperties = new ArrayList<>(cdtmae.getSecondTick().orderedProperties(root));
//        for (final Pair<String, Ordering> orderedProperty: orderedProperties) {
//            if (Ordering.ASCENDING == orderedProperty.getValue()) {
//                cdtmae.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
//            }
//            cdtmae.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
//  TODO      }
//        
//        for (final String sortingVal: action.getSortingVals()) {
//            final String[] splitted = sortingVal.split(":");
//            final String name = splitted[0].equals("this") ? "" : splitted[0];
//            cdtmae.getSecondTick().toggleOrdering(root, name);
//            if ("desc".equals(splitted[1])) {
//                cdtmae.getSecondTick().toggleOrdering(root, name);
//            }
//        }
        
        // after width has been altered, the action saving should proceed:
        return super.save(action);
    }
}
