package ua.com.fielden.platform.security.user;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.user.UserRoleReviewToken;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A producer for new instances of entity {@link UserRoleTokensUpdater}.
 *
 * @author TG Team
 *
 */
public class UserRoleTokensUpdaterProducer extends AbstractFunctionalEntityForCollectionModificationProducer<UserRole, UserRoleTokensUpdater> implements IEntityProducer<UserRoleTokensUpdater> {
    private final IUserRoleDao coUserRole;
    private final SecurityTokenProvider securityTokenProvider;
    
    @Inject
    public UserRoleTokensUpdaterProducer(
            final EntityFactory factory, 
            final ICompanionObjectFinder companionFinder, 
            final IUserRoleDao coUserRole, 
            final IApplicationSettings applicationSettings) throws Exception {
        super(factory, UserRoleTokensUpdater.class, companionFinder);
        this.coUserRole = coUserRole;
        this.securityTokenProvider = new SecurityTokenProvider(applicationSettings.pathToSecurityTokens(), applicationSettings.securityTokensPackageName());
    }
    
    @Override
    @Authorise(UserRoleReviewToken.class)
    protected UserRoleTokensUpdater provideCurrentlyAssociatedValues(final UserRoleTokensUpdater entity, final UserRole masterEntity) {
        final SortedSet<SecurityTokenNode> topLevelTokens = securityTokenProvider.getTopLevelSecurityTokenNodes();
        final Set<SecurityTokenInfo> linearisedTokens = lineariseTokens(topLevelTokens, factory());
        entity.setTokens(linearisedTokens);
        entity.getProperty("tokens").resetState();
        
        final Set<String> chosenRoleIds = new LinkedHashSet<>(masterEntity.getTokens().stream().map(item -> item.getSecurityToken().getName()).collect(Collectors.toList()));
        entity.setChosenIds(chosenRoleIds);
        return entity;
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
    
    @Override
    protected AbstractEntity<?> getMasterEntityFromContext(final CentreContext<?, ?> context) {
        // this producer is suitable for property actions on User Role master and for actions on User Role centre
        return context.getMasterEntity() == null ? context.getCurrEntity() : context.getMasterEntity();
    }

    @Override
    protected fetch<UserRole> fetchModelForMasterEntity() {
        return coUserRole.getFetchProvider().with("tokens").fetchModel();
    }
}