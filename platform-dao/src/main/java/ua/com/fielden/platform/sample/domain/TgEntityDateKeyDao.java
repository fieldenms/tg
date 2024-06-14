package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgEntityDateKey}.
 *
 * @author TG Team
 *
 */
@EntityType(TgEntityDateKey.class)
public class TgEntityDateKeyDao extends CommonEntityDao<TgEntityDateKey> implements ITgEntityDateKey {

    @Inject
    public TgEntityDateKeyDao(final IFilter filter) {
        super(filter);
    }

}