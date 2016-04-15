package ua.com.fielden.platform.security.provider;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.AbstractFunctionalEntityProducerForCollectionModification;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.tokens.user.UserSaveToken;
import ua.com.fielden.platform.security.user.IUserRolesUpdater;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRolesUpdater;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/** 
 * DAO implementation for companion object {@link IUserRolesUpdater}.
 * 
 * @author Developers
 *
 */
@EntityType(UserRolesUpdater.class)
public class UserRolesUpdaterDao extends CommonEntityDao<UserRolesUpdater> implements IUserRolesUpdater {
    private final IUserAndRoleAssociationBatchAction coUserAndRoleAssociationBatchAction;
    private final EntityFactory factory;
    
    @Inject
    public UserRolesUpdaterDao(final IFilter filter, final IUserAndRoleAssociationBatchAction coUserAndRoleAssociationBatchAction, final EntityFactory factory) {
        super(filter);
        this.coUserAndRoleAssociationBatchAction = coUserAndRoleAssociationBatchAction;
        this.factory = factory;
    }
    
    @Override
    @SessionRequired
    @Authorise(UserSaveToken.class)
    public UserRolesUpdater save(final UserRolesUpdater action) {
        final UserRolesUpdater actionToSave = AbstractFunctionalEntityProducerForCollectionModification.validateAction(action, a -> a.getRoles(), this, factory, Long.class);
        
        // after all validations have passed -- the association changes could be saved:
        final User userBeingUpdated = action.getKey();
        final Map<Object, UserRole> availableRoles = AbstractFunctionalEntityProducerForCollectionModification.mapById(action.getRoles(), Long.class);
        
        final Set<UserAndRoleAssociation> addedAssociations = new LinkedHashSet<>();
        for (final Long addedId : action.getAddedIds()) {
            final UserAndRoleAssociation assoc = factory.newByKey(UserAndRoleAssociation.class, userBeingUpdated, availableRoles.get(addedId));
            addedAssociations.add(assoc);
        }

        final Set<UserAndRoleAssociation> removedAssociations = new LinkedHashSet<>();
        for (final Long removedId : action.getRemovedIds()) {
            final UserAndRoleAssociation assoc = factory.newByKey(UserAndRoleAssociation.class, userBeingUpdated, availableRoles.get(removedId));
            removedAssociations.add(assoc);
        }

        final UserAndRoleAssociationBatchAction batchAction = new UserAndRoleAssociationBatchAction();
        batchAction.setSaveEntities(addedAssociations);
        batchAction.setRemoveEntities(removedAssociations);
        coUserAndRoleAssociationBatchAction.save(batchAction);
        
        // after the association changes were successfully saved, the action should also be saved:
        return super.save(actionToSave);
    }
}
