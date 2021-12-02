package ua.com.fielden.platform.menu;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.entity.IContextDecomposer;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Controller for {@link UserMenuVisibilityAssociator} that provides access to master entity and available users.
 *
 * @author TG Team
 *
 */
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
            return coUser.findBasedOnUsers(user, WebMenuItemInvisibilityCo.FETCH_PROVIDER.<User>fetchFor("owner").fetchModel());
        }
        return Collections.emptySet();
    }

}