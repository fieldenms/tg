package ua.com.fielden.platform.security.user;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.user.UserRoleReviewToken;

/**
 * A producer for new instances of entity {@link UserRoleTokensUpdater}.
 *
 * @author TG Team
 *
 */
public class UserRoleTokensUpdaterProducer extends AbstractFunctionalEntityForCollectionModificationProducer<UserRole, UserRoleTokensUpdater, String, SecurityTokenInfo> implements IEntityProducer<UserRoleTokensUpdater> {
    private final SecurityTokenProvider securityTokenProvider;
    private final ICollectionModificationController<UserRole, UserRoleTokensUpdater, String, SecurityTokenInfo> controller;
    
    @Inject
    public UserRoleTokensUpdaterProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final IApplicationSettings applicationSettings) {
        super(factory, UserRoleTokensUpdater.class, companionFinder);
        this.controller = new UserRoleTokensUpdaterController(factory, applicationSettings, co(UserRole.class), co(UserRoleTokensUpdater.class));
        this.securityTokenProvider = new SecurityTokenProvider(applicationSettings.pathToSecurityTokens(), applicationSettings.securityTokensPackageName());
    }
    
    @Override
    protected ICollectionModificationController<UserRole, UserRoleTokensUpdater, String, SecurityTokenInfo> controller() {
        return controller;
    }
    
    @Override
    @Authorise(UserRoleReviewToken.class)
    protected UserRoleTokensUpdater provideCurrentlyAssociatedValues(final UserRoleTokensUpdater entity, final UserRole masterEntity) {
        entity.setTokens(loadAvailableTokens(securityTokenProvider, factory()));
        entity.setChosenIds(new LinkedHashSet<>(masterEntity.getTokens().stream().map(item -> item.getSecurityToken().getName()).collect(Collectors.toList())));
        return entity;
    }
    
    public static Set<SecurityTokenInfo> loadAvailableTokens(final SecurityTokenProvider securityTokenProvider, final EntityFactory factory) {
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