package ua.com.fielden.platform.security.user;

import static ua.com.fielden.platform.entity.CollectionModificationUtils.persistedActionVersionFor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.provider.ISecurityTokenNodeTransformation;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Controller for {@link UserRoleTokensUpdater}.
 *
 * @author TG Team
 *
 */
public class UserRoleTokensUpdaterController implements ICollectionModificationController<UserRole, UserRoleTokensUpdater, String, SecurityTokenInfo> {
    private final EntityFactory factory;
    private final SecurityTokenProvider securityTokenProvider;
    private final ISecurityTokenNodeTransformation tokenTransformation;
    private final IEntityDao<UserRole> coUserRole;
    private final IEntityDao<UserRoleTokensUpdater> co$UserRoleTokensUpdater;

    public UserRoleTokensUpdaterController(
            final EntityFactory factory, 
            final IApplicationSettings applicationSettings, 
            final IEntityDao<UserRole> coUserRole, 
            final IEntityDao<UserRoleTokensUpdater> co$UserRoleTokensUpdater,
            final ISecurityTokenNodeTransformation tokenTransformation) {
        this.factory = factory;
        this.securityTokenProvider = new SecurityTokenProvider(applicationSettings.pathToSecurityTokens(), applicationSettings.securityTokensPackageName());
        this.tokenTransformation = tokenTransformation;
        this.coUserRole = coUserRole;
        this.co$UserRoleTokensUpdater = co$UserRoleTokensUpdater;
    }

    @Override
    public UserRole getMasterEntityFromContext(final CentreContext<?, ?> context) {
        return context.getMasterEntity() == null ? (UserRole) context.getCurrEntity() : (UserRole) context.getMasterEntity();
    }

    @Override
    public UserRole refetchMasterEntity(final UserRole masterEntityFromContext) {
        return coUserRole.findById(masterEntityFromContext.getId(), coUserRole.getFetchProvider().with("tokens").fetchModel());
    }

    @Override
    public UserRoleTokensUpdater refetchActionEntity(final UserRole masterEntity) {
        return co$UserRoleTokensUpdater.findByKey(masterEntity.getId());
    }

    @Override
    public Collection<SecurityTokenInfo> refetchAvailableItems(final UserRole masterEntity) {
        return loadAvailableTokens(securityTokenProvider, factory);
    }

    @Override
    public UserRoleTokensUpdater setAvailableItems(final UserRoleTokensUpdater action, final Collection<SecurityTokenInfo> items) {
        return action.setTokens((Set<SecurityTokenInfo>) items);
    }

    @Override
    public Long persistedActionVersion(final Long masterEntityId) {
        return persistedActionVersionFor(masterEntityId, co$UserRoleTokensUpdater);
    }

    private Set<SecurityTokenInfo> loadAvailableTokens(final SecurityTokenProvider securityTokenProvider, final EntityFactory factory) {
        return lineariseTokens(tokenTransformation.transform(securityTokenProvider.getTopLevelSecurityTokenNodes()), factory);
    }

    private static Set<SecurityTokenInfo> lineariseTokens(final SortedSet<SecurityTokenNode> topLevelTokenNodes, final EntityFactory factory) {
        final Set<SecurityTokenInfo> setOfTokens = new LinkedHashSet<>();
        for (final SecurityTokenNode node: topLevelTokenNodes){
            setOfTokens.addAll(lineariseToken(node, factory));
        }
        return setOfTokens;
    }

    private static List<SecurityTokenInfo> lineariseToken(final SecurityTokenNode tokenNode, final EntityFactory factory) {
        final SecurityTokenInfo tokenEntity = factory.newEntity(SecurityTokenInfo.class, null, tokenNode.getToken().getName(), tokenNode.getLongDesc()).setTitle(tokenNode.getShortDesc());
        final List<SecurityTokenInfo> listOfTokens = new ArrayList<>();
        listOfTokens.add(tokenEntity);
        final List<SecurityTokenNode> children = tokenNode.daughters();
        for (final SecurityTokenNode child: children) {
            final List<SecurityTokenInfo> childList = lineariseToken(child, factory);
            listOfTokens.addAll(childList);
        }
        return listOfTokens;
    }

}
