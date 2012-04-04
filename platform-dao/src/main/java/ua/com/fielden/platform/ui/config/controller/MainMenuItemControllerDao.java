package ua.com.fielden.platform.ui.config.controller;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;

import com.google.inject.Inject;

/**
 * DAO implementation of {@link IMainMenuItemController}.
 *
 * @author TG Team
 *
 */
@EntityType(MainMenuItem.class)
public class MainMenuItemControllerDao extends CommonEntityDao<MainMenuItem> implements IMainMenuItemController {

    @Inject
    protected MainMenuItemControllerDao(final IFilter filter) {
	super(filter);
    }

    @Override
    public void delete(final MainMenuItem entity) {
        defaultDelete(entity);
    }

}
