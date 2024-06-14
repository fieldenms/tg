package ua.com.fielden.platform.menu;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;
/** 
 * DAO implementation for companion object {@link ICustomView}.
 * 
 * @author Developers
 *
 */
@EntityType(CustomView.class)
public class CustomViewDao extends CommonEntityDao<CustomView> implements ICustomView {

    @Inject
    public CustomViewDao(final IFilter filter) {
        super(filter);
    }

}