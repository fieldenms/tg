package ua.com.fielden.platform.ui.config.controller.mixin;

import java.util.List;

import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuStructureBuilder;

import com.google.inject.Inject;

/**
 * Implementation of the {@link IMainMenuStructureBuilder} contract, which should be used for loading of the menu structure from a persisted form such as the database or web resource.
 * The appropriate implementation of the {@link IMainMenuItemController} governs the source where menu structure is read.
 *
 * @author TG Team
 *
 */
public final class PersistedMainMenuStructureBuilder implements IMainMenuStructureBuilder {

    private final MainMenuItemMixin mixin;
    private final IMainMenuItemController mmiController;

    @Inject
    public PersistedMainMenuStructureBuilder(final IMainMenuItemController mmiController, final IEntityCentreConfigController eccController) {
	mixin = new MainMenuItemMixin(mmiController, eccController);
	this.mmiController = mmiController;
    }

    @Override
    public List<MainMenuItem> build(final String username) {
	mmiController.setUsername(username);
	mixin.setUser(mmiController.getUser());
	return mixin.loadMenuSkeletonStructure();
    }

}
