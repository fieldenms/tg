package ua.com.fielden.platform.menu;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
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

    private final Logger logger = Logger.getLogger(this.getClass());

    private final IUserProvider userProvider;

    @Inject
    public MenuSaveActionDao(final IFilter filter, final IUserProvider userProvider) {
        super(filter);
        this.userProvider = userProvider;
    }

    @Override
    @SessionRequired
    public MenuSaveAction save(final MenuSaveAction entity) {
        if (userProvider.getUser().isBase()) {
            final IWebMenuItemInvisibility coMenuInvisibility = co$(WebMenuItemInvisibility.class);
            if (!entity.getInvisibleMenuItems().isEmpty()) {
                final Set<User> availableUsers = getAvailableUsers();
                final Set<WebMenuItemInvisibility> nonVisibleItems = getAvailableNonVisibleMenuItems(availableUsers, entity.getInvisibleMenuItems());
                entity.getInvisibleMenuItems().forEach(menuItem -> {
                    try {
                        availableUsers.forEach(nonBaseUser -> {
                            final WebMenuItemInvisibility newMenuItem = getEntityFactory().newByKey(WebMenuItemInvisibility.class, nonBaseUser, menuItem);
                            if (!nonVisibleItems.contains(newMenuItem)) {
                                coMenuInvisibility.quickSave(newMenuItem);
                            }
                        });
                    } catch (final EntityCompanionException e) {
                        logger.error(e.getMessage());
                    }
                });
            }
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

    private Set<User> getAvailableUsers() {
        final Set<User> availableUsers = new HashSet<>();
        final User user = userProvider.getUser();
        if (user.isBase()) {
            availableUsers.addAll(co(User.class).getAllEntities(from(
                    select(User.class).where()
                    .prop("active").eq().val(true).and()
                    .prop("base").eq().val(false).and()
                    .prop("basedOnUser").eq().val(user).model()).with(fetchKeyAndDescOnly(User.class)).model()));
        } else {
            availableUsers.add(user);
        }
        return availableUsers;
    }

    private Set<WebMenuItemInvisibility> getAvailableNonVisibleMenuItems(final Set<User> users, final Set<String> menuItemUris) {
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