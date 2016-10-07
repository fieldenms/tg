package ua.com.fielden.platform.menu;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibility;
import ua.com.fielden.platform.utils.EntityUtils;

import com.google.inject.Inject;

public class MenuProducer implements IEntityProducer<Menu> {

    private final IMenuRetriever menuRetirever;
    private final IUserProvider userProvider;
    private final IMainMenuItemInvisibility miInvisible;

    @Inject
    public MenuProducer(final IMenuRetriever menuRetirever, final IMainMenuItemInvisibility miInvisible, final IUserProvider userProvider) {
        this.menuRetirever = menuRetirever;
        this.miInvisible = miInvisible;
        this.userProvider = userProvider;
    }

    @Override
    public Menu newEntity() {
        final Menu menu = menuRetirever.getMenuEntity().setCanEdit(userProvider.getUser().isBase());
        //Get all invisible menu items
        final QueryExecutionModel<MainMenuItemInvisibility, EntityResultQueryModel<MainMenuItemInvisibility>> queryModel =
                from(select(MainMenuItemInvisibility.class).where().prop("owner").eq().val(userProvider.getUser().isBase() ? userProvider.getUser()
                        : userProvider.getUser().getBasedOnUser()).model()).
                with(EntityUtils.fetchNotInstrumentedWithKeyAndDesc(MainMenuItemInvisibility.class).fetchModel()).model();
        final List<MainMenuItemInvisibility> invisibleItems = miInvisible.getAllEntities(queryModel);
        //Remove all retrieved menu items from the menu entity.
        for (final MainMenuItemInvisibility menuItem : invisibleItems) {
            final List<String> menuParts = decodeParts(menuItem.getMenuItemUri().split("/"));
            final String lastMenuPart = menuParts.remove(menuParts.size() - 1);
            Optional<? extends IMenuManager> menuManager = Optional.of(menu);
            menuManager = menuParts.stream().reduce(menuManager,
                    (menuItemManager, menuPart) -> menuItemManager.flatMap(value -> value.getMenuItem(menuPart)),
                    (menuManager1, menuManager2) -> menuManager2);
            menuManager.ifPresent(value -> {
                if (userProvider.getUser().isBase()) {
                    value.makeMenuItemInvisible(lastMenuPart);
                } else {
                    value.removeMenuItem(lastMenuPart);
                }
            });
        }
        return menu;
    }

    private List<String> decodeParts(final String[] menuParts) {
        final List<String> decodedParts = new ArrayList<>();
        try {
            for (int partIndex = 0; partIndex < menuParts.length; partIndex++) {
                decodedParts.add(URLDecoder.decode(menuParts[partIndex], "UTF-8"));
            }
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedParts;
    }
}
