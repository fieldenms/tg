package ua.com.fielden.platform.ui.config.api;

import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.ui.config.MainMenuItem;

/**
 * A DAO/RAO controller contract for {@link MainMenuItem}.
 * 
 * @author TG Team
 * 
 */
public interface IMainMenuItemController extends IEntityDao<MainMenuItem> {

    /** This method should be used to load the full main menu hierarchical structure without corresponding entity centre configurations */
    List<MainMenuItem> loadMenuSkeletonStructure();

}
