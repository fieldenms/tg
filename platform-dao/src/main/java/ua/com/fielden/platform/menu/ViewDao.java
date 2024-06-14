package ua.com.fielden.platform.menu;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;
/** 
 * DAO implementation for companion object {@link IView}.
 * 
 * @author Developers
 *
 */
@EntityType(View.class)
public class ViewDao extends CommonEntityDao<View> implements IView {

    @Inject
    public ViewDao(final IFilter filter) {
        super(filter);
    }

}