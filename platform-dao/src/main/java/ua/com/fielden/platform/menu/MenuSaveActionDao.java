package ua.com.fielden.platform.menu;

import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Collection;
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
 * @author Developers
 *
 */
@EntityType(MenuSaveAction.class)
public class MenuSaveActionDao extends CommonEntityDao<MenuSaveAction> implements IMenuSaveAction {

    private final IUserProvider userProvider;

    @Inject
    public MenuSaveActionDao(final IFilter filter, final IUserProvider userProvider) {
        super(filter);
        this.userProvider = userProvider;
    }

    /**
     * Saves user action of clicking on menu item checkbox. This should save or remove {@link WebMenuItemInvisibility} entries for each non base
     * user based on current base user. However such save behaviour may encounter situation when such {@link WebMenuItemInvisibility} entity already exists.
     * Therefore one should check whether created {@link WebMenuItemInvisibility} exists before save.
     */
    @Override
    @SessionRequired
    public MenuSaveAction save(final MenuSaveAction entity) {
        if (userProvider.getUser().isBase()) {
            final IWebMenuItemInvisibility coMenuInvisibility = co$(WebMenuItemInvisibility.class);
            //Save new WebMenuItemInvisibility entities
            if (!entity.getInvisibleMenuItems().isEmpty()) {
                //Get all non base users and their invisible menu item entries to check entity existence before save
                final Set<User> availableUsers = getAvailableNonBaseUsers();
                final Set<WebMenuItemInvisibility> nonVisibleItems = getAvailableInvisibleMenuItems(availableUsers, entity.getInvisibleMenuItems());
                entity.getInvisibleMenuItems().stream()
                      .map(menuItemUri -> availableUsers
                              .stream()
                              .map(user -> getEntityFactory().newByKey(WebMenuItemInvisibility.class, user, menuItemUri)).collect(toList()))
                      .flatMap(Collection::stream)
                      .filter(newMenuItem -> !nonVisibleItems.contains(newMenuItem))
                      .forEach(webMenuItemInvisibility -> coMenuInvisibility.save(webMenuItemInvisibility));
            }
            //Remove WebMenuItemInvisibility entities for active non base users based on current base user and menu item URIs specified in menuSaveAction
            if (!entity.getVisibleMenuItems().isEmpty()) {
                final EntityResultQueryModel<WebMenuItemInvisibility> model = select(WebMenuItemInvisibility.class).where()//
                .prop("owner.base").eq().val(false).and()
                .prop("owner.active").eq().val(true).and()
                .prop("owner.basedOnUser").eq().val(userProvider.getUser()).and()//
                .prop("menuItemUri").in().values(entity.getVisibleMenuItems().toArray(new String[0])).model();
                coMenuInvisibility.batchDelete(model);
            }
        }
        return entity;
    }

    /**
     * Returns all non base active users those are based on current base user.
     *
     * @return
     */
    private Set<User> getAvailableNonBaseUsers() {
        final Set<User> availableUsers = new HashSet<>();
        final User user = userProvider.getUser();
        if (user.isBase()) {
            final IUser coUser = co(User.class);//findNonBaseUser
            availableUsers.addAll(coUser.findNonBaseUsers(user, co(WebMenuItemInvisibility.class).getFetchProvider().<User>fetchFor("owner").fetchModel()));
        } else {
            availableUsers.add(user);
        }
        return availableUsers;
    }

    /**
     * Returns all {@link WebMenuItemInvisibility} instances with specified users as owners and specified menu item URIs.
     *
     * @param users
     * @param menuItemUris
     * @return
     */
    private Set<WebMenuItemInvisibility> getAvailableInvisibleMenuItems(final Set<User> users, final Set<String> menuItemUris) {
        if (!users.isEmpty()) {
            return new HashSet<>(co(WebMenuItemInvisibility.class).getAllEntities(from(
                    select(WebMenuItemInvisibility.class).where()
                    .prop("owner").in().values(users.toArray()).and()
                    .prop("menuItemUri").in().values(menuItemUris.toArray())
                    .model()).with(fetchKeyAndDescOnly(WebMenuItemInvisibility.class)).model()));
        }
        return new HashSet<>();
    }
}