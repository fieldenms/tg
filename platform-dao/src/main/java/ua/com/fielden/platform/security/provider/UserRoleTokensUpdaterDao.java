package ua.com.fielden.platform.security.provider;

import static ua.com.fielden.platform.entity.CollectionModificationUtils.mapById;
import static ua.com.fielden.platform.entity.CollectionModificationUtils.validateAction;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.tokens.user.UserRoleSaveToken;
import ua.com.fielden.platform.security.user.IUserRoleTokensUpdater;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityTokenInfo;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRoleTokensUpdater;
import ua.com.fielden.platform.security.user.UserRoleTokensUpdaterController;
import ua.com.fielden.platform.types.tuples.T2;

/** 
 * DAO implementation for companion object {@link IUserRoleTokensUpdater}.
 * 
 * @author Developers
 *
 */
@EntityType(UserRoleTokensUpdater.class)
public class UserRoleTokensUpdaterDao extends CommonEntityDao<UserRoleTokensUpdater> implements IUserRoleTokensUpdater {
    private final EntityFactory factory;
    private final IApplicationSettings applicationSettings;
    
    @Inject
    public UserRoleTokensUpdaterDao(final IFilter filter, final EntityFactory factory, final IApplicationSettings applicationSettings) {
        super(filter);
        this.factory = factory;
        this.applicationSettings = applicationSettings;
    }
    
    @Override
    @SessionRequired
    @Authorise(UserRoleSaveToken.class)
    public UserRoleTokensUpdater save(final UserRoleTokensUpdater action) {
        final T2<UserRoleTokensUpdater, UserRole> actionAndUserRoleBeingUpdated = validateAction(action, this, String.class, new UserRoleTokensUpdaterController(factory, applicationSettings, co$(UserRole.class), co$(UserRoleTokensUpdater.class)));
        final UserRoleTokensUpdater actionToSave = actionAndUserRoleBeingUpdated._1;
        
        // after all validations have passed -- the association changes could be saved:
        final UserRole userRoleBeingUpdated = actionAndUserRoleBeingUpdated._2;
        final Map<Object, SecurityTokenInfo> availableTokens = mapById(actionToSave.getTokens(), String.class);
        
        final Set<SecurityRoleAssociation> addedAssociations = new LinkedHashSet<>();
        for (final String addedId : actionToSave.getAddedIds()) {
            final Class<? extends ISecurityToken> token = loadToken(availableTokens.get(addedId).getKey());
            final SecurityRoleAssociation assoc = factory.newByKey(SecurityRoleAssociation.class, token, userRoleBeingUpdated);
            addedAssociations.add(assoc);
        }

        final Set<SecurityRoleAssociation> removedAssociations = new LinkedHashSet<>();
        for (final String removedId : actionToSave.getRemovedIds()) {
            final Class<? extends ISecurityToken> token = loadToken(availableTokens.get(removedId).getKey());
            final SecurityRoleAssociation assoc = factory.newByKey(SecurityRoleAssociation.class, token, userRoleBeingUpdated);
            removedAssociations.add(assoc);
        }
        
        final SecurityRoleAssociationBatchAction batchAction = new SecurityRoleAssociationBatchAction();
        batchAction.setSaveEntities(addedAssociations);
        batchAction.setRemoveEntities(removedAssociations);
        co$(SecurityRoleAssociationBatchAction.class).save(batchAction);
        
        // after the association changes were successfully saved, the action should also be saved:
        return super.save(actionToSave);
    }

    private Class<? extends ISecurityToken> loadToken(final String name) {
        final Class<? extends ISecurityToken> token;
        try {
            token = (Class<? extends ISecurityToken>) Class.forName(name);
        } catch (final ClassNotFoundException e) {
            throw Result.failure(new IllegalStateException(String.format("Security token [%s] could not be found.", name)));
        }
        return token;
    }
}
