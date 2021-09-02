package ua.com.fielden.platform.menu;

import static java.util.stream.Collectors.toCollection;

import java.util.LinkedHashSet;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

public class UserMenuItemAssociatorProducer extends AbstractFunctionalEntityForCollectionModificationProducer<WebMenuItemInvisibility, UserMenuVisibilityAssociator, Long, User> {

    private final ICollectionModificationController<WebMenuItemInvisibility, UserMenuVisibilityAssociator, Long, User> controller;
    @Inject
    public UserMenuItemAssociatorProducer(final EntityFactory factory, final IUserProvider userProvider, final ICompanionObjectFinder companionFinder) {
        super(factory, UserMenuVisibilityAssociator.class, companionFinder);
        this.controller = new UserMenuVisibilityAssociatorController(co(User.class), userProvider);
    }

    @Override
    protected ICollectionModificationController<WebMenuItemInvisibility, UserMenuVisibilityAssociator, Long, User> controller() {
        return controller;
    }

    @Override
    protected UserMenuVisibilityAssociator provideCurrentlyAssociatedValues(final UserMenuVisibilityAssociator entity, final WebMenuItemInvisibility masterEntity) {
        controller.setAvailableItems(entity, controller.refetchAvailableItems(masterEntity));
        entity.setChosenIds(masterEntity.getRoles().stream().map(item -> item.getUserRole().getId()).collect(toCollection(LinkedHashSet::new)));
        return entity;
    }

}
