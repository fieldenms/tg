package ua.com.fielden.platform.menu;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModificationProducer;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

/**
 * Producer for {@link UserMenuVisibilityAssociator} entity.
 *
 * @author TG Team
 *
 */
public class UserMenuVisibilityAssociatorProducer extends AbstractFunctionalEntityForCollectionModificationProducer<WebMenuItemInvisibility, UserMenuVisibilityAssociator, Long, User> {

    private final ICollectionModificationController<WebMenuItemInvisibility, UserMenuVisibilityAssociator, Long, User> controller;
    private IUser coUser;
    private final IUserProvider userProvider;

    @Inject
    public UserMenuVisibilityAssociatorProducer(final EntityFactory factory, final IUserProvider userProvider, final ICompanionObjectFinder companionFinder) {
        super(factory, UserMenuVisibilityAssociator.class, companionFinder);
        this.coUser = co(User.class);
        this.userProvider = userProvider;
        this.controller = new UserMenuVisibilityAssociatorController(this.coUser, this.userProvider);
    }

    @Override
    protected ICollectionModificationController<WebMenuItemInvisibility, UserMenuVisibilityAssociator, Long, User> controller() {
        return controller;
    }

    @Override
    protected UserMenuVisibilityAssociator provideCurrentlyAssociatedValues(final UserMenuVisibilityAssociator entity, final WebMenuItemInvisibility masterEntity) {
        controller.setAvailableItems(entity, controller.refetchAvailableItems(masterEntity));
        final User currentUser = userProvider.getUser();
        if (currentUser.isBase()) {
            final EntityResultQueryModel<User> query = select(User.class).where().prop("base").eq().val(false).and()
                    .prop("active").eq().val(true).and().prop("basedOnUser").eq().val(currentUser).and()
                    .notExists(
                            select(WebMenuItemInvisibility.class).where().prop("owner").eq().extProp("id")
                            .and().prop("menuItemUri").eq().val(masterEntity.getMenuItemUri()).model())
                    .model();
            entity.setChosenIds(coUser.getAllEntities(from(query).with(fetchKeyAndDescOnly(User.class)).model())
                    .stream().map(User::getId).collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        return entity;
    }

}
