package ua.com.fielden.platform.menu;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.EntityUtils.fetchNotInstrumentedWithKeyAndDesc;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

public class MenuProducer extends DefaultEntityProducerWithContext<Menu> {

    private static final Logger logger = Logger.getLogger(MenuProducer.class);

    private final IMenuRetriever menuRetriever;
    private final IUserProvider userProvider;
    private final IWebMenuItemInvisibility miInvisible;

    @Inject
    public MenuProducer(
            final IMenuRetriever menuRetriever,
            final IWebMenuItemInvisibility miInvisible,
            final IUserProvider userProvider,
            final ICompanionObjectFinder coFinder,
            final EntityFactory entityFactory) {
        super(entityFactory, Menu.class, coFinder);
        this.menuRetriever = menuRetriever;
        this.miInvisible = miInvisible;
        this.userProvider = userProvider;
    }

    @Override
    protected Menu provideDefaultValues(final Menu entity) {
        final Menu menu;
        if (chosenPropertyEqualsTo("desktop")) {
            menu = buildMenuConfiguration();
        } else { // chosenPropertyEqualsTo("mobile")
            menu = menuRetriever.getMenuEntity(MOBILE).setUserName(userProvider.getUser().getKey()).setCanEdit(false);
            // At this stage all items are visible for mobile profile.
            // In case where invisibility logic should be implemented, there is a need to extend persistent WebMenuItemInvisibility entity.
        }
        return menu.copyTo(entity);
    }

    private boolean isMenuVisible(final IMenuManager menuItem) {
        if (userProvider.getUser().isBase()) {
            return menuItem.getMenu().stream().anyMatch(item -> item.isVisible());
        } else {
            return !menuItem.getMenu().isEmpty();
        }
    }

    private boolean isMenuSemiVisible(final IMenuManager menuItem) {
        if (userProvider.getUser().isBase()) {
            return menuItem.getMenu().stream().anyMatch(item -> !item.isVisible() || item.isSemiVisible());
        }
        return false;
    }

    private Menu buildMenuConfiguration() {
        final Menu menu = menuRetriever.getMenuEntity(DESKTOP).setUserName(userProvider.getUser().getKey()).setCanEdit(userProvider.getUser().isBase());
        //Get all invisible menu items
        final Map<String, Set<User>> invisibleItems = miInvisible.getAllEntities(createMenuInvisibilityQuery()).stream()
                .collect(groupingBy(WebMenuItemInvisibility::getMenuItemUri, Collectors.mapping(WebMenuItemInvisibility::getOwner, toSet())));
        final Set<User> availableUsers = getAvailableUsers();
        //Remove all retrieved menu items from the menu entity.
        for (final String menuItem : invisibleItems.keySet()) {
            //Split menu items by '/' and decode it with URL decoder
            final List<String> menuParts = decodeParts(menuItem.split("/"));
            //Find menu path that is a list of menu managers starting from menu, then module menu and so on, that corresponds to menuParts
            final List<IMenuManager> menuPath = findMenuPath(menu, menuParts);
            //If menu item was found (e.a. menu path is not empty) then process each menu item with his parent until module menu to set their visible and semiVisible properties
            final Set<User> excludedUsers = invisibleItems.get(menuItem);
            if (menuPath.size() > 2) {
                computeVisibility(menuPath.get(menuPath.size() - 2), menuPath.get(menuPath.size() - 1).getTitle(), excludedUsers, availableUsers);
                for (int itemIndex = menuPath.size() - 2; itemIndex > 1; itemIndex --) {
                    computeVisibilityForGroup(menuPath.get(itemIndex - 1), menuPath.get(itemIndex));
                }
            }
        }
        return menu;
    }

    private List<IMenuManager> findMenuPath(final IMenuManager menu, final List<String> menuParts) {
        final List<IMenuManager> menuPath = new  ArrayList<>();
        menuPath.add(menu);
        for(final String menuPart : menuParts) {
            final IMenuManager lastItem = menuPath.get(menuPath.size() - 1);
            final Optional<IMenuManager> nextItem = lastItem.getMenuItem(menuPart);
            if (nextItem.isPresent()) {
                menuPath.add(nextItem.get());
            } else {
                return new ArrayList<>();
            }
        }
        return menuPath;
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

    private QueryExecutionModel<WebMenuItemInvisibility, EntityResultQueryModel<WebMenuItemInvisibility>> createMenuInvisibilityQuery() {
        return from(buildQueryPartForUserType(select(WebMenuItemInvisibility.class).where())
                .prop("owner.active").eq().val(true)
                .model()).
                with(fetchNotInstrumentedWithKeyAndDesc(WebMenuItemInvisibility.class).fetchModel()).model();
    }

    private IWhere0<WebMenuItemInvisibility> buildQueryPartForUserType(final IWhere0<WebMenuItemInvisibility> queryPart) {
        final User user = userProvider.getUser();
        if (user.isBase()) {
            return queryPart.prop("owner.basedOnUser").eq().val(user).and();
        } else {
            return queryPart.prop("owner").eq().val(user).and();
        }
    }

    private void computeVisibilityForGroup(final IMenuManager parentItem, final IMenuManager menuItem) {
        final User currentUser = userProvider.getUser();
        if (!isMenuVisible(menuItem)) {
            if (currentUser.isBase()) {
                parentItem.makeMenuItemInvisible(menuItem.getTitle());
            } else {
                parentItem.removeMenuItem(menuItem.getTitle());
            }
        } else if (isMenuSemiVisible(menuItem)) {
            if (currentUser.isBase()) {
                parentItem.makeMenuItemSemiVisible(menuItem.getTitle());
            }
        }
    }

    private void computeVisibility(final IMenuManager parentItem, final String menuItemTitle, final Set<User> excludedUsers, final Set<User> availableUsers) {
        final User currentUser = userProvider.getUser();
        if (currentUser.isBase()) {
            if (excludedUsers.containsAll(availableUsers)) {
                parentItem.makeMenuItemInvisible(menuItemTitle);
            } else {
                parentItem.makeMenuItemSemiVisible(menuItemTitle);
            }
        } else {
            parentItem.removeMenuItem(menuItemTitle);
        };
    }

    public static List<String> decodeParts(final String[] menuParts) {
        final List<String> decodedParts = new ArrayList<>();
        try {
            for (int partIndex = 0; partIndex < menuParts.length; partIndex++) {
                decodedParts.add(URLDecoder.decode(menuParts[partIndex], "UTF-8"));
            }
        } catch (final UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
        return decodedParts;
    }
}
