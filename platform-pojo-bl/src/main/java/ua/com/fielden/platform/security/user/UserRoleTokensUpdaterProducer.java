package ua.com.fielden.platform.security.user;

import static java.util.stream.Collectors.toCollection;

import java.util.LinkedHashSet;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.provider.ISecurityTokenNodeTransformation;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.tokens.user.UserRoleTokensUpdater_CanExecute_Token;

/**
 * A producer for new instances of entity {@link UserRoleTokensUpdater}.
 *
 * @author TG Team
 *
 */
public class UserRoleTokensUpdaterProducer extends AbstractFunctionalEntityForCollectionModificationProducer<UserRole, UserRoleTokensUpdater, String, SecurityTokenInfo> {

    private final ICollectionModificationController<UserRole, UserRoleTokensUpdater, String, SecurityTokenInfo> controller;

    @Inject
    public UserRoleTokensUpdaterProducer(
            final EntityFactory factory, 
            final ICompanionObjectFinder companionFinder, 
            final ISecurityTokenNodeTransformation tokenTransformation,
            final ISecurityTokenProvider securityTokenProvider) {
        super(factory, UserRoleTokensUpdater.class, companionFinder);
        this.controller = new UserRoleTokensUpdaterController(factory, co(UserRole.class), co$(UserRoleTokensUpdater.class), tokenTransformation, securityTokenProvider);
    }
    
    @Override
    protected ICollectionModificationController<UserRole, UserRoleTokensUpdater, String, SecurityTokenInfo> controller() {
        return controller;
    }
    
    @Override
    @Authorise(UserRoleTokensUpdater_CanExecute_Token.class)
    protected UserRoleTokensUpdater provideCurrentlyAssociatedValues(final UserRoleTokensUpdater entity, final UserRole masterEntity) {
        controller.setAvailableItems(entity, controller.refetchAvailableItems(masterEntity));
        entity.setChosenIds(masterEntity.getTokens().stream().map(item -> item.getSecurityToken().getName()).collect(toCollection(LinkedHashSet::new)));
        return entity;
    }
    
}