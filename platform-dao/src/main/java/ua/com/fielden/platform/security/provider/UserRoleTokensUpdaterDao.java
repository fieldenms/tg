package ua.com.fielden.platform.security.provider;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.AbstractFunctionalEntityProducerForCollectionModification;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.ISecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.user.IUserRoleTokensUpdater;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityTokenInfo;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRoleTokensUpdater;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/** 
 * DAO implementation for companion object {@link IUserRoleTokensUpdater}.
 * 
 * @author Developers
 *
 */
@EntityType(UserRoleTokensUpdater.class)
public class UserRoleTokensUpdaterDao extends CommonEntityDao<UserRoleTokensUpdater> implements IUserRoleTokensUpdater {
    private final Logger logger = Logger.getLogger(getClass());
    private final ISecurityRoleAssociationBatchAction coSecurityRoleAssociationBatchAction;
    private final EntityFactory factory;
    
    @Inject
    public UserRoleTokensUpdaterDao(final IFilter filter, final ISecurityRoleAssociationBatchAction coSecurityRoleAssociationBatchAction, final EntityFactory factory) {
        super(filter);
        this.coSecurityRoleAssociationBatchAction = coSecurityRoleAssociationBatchAction;
        this.factory = factory;
    }
    
    @Override
    @SessionRequired
    public UserRoleTokensUpdater save(final UserRoleTokensUpdater action) {
        final UserRoleTokensUpdater actionToSave = AbstractFunctionalEntityProducerForCollectionModification.validateAction(action, a -> a.getTokens(), this, factory, String.class);
        
        // after all validations have passed -- the association changes could be saved:
        final UserRole userRoleBeingUpdated = action.getKey();
        final Map<Object, SecurityTokenInfo> availableTokens = AbstractFunctionalEntityProducerForCollectionModification.mapById(action.getTokens(), String.class);
        
        final Set<SecurityRoleAssociation> addedAssociations = new LinkedHashSet<>();
        for (final String addedId : action.getAddedIds()) {
            final Class<? extends ISecurityToken> token = loadToken(availableTokens.get(addedId).getKey());
            final SecurityRoleAssociation assoc = factory.newByKey(SecurityRoleAssociation.class, token, userRoleBeingUpdated);
            addedAssociations.add(assoc);
        }

        final Set<SecurityRoleAssociation> removedAssociations = new LinkedHashSet<>();
        for (final String removedId : action.getRemovedIds()) {
            final Class<? extends ISecurityToken> token = loadToken(availableTokens.get(removedId).getKey());
            final SecurityRoleAssociation assoc = factory.newByKey(SecurityRoleAssociation.class, token, userRoleBeingUpdated);
            removedAssociations.add(assoc);
        }
        
        logger.error("addedAssociations == " + addedAssociations + " removedAssociations == " + removedAssociations);

        final SecurityRoleAssociationBatchAction batchAction = new SecurityRoleAssociationBatchAction();
        batchAction.setSaveEntities(addedAssociations);
        batchAction.setRemoveEntities(removedAssociations);
        coSecurityRoleAssociationBatchAction.save(batchAction);
        
        // after the association changes were successfully saved, the action should also be saved:
        final UserRoleTokensUpdater saved = super.save(actionToSave);
        logger.error("saved.getVersion() = " + saved.getVersion());
        logger.error("saved.getSurrogateVersion() = " + saved.getSurrogateVersion());
        return saved;
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
