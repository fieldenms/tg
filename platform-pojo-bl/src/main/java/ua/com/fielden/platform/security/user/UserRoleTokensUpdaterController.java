package ua.com.fielden.platform.security.user;

import static ua.com.fielden.platform.entity.CollectionModificationUtils.persistedActionVersionFor;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
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
    private final IEntityDao<UserRole> coUserRole;
    private final IEntityDao<UserRoleTokensUpdater> coUserRoleTokensUpdater;
    
    public UserRoleTokensUpdaterController(final EntityFactory factory, final IApplicationSettings applicationSettings, final IEntityDao<UserRole> coUserRole, final IEntityDao<UserRoleTokensUpdater> coUserRoleTokensUpdater) {
        this.factory = factory;
        this.securityTokenProvider = new SecurityTokenProvider(applicationSettings.pathToSecurityTokens(), applicationSettings.securityTokensPackageName());
        this.coUserRole = coUserRole;
        this.coUserRoleTokensUpdater = coUserRoleTokensUpdater;
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
        return coUserRoleTokensUpdater.getEntity(
            from(select(UserRoleTokensUpdater.class).where().prop(AbstractEntity.KEY).eq().val(masterEntity.getId()).model())
            .with(fetchAndInstrument(UserRoleTokensUpdater.class).with(AbstractEntity.KEY))
            .model()
        );
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
        return persistedActionVersionFor(masterEntityId, coUserRoleTokensUpdater);
    }
    
    private static Set<SecurityTokenInfo> loadAvailableTokens(final SecurityTokenProvider securityTokenProvider, final EntityFactory factory) {
        return lineariseTokens(securityTokenProvider.getTopLevelSecurityTokenNodes(), factory);
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
