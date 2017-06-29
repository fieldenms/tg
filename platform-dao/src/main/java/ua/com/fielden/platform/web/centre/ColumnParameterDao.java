package ua.com.fielden.platform.web.centre;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;
/** 
 * DAO implementation for companion object {@link IColumnParameter}.
 * 
 * @author Developers
 *
 */
@EntityType(ColumnParameter.class)
public class ColumnParameterDao extends CommonEntityDao<ColumnParameter> implements IColumnParameter {

    @Inject
    public ColumnParameterDao(final IFilter filter) {
        super(filter);
    }

}