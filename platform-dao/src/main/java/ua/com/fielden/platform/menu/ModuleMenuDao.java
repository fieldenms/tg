package ua.com.fielden.platform.menu;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;

/** 
 * DAO implementation for companion object {@link ModuleMenuCo}.
 * 
 * @author TG Team
 *
 */
@EntityType(ModuleMenu.class)
public class ModuleMenuDao extends CommonEntityDao<ModuleMenu> implements ModuleMenuCo {

    @Inject
    public ModuleMenuDao(final IFilter filter) {
        super(filter);
    }

}