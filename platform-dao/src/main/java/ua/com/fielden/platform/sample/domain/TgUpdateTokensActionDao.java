package ua.com.fielden.platform.sample.domain;

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
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/** 
 * DAO implementation for companion object {@link ITgUpdateTokensAction}.
 * 
 * @author Developers
 *
 */
@EntityType(TgUpdateTokensAction.class)
public class TgUpdateTokensActionDao extends CommonEntityDao<TgUpdateTokensAction> implements ITgUpdateTokensAction {
    private final Logger logger = Logger.getLogger(getClass());
    private final IUserAndRoleAssociationBatchAction coUserAndRoleAssociationBatchAction;
    private final EntityFactory factory;
    
    @Inject
    public TgUpdateTokensActionDao(final IFilter filter, final IUserAndRoleAssociationBatchAction coUserAndRoleAssociationBatchAction, final EntityFactory factory) {
        super(filter);
        this.coUserAndRoleAssociationBatchAction = coUserAndRoleAssociationBatchAction;
        this.factory = factory;
    }
    
    @Override
    @SessionRequired
    public TgUpdateTokensAction save(final TgUpdateTokensAction action) {
        final TgUpdateTokensAction actionToSave = AbstractFunctionalEntityProducerForCollectionModification.validateAction(action, a -> a.getTokens(), this, factory, String.class);

        // TODO implement
        // after all validations have passed -- the association changes could be saved:
        final UserRole userRoleBeingUpdated = action.getKey();
        final Map<Object, TgSecurityToken> availableTokens = AbstractFunctionalEntityProducerForCollectionModification.mapById(action.getTokens(), String.class);
        
        final Set<SecurityRoleAssociation> addedAssociations = new LinkedHashSet<>();
        for (final String addedId : action.getAddedIds()) {
            final Class<? extends ISecurityToken> token = loadToken(availableTokens.get(addedId).getKey());
            addedAssociations.add(factory.newPlainEntity(SecurityRoleAssociation.class, null).setSecurityToken(token).setRole(userRoleBeingUpdated));
        }

        final Set<SecurityRoleAssociation> removedAssociations = new LinkedHashSet<>();
        for (final String removedId : action.getRemovedIds()) {
            final Class<? extends ISecurityToken> token = loadToken(availableTokens.get(removedId).getKey());
            removedAssociations.add(factory.newPlainEntity(SecurityRoleAssociation.class, null).setSecurityToken(token).setRole(userRoleBeingUpdated));
        }
        
        logger.error("addedAssociations == " + addedAssociations + " removedAssociations == " + removedAssociations);

//        final UserAndRoleAssociationBatchAction batchAction = new UserAndRoleAssociationBatchAction();
//        batchAction.setSaveEntities(addedAssociations);
//        batchAction.setRemoveEntities(removedAssociations);
//        coUserAndRoleAssociationBatchAction.save(batchAction);
        
        // after the association changes were successfully saved, the action should also be saved:
        final TgUpdateTokensAction saved = super.save(actionToSave);
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