package ua.com.fielden.platform.ui.config.controller;

import java.util.List;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfigController;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemController;
import ua.com.fielden.platform.ui.config.controller.mixin.MainMenuItemMixin;

import com.google.inject.Inject;

/**
 * DAO implementation of {@link IMainMenuItemController}.
 * 
 * @author TG Team
 * 
 */
@EntityType(MainMenuItem.class)
public class MainMenuItemControllerDao extends CommonEntityDao<MainMenuItem> implements IMainMenuItemController {

    private final MainMenuItemMixin mixin;
    private final IUserDao userDao;

    @Inject
    protected MainMenuItemControllerDao(final IFilter filter, final IEntityCentreConfigController eccController, final IUserDao userDao) {
	super(filter);
	this.userDao = userDao;
	mixin = new MainMenuItemMixin(this, eccController);
    }

    @Override
    public List<MainMenuItem> loadMenuSkeletonStructure() {
	mixin.setUser(userDao.findByKey(getUsername()));
	return mixin.loadMenuSkeletonStructure();
    }

}
