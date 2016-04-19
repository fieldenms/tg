package ua.com.fielden.platform.web.centre;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.AbstractFunctionalEntityProducerForCollectionModification;
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
    // private final ISecurityRoleAssociationBatchAction coSecurityRoleAssociationBatchAction;
    private final EntityFactory factory;
    
    @Inject
    public CentreConfigUpdaterDao(final IFilter filter, /* final ISecurityRoleAssociationBatchAction coSecurityRoleAssociationBatchAction, */ final EntityFactory factory) {
        super(filter);
        // this.coSecurityRoleAssociationBatchAction = coSecurityRoleAssociationBatchAction;
        this.factory = factory;
    }
    
    @Override
    @SessionRequired
    // @Authorise(UserRoleSaveToken.class)
    public CentreConfigUpdater save(final CentreConfigUpdater action) {
        final CentreConfigUpdater actionToSave = AbstractFunctionalEntityProducerForCollectionModification.validateAction(action, a -> a.getSortingProperties(), this, factory, String.class);
        
        // after all validations have passed -- the association changes could be saved:
        final EnhancedCentreEntityQueryCriteria criteriaEntityBeingUpdated = action.getKey();
        final Map<Object, SortingProperty> availableSortingProperties = AbstractFunctionalEntityProducerForCollectionModification.mapById(action.getSortingProperties(), String.class);
        logger.error("availableSortingProperties == " + availableSortingProperties);
        logger.error("sortingVals == " + action.getSortingVals());
        
//        final Set<SecurityRoleAssociation> addedAssociations = new LinkedHashSet<>();
//        for (final String addedId : action.getAddedIds()) {
//            final Class<? extends ISecurityToken> token = loadToken(availableSortingProperties.get(addedId).getKey());
//            final SecurityRoleAssociation assoc = factory.newByKey(SecurityRoleAssociation.class, token, criteriaEntityBeingUpdated);
//            addedAssociations.add(assoc);
//        }
//
//        final Set<SecurityRoleAssociation> removedAssociations = new LinkedHashSet<>();
//        for (final String removedId : action.getRemovedIds()) {
//            final Class<? extends ISecurityToken> token = loadToken(availableSortingProperties.get(removedId).getKey());
//            final SecurityRoleAssociation assoc = factory.newByKey(SecurityRoleAssociation.class, token, criteriaEntityBeingUpdated);
//            removedAssociations.add(assoc);
//        }
//        
//        final SecurityRoleAssociationBatchAction batchAction = new SecurityRoleAssociationBatchAction();
//        batchAction.setSaveEntities(addedAssociations);
//        batchAction.setRemoveEntities(removedAssociations);
//        coSecurityRoleAssociationBatchAction.save(batchAction);
        
        final ICentreDomainTreeManagerAndEnhancer cdtmae = criteriaEntityBeingUpdated.getCentreDomainTreeMangerAndEnhancer();
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
