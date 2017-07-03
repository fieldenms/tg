package ua.com.fielden.platform.web.centre;

import static ua.com.fielden.platform.web.centre.WebApiUtils.treeName;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;

/** 
 * DAO implementation for companion object {@link ICentreConfigUpdater}.
 * 
 * @author Developers
 *
 */
@EntityType(CentreConfigUpdater.class)
public class CentreConfigUpdaterDao extends CommonEntityDao<CentreConfigUpdater> implements ICentreConfigUpdater {
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
        final CentreConfigUpdater actionToSave = AbstractFunctionalEntityForCollectionModificationProducer.validateAction(action, a -> a.getCustomisableColumns(), this, factory, String.class);
        
        // after all validations have passed -- the association changes could be saved:
        final EnhancedCentreEntityQueryCriteria criteriaEntityBeingUpdated = (EnhancedCentreEntityQueryCriteria) action.refetchedMasterEntity();
        final Class<?> root = criteriaEntityBeingUpdated.getEntityClass();
        final Consumer<Consumer<ICentreDomainTreeManagerAndEnhancer>> centreAdjuster = criteriaEntityBeingUpdated.centreAdjuster();
        
        centreAdjuster.accept(cdtmae -> {
            // remove sorting information
            final List<Pair<String, Ordering>> orderedProperties = new ArrayList<>(cdtmae.getSecondTick().orderedProperties(root));
            for (final Pair<String, Ordering> orderedProperty: orderedProperties) {
                if (Ordering.ASCENDING == orderedProperty.getValue()) {
                    cdtmae.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
                }
                cdtmae.getSecondTick().toggleOrdering(root, orderedProperty.getKey());
            }
            
            // remove usage information
            final List<String> currUsedProperties = cdtmae.getSecondTick().usedProperties(root);
            for (final String currUsedProperty: currUsedProperties) {
                cdtmae.getSecondTick().use(root, currUsedProperty, false);
            }
            
            // apply usage information
            for (final String chosenId : action.getChosenIds()) {
                cdtmae.getSecondTick().use(root, treeName(chosenId), true);
            }
            
            // apply sorting information
            for (final String sortingVal: action.getSortingVals()) {
                final String[] splitted = sortingVal.split(":");
                final String name = treeName(splitted[0]);
                cdtmae.getSecondTick().toggleOrdering(root, name);
                if ("desc".equals(splitted[1])) {
                    cdtmae.getSecondTick().toggleOrdering(root, name);
                }
            }
        });
        
        // after the association changes were successfully saved, the action should also be saved:
        return super.save(actionToSave);
    }
}
