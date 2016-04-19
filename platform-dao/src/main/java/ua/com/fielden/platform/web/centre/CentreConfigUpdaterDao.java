package ua.com.fielden.platform.web.centre;

import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.AbstractFunctionalEntityProducerForCollectionModification;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.user.UserRoleTokensUpdater;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;

/** 
 * DAO implementation for companion object {@link ICentreConfigUpdater}.
 * 
 * @author Developers
 *
 */
@EntityType(UserRoleTokensUpdater.class)
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
        
        // after the association changes were successfully saved, the action should also be saved:
        return super.save(actionToSave);
    }
}
