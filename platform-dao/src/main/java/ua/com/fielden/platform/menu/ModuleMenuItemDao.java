package ua.com.fielden.platform.menu;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;
/** 
 * DAO implementation for companion object {@link IModuleMenuItem}.
 * 
 * @author Developers
 *
 */
@EntityType(ModuleMenuItem.class)
public class ModuleMenuItemDao extends CommonEntityDao<ModuleMenuItem> implements IModuleMenuItem {

    @Inject
    public ModuleMenuItemDao(final IFilter filter) {
        super(filter);
    }

}