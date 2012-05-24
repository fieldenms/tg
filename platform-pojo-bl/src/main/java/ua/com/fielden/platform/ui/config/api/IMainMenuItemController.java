package ua.com.fielden.platform.ui.config.api;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.ui.config.MainMenuItem;

/**
 * A controller contract for constructing the hierarchical structure of the main application menu and managing its persistence.
 *
 * @author TG Team
 *
 */
public interface IMainMenuItemController extends IEntityDao<MainMenuItem> {
}
