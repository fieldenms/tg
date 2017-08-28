package ua.com.fielden.platform.security.user;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IUserRole;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.UserReviewToken;

/**
 * A producer for new instances of entity {@link UserRolesUpdater}.
 *
 * @author TG Team
 *
 */
public class UserRolesUpdaterProducer extends AbstractFunctionalEntityForCollectionModificationProducer<User, UserRolesUpdater, Long, UserRole> {
    private final ICollectionModificationController<User, UserRolesUpdater, Long, UserRole> controller;
    
    @Inject
    public UserRolesUpdaterProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, UserRolesUpdater.class, companionFinder);
        this.controller = new UserRolesUpdaterController(co(User.class), co$(UserRolesUpdater.class), this.<IUserRole, UserRole>co(UserRole.class));
    }
    
    @Override
    protected ICollectionModificationController<User, UserRolesUpdater, Long, UserRole> controller() {
        return controller;
    }
    
    @Override
    @Authorise(UserReviewToken.class)
    protected UserRolesUpdater provideCurrentlyAssociatedValues(final UserRolesUpdater entity, final User masterEntity) {
        controller.setAvailableItems(entity, controller.refetchAvailableItems(masterEntity));
        entity.setChosenIds(new LinkedHashSet<>(masterEntity.getRoles().stream().map(item -> item.getUserRole().getId()).collect(Collectors.toList())));
        return entity;
    }
    
}