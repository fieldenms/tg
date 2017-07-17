package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ICustomisableColumn}.
 * 
 * @author Developers
 *
 */
@EntityType(CustomisableColumn.class)
public class CustomisableColumnDao extends CommonEntityDao<CustomisableColumn> implements ICustomisableColumn {
    @Inject
    public CustomisableColumnDao(final IFilter filter) {
        super(filter);
    }

}