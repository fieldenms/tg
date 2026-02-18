package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/// DAO implementation for companion object [CanLeaveOptionsCo].
///
@EntityType(CanLeaveOptions.class)
public class CanLeaveOptionsDao extends CommonEntityDao<CanLeaveOptions> implements CanLeaveOptionsCo {

    @Inject
    public CanLeaveOptionsDao(final IFilter filter) {
        super(filter);
    }

}
