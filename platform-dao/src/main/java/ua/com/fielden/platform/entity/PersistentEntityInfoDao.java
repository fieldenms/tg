package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link PersistentEntityInfoCo}.
 *
 * @author TG Team
 */
@EntityType(PersistentEntityInfo.class)
public class PersistentEntityInfoDao extends CommonEntityDao<PersistentEntityInfo> implements PersistentEntityInfoCo {

    @Inject
    public PersistentEntityInfoDao(final IFilter filter) {
        super(filter);
    }
}
