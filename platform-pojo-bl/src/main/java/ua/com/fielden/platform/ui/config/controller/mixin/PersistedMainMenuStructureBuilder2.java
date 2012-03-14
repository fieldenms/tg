package ua.com.fielden.platform.ui.config.controller.mixin;

import java.util.List;

import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController2;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController2;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder2;

import com.google.inject.Inject;

/**
 * Implementation of the {@link IMainMenuStructureBuilder2} contract, which should be used for loading of the menu structure from a persisted form such as the database or web resource.
 * The appropriate implementation of the {@link IMainMenuItemController2} governs the source where menu structure is read.
 *
 * @author TG Team
 *
 */
public final class PersistedMainMenuStructureBuilder2 implements IMainMenuStructureBuilder2 {

    private final MainMenuItemMixin2 mixin;
    private final IMainMenuItemController2 mmiController;

    @Inject
    public PersistedMainMenuStructureBuilder2(final IMainMenuItemController2 mmiController, final IEntityCentreConfigController2 eccController) {
	mixin = new MainMenuItemMixin2(mmiController, eccController);
	this.mmiController = mmiController;
    }

    @Override
    public List<MainMenuItem> build(final String username) {
	mmiController.setUsername(username);
	mixin.setUser(mmiController.getUser());
	return mixin.loadMenuSkeletonStructure();
    }

}
