package ua.com.fielden.platform.menu;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

/**
 * DAO implementation for companion object {@link IMenuSaveAction}.
 *
 * @author TG Team
 *
 */
@EntityType(MenuSaveAction.class)
public class MenuSaveActionDao extends CommonEntityDao<MenuSaveAction> implements IMenuSaveAction {

    private final IUserProvider userProvider;

    @Inject
    protected MenuSaveActionDao(final IUserProvider userProvider) {
        this.userProvider = userProvider;
    }

    /**
     * Saves or deletes {@link WebMenuItemInvisibility} entries for each user who is based on the current base user.
     */
    @Override
    @SessionRequired
    public MenuSaveAction save(final MenuSaveAction action) {
        final User currUser = userProvider.getUser();
        if (currUser.isBase()) {
            final WebMenuItemInvisibilityCo coMenuInvisibility = co$(WebMenuItemInvisibility.class);
            // save new WebMenuItemInvisibility entities
            if (!action.getInvisibleMenuItems().isEmpty()) {
                //Get all non base users and their invisible menu item entries to check entity existence before save
                final Set<User> users = findActiveUsersBasedOn(currUser);
                final Set<WebMenuItemInvisibility> invisibleItems = findInvisibleMenuItems(users, action.getInvisibleMenuItems());
                action.getInvisibleMenuItems().stream()
                      .flatMap(menuItemUri -> users
                              .stream()
                              .map(user -> coMenuInvisibility.new_().setOwner(user).setMenuItemUri(menuItemUri)))
                      .filter(newMenuItem -> !invisibleItems.contains(newMenuItem))
                      .forEach(webMenuItemInvisibility -> coMenuInvisibility.save(webMenuItemInvisibility));
            }
            // delete WebMenuItemInvisibility entities for active users based on the current base user and menu item URIs specified in the action
            if (!action.getVisibleMenuItems().isEmpty()) {
                final EntityResultQueryModel<WebMenuItemInvisibility> model = select(WebMenuItemInvisibility.class).where()
                    .prop("owner.base").eq().val(false).and()
                    .prop("owner.active").eq().val(true).and()
                    .prop("owner.basedOnUser").eq().val(currUser).and()
                    .prop("menuItemUri").in().values(action.getVisibleMenuItems().toArray()).model();
                coMenuInvisibility.batchDelete(model);
            }
        }
        return action;
    }

    /**
     * Returns all active users that are based on the specified user.
     *
     * @param baseUser - a base user; an exception is thrown if this is not a base user.
     * @return a set of users based on the specified user.
     */
    private Set<User> findActiveUsersBasedOn(final User baseUser) {
        if (!baseUser.isBase()) {
            throw new SecurityException(String.format("A bse user is expected. User [%s] is not a base user.", baseUser));
        }
        final IUser coUser = co(User.class);
        return coUser.findBasedOnUsers(baseUser, WebMenuItemInvisibilityCo.FETCH_PROVIDER.<User>fetchFor("owner").fetchModel());
    }

    /**
     * Returns all {@link WebMenuItemInvisibility} instances with specified users as owners and specified menu item URIs.
     *
     * @param users
     * @param menuItemUris
     * @return
     */
    private Set<WebMenuItemInvisibility> findInvisibleMenuItems(final Set<User> users, final Set<String> menuItemUris) {
        if (users.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(co(WebMenuItemInvisibility.class).getAllEntities(from(
                select(WebMenuItemInvisibility.class).where()
                .prop("owner").in().values(users.toArray()).and()
                .prop("menuItemUri").in().values(menuItemUris.toArray())
                .model()).with(fetchKeyAndDescOnly(WebMenuItemInvisibility.class)).model()));
    }

}
