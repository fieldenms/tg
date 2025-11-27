package ua.com.fielden.platform.security.user;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenNodeTransformation;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanSave_Token;

import java.util.Map;

import static ua.com.fielden.platform.entity.CollectionModificationUtils.toMapByKey;
import static ua.com.fielden.platform.entity.CollectionModificationUtils.validateAction;

/// DAO implementation for companion object [UserRoleTokensUpdaterCo].
///
@EntityType(UserRoleTokensUpdater.class)
public class UserRoleTokensUpdaterDao extends CommonEntityDao<UserRoleTokensUpdater> implements UserRoleTokensUpdaterCo {

    public static final String ERR_SECURITY_TOKEN_NOT_FOUND = "Security token [%s] could not be found.";

    private final EntityFactory factory;
    private final ISecurityTokenNodeTransformation tokenTransformation;
    private final ISecurityTokenProvider securityTokenProvider;
    
    @Inject
    protected UserRoleTokensUpdaterDao(
            final EntityFactory factory,
            final ISecurityTokenNodeTransformation tokenTransformation,
            final ISecurityTokenProvider securityTokenProvider)
    {
        this.factory = factory;
        this.tokenTransformation = tokenTransformation;
        this.securityTokenProvider = securityTokenProvider;
    }
    
    @Override
    @SessionRequired
    @Authorise(UserRole_CanSave_Token.class)
    public UserRoleTokensUpdater save(final UserRoleTokensUpdater action) {
        return validateAction(
                action,
                this,
                String.class,
                new UserRoleTokensUpdaterController(factory,
                                                    co(UserRole.class),
                                                    co$(UserRoleTokensUpdater.class),
                                                    tokenTransformation,
                                                    securityTokenProvider)
        ).map((actionToSave, userRoleBeingUpdated) -> {
            // After validation succeeds, the association changes can be saved.
            final Map<Object, SecurityTokenInfo> availableTokens = toMapByKey(actionToSave.getTokens());
            final SecurityRoleAssociationCo co$Association = co$(SecurityRoleAssociation.class);

            final var associationsToAdd = actionToSave.getAddedIds()
                    .stream()
                    .map(id -> co$Association.new_()
                            .setSecurityToken(loadToken(availableTokens.get(id).getKey()))
                            .setRole(userRoleBeingUpdated))
                    .toList();

            final var associationsToRemove = actionToSave.getRemovedIds()
                    .stream()
                    .map(id -> co$Association.new_()
                            .setSecurityToken(loadToken(availableTokens.get(id).getKey()))
                            .setRole(userRoleBeingUpdated))
                    .toList();

            co$Association.addAssociations(associationsToAdd);
            co$Association.removeAssociations(associationsToRemove);

            return super.save(actionToSave);
        });
    }

    private Class<? extends ISecurityToken> loadToken(final String name) {
        return securityTokenProvider.getTokenByName(name)
                .orElseThrow(() -> Result.failure(new InvalidStateException(ERR_SECURITY_TOKEN_NOT_FOUND.formatted(name))));
    }

}
