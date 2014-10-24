package ua.com.fielden.platform.entity.functional;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.functional.centre.CritProp;
import ua.com.fielden.platform.entity.functional.centre.ICritProp;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ICritProp}.
 *
 * @author Developers
 *
 */
@EntityType(CritProp.class)
public class CritPropDao extends CommonEntityDao<CritProp> implements ICritProp {
    @Inject
    public CritPropDao(final IFilter filter) {
        super(filter);
    }

}