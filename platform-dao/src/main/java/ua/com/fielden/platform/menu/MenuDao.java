package ua.com.fielden.platform.menu;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;

/** 
 * DAO implementation for companion object {@link MenuCo}.
 * 
 * @author TG Team
 *
 */
@EntityType(Menu.class)
public class MenuDao extends CommonEntityDao<Menu> implements MenuCo {

    @Inject
    public MenuDao(final IFilter filter) {
        super(filter);
    }

}