package ua.com.fielden.platform.menu;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;
/** 
 * DAO implementation for companion object {@link IModule}.
 * 
 * @author Developers
 *
 */
@EntityType(Module.class)
public class ModuleDao extends CommonEntityDao<Module> implements IModule {

    @Inject
    public ModuleDao(final IFilter filter) {
        super(filter);
    }

}