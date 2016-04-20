package ua.com.fielden.platform.web.centre;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;

/** 
 * DAO implementation for companion object {@link ICentreConfigUpdater}.
 * 
 * @author Developers
 *
 */
@EntityType(CentreConfigUpdater.class)
public class CentreConfigUpdaterDao extends CommonEntityDao<CentreConfigUpdater> implements ICentreConfigUpdater {
    private final Logger logger = Logger.getLogger(getClass());
    private final EntityFactory factory;
    
    @Inject
    public CentreConfigUpdaterDao(final IFilter filter, final EntityFactory factory) {
        super(filter);
        this.factory = factory;
    }
    
    @Override
    @SessionRequired
    // @Authorise(UserRoleSaveToken.class)
    public CentreConfigUpdater save(final CentreConfigUpdater action) {
        final CentreConfigUpdater actionToSave = AbstractFunctionalEntityForCollectionModificationProducer.validateAction(action, a -> a.getSortingProperties(), this, factory, String.class);
        
        // after all validations have passed -- the association changes could be saved:
        final EnhancedCentreEntityQueryCriteria criteriaEntityBeingUpdated = (EnhancedCentreEntityQueryCriteria) action.refetchedMasterEntity();
        // final Map<Object, SortingProperty> availableSortingProperties = AbstractFunctionalEntityForCollectionModificationProducer.mapById(action.getSortingProperties(), String.class);
        final ICentreDomainTreeManagerAndEnhancer cdtmae = (ICentreDomainTreeManagerAndEnhancer) criteriaEntityBeingUpdated.freshCentreSupplier().get();
        final Class<?> root = criteriaEntityBeingUpdated.getEntityClass();
        final List<Pair<String, Ordering>> orderedProperties = new ArrayList<>(cdtmae.getSecondTick().orderedProperties(root));
        for (final Pair<String, Ordering> orderedProperty: orderedProperties) {
            if (Ordering.ASCENDING == orderedProperty.getValue()) {
                cdtmae.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
            }
            cdtmae.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
        }
        
        for (final String sortingVal: action.getSortingVals()) {
            final String[] splitted = sortingVal.split(":");
            cdtmae.getSecondTick().toggleOrdering(root, splitted[0]);
            if ("desc".equals(splitted[1])) {
                cdtmae.getSecondTick().toggleOrdering(root, splitted[0]);
            }
        }
        
        // after the association changes were successfully saved, the action should also be saved:
        return super.save(actionToSave);
    }
}
