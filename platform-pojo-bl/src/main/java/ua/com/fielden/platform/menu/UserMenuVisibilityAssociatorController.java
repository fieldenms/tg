package ua.com.fielden.platform.menu;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.IContextDecomposer;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.centre.CentreContext;

public class UserMenuVisibilityAssociatorController implements ICollectionModificationController<WebMenuItemInvisibility, UserMenuVisibilityAssociator, Long, User> {

    private IUser coUser;
    private final IUserProvider userProvider;

    public UserMenuVisibilityAssociatorController(final IUser coUser, final IUserProvider userProvider) {
        this.userProvider = userProvider;
        this.coUser = coUser;
    }

    @Override
    public WebMenuItemInvisibility getMasterEntityFromContext(final CentreContext<?, ?> context) {
        return IContextDecomposer.decompose(context).currentEntity(WebMenuItemInvisibility.class);
    }

    @Override
    public UserMenuVisibilityAssociator setAvailableItems(final UserMenuVisibilityAssociator action, final Collection<User> availableItems) {
        return action.setUsers((Set<User>)availableItems);
    }

    @Override
    public Collection<User> refetchAvailableItems(final WebMenuItemInvisibility masterEntity) {
        final User user = userProvider.getUser();
        if (user.isBase()) {
            return new LinkedHashSet<>(coUser.getAllEntities(
                    from(select(User.class).where().prop("base").eq().val(false).and().prop("active").eq().val(true).and().prop("basedOnUser").eq().val(user).model())
                    .with(coUser.getFetchProvider().fetchModel()).with(orderBy().prop("key").asc().model())
                    .model()));
        }
        return new LinkedHashSet<User>();
    }
}
