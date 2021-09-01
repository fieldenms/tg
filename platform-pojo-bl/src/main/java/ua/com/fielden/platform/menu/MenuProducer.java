package ua.com.fielden.platform.menu;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.MOBILE;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.EntityUtils;

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
            menu = buildMenuForBaseUserConfiguration();
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

    private Menu buildMenuForBaseUserConfiguration() {
        final Menu menu = menuRetriever.getMenuEntity(DESKTOP).setUserName(userProvider.getUser().getKey()).setCanEdit(userProvider.getUser().isBase());
        //Get all invisible menu items
        final QueryExecutionModel<WebMenuItemInvisibility, EntityResultQueryModel<WebMenuItemInvisibility>> queryModel =
                from(select(WebMenuItemInvisibility.class).where().prop("owner").eq().val(userProvider.getUser().isBase() ? userProvider.getUser()
                        : userProvider.getUser().getBasedOnUser()).or().prop("owner").eq().val(userProvider.getUser()).model()).
                        with(EntityUtils.fetchNotInstrumentedWithKeyAndDesc(WebMenuItemInvisibility.class).fetchModel()).model();
        final List<WebMenuItemInvisibility> invisibleItems = miInvisible.getAllEntities(queryModel);
        //Remove all retrieved menu items from the menu entity.
        for (final WebMenuItemInvisibility menuItem : invisibleItems) {
            final List<String> menuParts = decodeParts(menuItem.getMenuItemUri().split("/"));
            removeMenuItem(Arrays.asList(menu), menuParts);
        }
        return menu;
    }

    private void removeMenuItem(final List<IMenuManager> menuPath, final List<String> menuParts) {
        if (menuParts.size() > 1) {
            final IMenuManager lastItem = menuPath.get(menuPath.size() - 1);
            final Optional<IMenuManager> nextItem = lastItem.getMenuItem(menuParts.get(0));
            nextItem.ifPresent(i -> {
                final ArrayList<IMenuManager> newMenuPath = new ArrayList<>(menuPath);
                newMenuPath.add(i);
                removeMenuItem(newMenuPath, menuParts.subList(1, menuParts.size()));
            });
        }
        if (menuPath.size() > 2 && menuParts.size() > 0) {
            final IMenuManager lastMenuItem = menuPath.get(menuPath.size() - 1);
            if (menuParts.size() == 1) {
                computeVisibility(menuParts.get(0)).accept(lastMenuItem);
            }
            if (!isMenuVisible(lastMenuItem)) {
                final IMenuManager beforeLastMenuItem = menuPath.get(menuPath.size() - 2);
                computeVisibility(lastMenuItem.getTitle()).accept(beforeLastMenuItem);
            }
        }
    }

    private Consumer<IMenuManager> computeVisibility(final String lastMenuPart) {
        return (menuItem) -> {if (userProvider.getUser().isBase()) {
            menuItem.makeMenuItemInvisible(lastMenuPart);
        } else {
            menuItem.removeMenuItem(lastMenuPart);
        }};
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
