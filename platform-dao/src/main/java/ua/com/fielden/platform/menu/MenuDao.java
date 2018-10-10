package ua.com.fielden.platform.menu;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;
/** 
 * DAO implementation for companion object {@link IMenu}.
 * 
 * @author Developers
 *
 */
@EntityType(Menu.class)
public class MenuDao extends CommonEntityDao<Menu> implements IMenu {

    @Inject
    public MenuDao(final IFilter filter) {
        super(filter);
    }

}