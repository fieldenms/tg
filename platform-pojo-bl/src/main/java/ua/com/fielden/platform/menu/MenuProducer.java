package ua.com.fielden.platform.menu;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.EntityUtils;

public class MenuProducer implements IEntityProducer<Menu> {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final IMenuRetriever menuRetirever;
    private final IUserProvider userProvider;
    private final IWebMenuItemInvisibility miInvisible;

    @Inject
    public MenuProducer(final IMenuRetriever menuRetirever, final IWebMenuItemInvisibility miInvisible, final IUserProvider userProvider) {
        this.menuRetirever = menuRetirever;
        this.miInvisible = miInvisible;
        this.userProvider = userProvider;
    }

    @Override
    public Menu newEntity() {
        final Menu menu = menuRetirever.getMenuEntity().setCanEdit(userProvider.getUser().isBase());
        //Get all invisible menu items
        final QueryExecutionModel<WebMenuItemInvisibility, EntityResultQueryModel<WebMenuItemInvisibility>> queryModel =
                from(select(WebMenuItemInvisibility.class).where().prop("owner").eq().val(userProvider.getUser().isBase() ? userProvider.getUser()
                        : userProvider.getUser().getBasedOnUser()).model()).
                        with(EntityUtils.fetchNotInstrumentedWithKeyAndDesc(WebMenuItemInvisibility.class).fetchModel()).model();
        final List<WebMenuItemInvisibility> invisibleItems = miInvisible.getAllEntities(queryModel);
        //Remove all retrieved menu items from the menu entity.
        for (final WebMenuItemInvisibility menuItem : invisibleItems) {
            final List<String> menuParts = decodeParts(menuItem.getMenuItemUri().split("/"));
            final String lastMenuPart = menuParts.remove(menuParts.size() - 1);
            menuParts.stream()
            .reduce(Optional.of(menu), MenuProducer::accumulator, MenuProducer::combiner)
            .ifPresent(computeVisibility(userProvider, lastMenuPart));
        }
        return menu;
    }

    private static Optional<IMenuManager> accumulator(final Optional<IMenuManager> menuItemManager, final String menuPart) {
        return menuItemManager.flatMap(value -> value.getMenuItem(menuPart));
    }

    private static Optional<IMenuManager> combiner(final Optional<IMenuManager> menuItemManager1, final Optional<IMenuManager> menuItemManager2) {
        return menuItemManager2;
    }

    private Consumer<IMenuManager> computeVisibility(final IUserProvider up, final String lastMenuPart) {
        return (menuItem) -> {if (up.getUser().isBase()) {
            menuItem.makeMenuItemInvisible(lastMenuPart);
        } else {
            menuItem.removeMenuItem(lastMenuPart);
        }};
    }

    private List<String> decodeParts(final String[] menuParts) {
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
