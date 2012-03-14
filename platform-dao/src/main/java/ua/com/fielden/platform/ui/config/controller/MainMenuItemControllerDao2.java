package ua.com.fielden.platform.ui.config.controller;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController2;

import com.google.inject.Inject;

/**
 * DAO implementation of {@link IMainMenuItemController}.
 *
 * @author TG Team
 *
 */
@EntityType(MainMenuItem.class)
public class MainMenuItemControllerDao2 extends CommonEntityDao2<MainMenuItem> implements IMainMenuItemController2 {

    @Inject
    protected MainMenuItemControllerDao2(final IFilter filter) {
	super(filter);
    }

    @Override
    public void delete(final MainMenuItem entity) {
        defaultDelete(entity);
    }

}
