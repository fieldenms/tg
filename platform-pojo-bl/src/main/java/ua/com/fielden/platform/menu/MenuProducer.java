package ua.com.fielden.platform.menu;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.menu.exceptions.MenuInitialisationException;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.try_wrapper.TryWrapper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.utils.EntityUtils.fetchNotInstrumentedWithKeyAndDesc;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;

public class MenuProducer extends DefaultEntityProducerWithContext<Menu> {

    private static final Logger LOGGER = getLogger(MenuProducer.class);
    private static final int DEFAULT_EXTERNAL_SITE_EXPIRY_DAYS = 183;

    private final IMenuRetriever menuRetriever;
    private final IUserProvider userProvider;
    private final WebMenuItemInvisibilityCo miInvisible;
    private final Set<String> siteAllowlist;
    private final int daysUntilSitePermissionExpires;
    private final IApplicationSettings appSettings;

    @Inject
    public MenuProducer(
            final IMenuRetriever menuRetriever,
            final WebMenuItemInvisibilityCo miInvisible,
            final IUserProvider userProvider,
            final ICompanionObjectFinder coFinder,
            final EntityFactory entityFactory,
            final IApplicationSettings appSettings,
            final @Named("externalSites.allowlist") String siteAllowlist,
            final @Named("externalSites.expiresIn") String expiryDays) {
        super(entityFactory, Menu.class, coFinder);
        this.menuRetriever = menuRetriever;
        this.miInvisible = miInvisible;
        this.userProvider = userProvider;
        this.siteAllowlist = TryWrapper.Try( () -> stream(siteAllowlist.trim().split("\\s*,\\s*"))
                        // Exclude blank entries.
                        .filter(StringUtils::isNotBlank)
                        // Wildcard subdomains should expand to include the corresponding domains.
                        // For example, `*.google.com` should expand to `*.google.com` and `google.com`.
                        .flatMap(site -> site.startsWith("*.") ? Stream.of(site, site.substring(2)) : Stream.of(site))
                        // Convert sites to JavaScript regular expressions.
                        .map(site -> site.toLowerCase().replaceAll("[\"']", "").replace(".", "\\.").replace("*", ".*"))
                        .collect(toSet()))
                .orElseThrow(ex -> new MenuInitialisationException("Could not parse value for 'siteAllowlist': %s".formatted(ex.getMessage())));
        this.daysUntilSitePermissionExpires = TryWrapper.Try( () -> isEmpty(expiryDays) ? DEFAULT_EXTERNAL_SITE_EXPIRY_DAYS : Integer.parseInt(expiryDays) )
                .orElseThrow(ex -> new MenuInitialisationException("Could not parse value for 'daysUntilSitePermissionExpires': %s".formatted(ex.getMessage())));
        this.appSettings = appSettings;
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
        menu.setSiteAllowlist(this.siteAllowlist);
        menu.setDaysUntilSitePermissionExpires(this.daysUntilSitePermissionExpires);
        menu.setCurrencySymbol(appSettings.currencySymbol());
        return menu.copyTo(entity);
    }

    /**
     * Determines whether specified menu item with sub menu is visible or not.
     *
     * @param menuItem
     * @return
     */
    private boolean isMenuVisible(final IMenuManager menuItem) {
        if (userProvider.getUser().isBase()) {
            return menuItem.getMenu().stream().anyMatch(item -> item.isVisible());
        } else {
            return !menuItem.getMenu().isEmpty();
        }
    }

    /**
     * Determines whether specified menu item with menu is semi visible or not.
     *
     * @param menuItem
     * @return
     */
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
        //Remove or make invisible all retrieved invisible menu items those are in menu entity.
        for (final String menuItem : invisibleItems.keySet()) {
            //Split menu items by '/' and decode it with URL decoder
            final List<String> menuParts = decodeParts(menuItem.split("/"));
            //Find menu path that is a list of menu managers starting from menu, then module menu and so on, that corresponds to menuParts
            final List<IMenuManager> menuPath = findMenuPath(menu, menuParts);
            //If menu item was found (e.a. menu path is not empty) then process each menu item with his parent until module menu to set their visible and semiVisible properties
            final Set<User> excludedUsers = invisibleItems.get(menuItem);
            if (menuPath.size() > 2) {
                //Compute visibility of the leaf menu item
                computeVisibility(menuPath.get(menuPath.size() - 2), menuPath.get(menuPath.size() - 1).getTitle(), excludedUsers, availableUsers);
                //Compute visibility of all group menu items
                for (int itemIndex = menuPath.size() - 2; itemIndex > 1; itemIndex--) {
                    computeVisibilityForGroup(menuPath.get(itemIndex - 1), menuPath.get(itemIndex));
                }
            }
        }
        return menu;
    }

    /**
     * Returns a list of menu items that correspond to menu item titles specified in {@code menuParts}.
     *
     * @param menu
     * @param menuParts
     * @return
     */
    private List<IMenuManager> findMenuPath(final IMenuManager menu, final List<String> menuParts) {
        final List<IMenuManager> menuPath = new  ArrayList<>();
        menuPath.add(menu);
        for (final String menuPart : menuParts) {
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

    /**
     * Returns all active based-on users if current user is base. Otherwise, returns a list with only the current user.
     *
     * @return
     */
    private Set<User> getAvailableUsers() {
        final Set<User> availableUsers = new LinkedHashSet<>();
        final User user = userProvider.getUser();
        if (user.isBase()) {
            final IUser coUser = co(User.class);
            availableUsers.addAll(coUser.findBasedOnUsers(user, fetchKeyAndDescOnly(User.class)));
        } else {
            availableUsers.add(user);
        }
        return availableUsers;
    }

    /**
     * Creates query to retrieve all invisible menu items for current user, regardless of whether it is base or not.
     *
     * @return
     */
    private QueryExecutionModel<WebMenuItemInvisibility, EntityResultQueryModel<WebMenuItemInvisibility>> createMenuInvisibilityQuery() {
        return from(buildQueryPartForUserType(select(WebMenuItemInvisibility.class).where())
                .prop("owner.active").eq().val(true)
                .model())
                .with(fetchNotInstrumentedWithKeyAndDesc(WebMenuItemInvisibility.class).fetchModel()).model();
    }

    /**
     * Creates query condition for invisibility menu item owner, regardless of whether it is base or not.
     *
     * @param queryPart
     * @return
     */
    private IWhere0<WebMenuItemInvisibility> buildQueryPartForUserType(final IWhere0<WebMenuItemInvisibility> queryPart) {
        final User user = userProvider.getUser();
        if (user.isBase()) {
            return queryPart.prop("owner.basedOnUser").eq().val(user).and();
        } else {
            return queryPart.prop("owner").eq().val(user).and();
        }
    }

    /**
     * Computes {@code visible} and {@code semiVisible} properties for a group menu item specified in {@code menuItem}.
     *
     * @param parentItem
     * @param menuItem
     */
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

    /**
     * Computes {@code visible} and {@code semiVisible} property for menu with title {@code menuItemTitle}.
     *
     * @param parentItem
     * @param menuItemTitle
     * @param excludedUsers
     * @param availableUsers
     */
    private void computeVisibility(final IMenuManager parentItem, final String menuItemTitle, final Set<User> excludedUsers, final Set<User> availableUsers) {
        final User currentUser = userProvider.getUser();
        if (currentUser.isBase()) {
            //If menu item is invisible for all available non base user then it is invisible otherwise it is semi visible.
            if (excludedUsers.containsAll(availableUsers)) {
                parentItem.makeMenuItemInvisible(menuItemTitle);
            } else {
                parentItem.makeMenuItemSemiVisible(menuItemTitle);
            }
        } else {
            parentItem.removeMenuItem(menuItemTitle);
        }
    }

    /**
     * Decodes menu item URI.
     *
     * @param menuParts
     * @return
     */
    public static List<String> decodeParts(final String[] menuParts) {
        final List<String> decodedParts = new ArrayList<>();
        try {
            for (int partIndex = 0; partIndex < menuParts.length; partIndex++) {
                decodedParts.add(URLDecoder.decode(menuParts[partIndex], "UTF-8"));
            }
        } catch (final UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage());
        }
        return decodedParts;
    }

}