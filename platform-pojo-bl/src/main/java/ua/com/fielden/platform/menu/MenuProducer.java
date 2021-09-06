package ua.com.fielden.platform.menu;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.EntityUtils.fetchNotInstrumentedWithKeyAndDesc;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
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

    private boolean isMenuVisibleForAllUsers(final IMenuManager menuItem) {
        if (userProvider.getUser().isBase()) {
            return !menuItem.getMenu().stream().anyMatch(item -> !item.isVisibleForAllUsers());
        }
        return true;
    }

    private Menu buildMenuConfiguration() {
        final Menu menu = menuRetriever.getMenuEntity(DESKTOP).setUserName(userProvider.getUser().getKey()).setCanEdit(userProvider.getUser().isBase());
        //Get all invisible menu items
        final Map<String, Set<User>> invisibleItems = miInvisible.getAllEntities(createMenuInvisibilityQuery()).stream()
                .collect(groupingBy(WebMenuItemInvisibility::getMenuItemUri, Collectors.mapping(WebMenuItemInvisibility::getOwner, toSet())));
        //Remove all retrieved menu items from the menu entity.
        for (final String menuItem : invisibleItems.keySet()) {
            final List<String> menuParts = decodeParts(menuItem.split("/"));
            processMenuItem(Arrays.asList(menu), menuParts, invisibleItems.get(menuItem));
        }
        return menu;
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
            return queryPart
                    .begin()
                        .prop("owner").eq().val(user)
                        .or().prop("owner.basedOnUser").eq().val(user)
                    .end().and();
        } else {
            return queryPart
                    .begin()
                        .prop("owner").eq().val(user)
                        .or().prop("owner").eq().val(user.getBasedOnUser())
                    .end().and();
        }
    }

    private void processMenuItem(final List<IMenuManager> menuPath, final List<String> menuParts, final Set<User> excludedUsers) {
        if (menuParts.size() > 1) {
            final IMenuManager lastItem = menuPath.get(menuPath.size() - 1);
            final Optional<IMenuManager> nextItem = lastItem.getMenuItem(menuParts.get(0));
            nextItem.ifPresent(i -> {
                final ArrayList<IMenuManager> newMenuPath = new ArrayList<>(menuPath);
                newMenuPath.add(i);
                processMenuItem(newMenuPath, menuParts.subList(1, menuParts.size()), excludedUsers);
            });
        }
        if (menuPath.size() > 2 && menuParts.size() > 0) {
            final IMenuManager lastMenuItem = menuPath.get(menuPath.size() - 1);
            if (menuParts.size() == 1) {
                computeVisibility(lastMenuItem, menuParts.get(0), excludedUsers);
            }
            final IMenuManager beforeLastMenuItem = menuPath.get(menuPath.size() - 2);
            if (!isMenuVisible(lastMenuItem)) {
                computeVisibilityForGroup(beforeLastMenuItem, lastMenuItem.getTitle());
            }
            if (!isMenuVisibleForAllUsers(lastMenuItem)) {
                computeVisibilityForAllUsersAndGroup(beforeLastMenuItem, lastMenuItem.getTitle());
            }
        }
    }

    private void computeVisibilityForAllUsersAndGroup(final IMenuManager parentItem, final String menuItemTitle) {
        final User currentUser = userProvider.getUser();
        if (currentUser.isBase()) {
            parentItem.makeMenuItemInvisibleForSomeUser(menuItemTitle);
        }
    }

    private void computeVisibilityForGroup(final IMenuManager parentItem, final String menuItemTitle) {
        final User currentUser = userProvider.getUser();
        if (currentUser.isBase()) {
            parentItem.makeMenuItemInvisible(menuItemTitle);
        } else {
            parentItem.removeMenuItem(menuItemTitle);
        }
    }

    private void computeVisibility(final IMenuManager parentItem, final String menuItemTitle, final Set<User> excludedUsers) {
        final User currentUser = userProvider.getUser();
        if (currentUser.isBase()) {
            if (excludedUsers.contains(currentUser)) {
                parentItem.makeMenuItemInvisible(menuItemTitle);
            }
            if (excludedUsers.size() > 1 || !excludedUsers.contains(currentUser)) {
                parentItem.makeMenuItemInvisibleForSomeUser(menuItemTitle);
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
