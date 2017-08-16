package ua.com.fielden.platform.security.provider;

import static ua.com.fielden.platform.entity.CollectionModificationUtils.mapById;
import static ua.com.fielden.platform.entity.CollectionModificationUtils.validateAction;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IUserRole;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.tokens.user.UserSaveToken;
import ua.com.fielden.platform.security.user.IUserRolesUpdater;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRolesUpdater;
import ua.com.fielden.platform.security.user.UserRolesUpdaterController;
import ua.com.fielden.platform.types.tuples.T2;

/** 
 * DAO implementation for companion object {@link IUserRolesUpdater}.
 * 
 * @author Developers
 *
 */
@EntityType(UserRolesUpdater.class)
public class UserRolesUpdaterDao extends CommonEntityDao<UserRolesUpdater> implements IUserRolesUpdater {
    
    @Inject
    public UserRolesUpdaterDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    @Authorise(UserSaveToken.class)
    public UserRolesUpdater save(final UserRolesUpdater action) {
        final T2<UserRolesUpdater, User> actionAndUserBeingUpdated = validateAction(action, this, Long.class, new UserRolesUpdaterController(co(User.class), co(UserRolesUpdater.class), this.<IUserRole, UserRole>co(UserRole.class)));
        final UserRolesUpdater actionToSave = actionAndUserBeingUpdated._1;
        final Map<Object, UserRole> availableRoles = mapById(actionToSave.getRoles(), Long.class);
        
        final Set<UserAndRoleAssociation> addedAssociations = new LinkedHashSet<>();
        for (final Long addedId : actionToSave.getAddedIds()) {
            addedAssociations.add(co(UserAndRoleAssociation.class).new_().setUser(actionAndUserBeingUpdated._2).setUserRole(availableRoles.get(addedId)));
        }

        final Set<UserAndRoleAssociation> removedAssociations = new LinkedHashSet<>();
        for (final Long removedId : actionToSave.getRemovedIds()) {
            removedAssociations.add(co(UserAndRoleAssociation.class).new_().setUser(actionAndUserBeingUpdated._2).setUserRole(availableRoles.get(removedId)));
        }

        final UserAndRoleAssociationBatchAction batchAction = new UserAndRoleAssociationBatchAction();
        batchAction.setSaveEntities(addedAssociations);
        batchAction.setRemoveEntities(removedAssociations);
        co(UserAndRoleAssociationBatchAction.class).save(batchAction);
        
        // after the association changes were successfully saved, the action should also be saved:
        return super.save(actionToSave);
    }
    
}
