package ua.com.fielden.platform.ui.config.api;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.rao.WebResourceType;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.controller.mixin.MainMenuItemMixin;

import com.google.inject.Inject;

/**
 * RAO implementation of {@link IMainMenuItemController}.
 *
 * @author TG Team
 *
 */
@EntityType(MainMenuItem.class)
public class MainMenuItemControllerRao extends CommonEntityRao<MainMenuItem> implements IMainMenuItemController {

    private final MainMenuItemMixin mixin;

    @Inject
    public MainMenuItemControllerRao(final RestClientUtil restUtil, final IEntityCentreConfigController eccController) {
	super(restUtil);
	mixin = new MainMenuItemMixin(this, eccController);
    }

    protected WebResourceType getDefaultWebResourceType() {
	return WebResourceType.SYSTEM;
    }

//    @Override
//    public List<MainMenuItem> build() {
//	mixin.setUser(restUtil.getUser());
//	return mixin.loadMenuSkeletonStructure();
//    }

}
