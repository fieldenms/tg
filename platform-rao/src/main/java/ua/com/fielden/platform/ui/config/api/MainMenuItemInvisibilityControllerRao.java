package ua.com.fielden.platform.ui.config.api;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.rao.WebResourceType;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;

import com.google.inject.Inject;

/**
 * RAO implementation of {@link IMainMenuItemInvisibilityController}.
 * 
 * @author TG Team
 * 
 */
@EntityType(MainMenuItemInvisibility.class)
public class MainMenuItemInvisibilityControllerRao extends CommonEntityRao<MainMenuItemInvisibility> implements IMainMenuItemInvisibilityController {

    private final EntityFactory factory;

    @Inject
    public MainMenuItemInvisibilityControllerRao(final RestClientUtil restUtil, final EntityFactory factory) {
        super(restUtil);
        this.factory = factory;
    }

    protected WebResourceType getDefaultWebResourceType() {
        return WebResourceType.SYSTEM;
    }

    @Override
    public void makeInvisible(final MainMenuItem menuItem, final User user) {
        final MainMenuItemInvisibility invisibility = factory.newByKey(MainMenuItemInvisibility.class, user, menuItem);
        save(invisibility);
    }

    @Override
    public void makeVisible(final MainMenuItem menuItem, final User user) {
        final EntityResultQueryModel<MainMenuItemInvisibility> model = select(MainMenuItemInvisibility.class).where()//
        .prop("owner").eq().val(user).and()//
        .prop("menuItem").eq().val(menuItem).model();

        delete(model);
    }
}
