package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgEntityLongKey}.
 *
 * @author TG Team
 *
 */
@EntityType(TgEntityLongKey.class)
public class TgEntityLongKeyDao extends CommonEntityDao<TgEntityLongKey> implements ITgEntityLongKey {

    @Inject
    public TgEntityLongKeyDao(final IFilter filter) {
        super(filter);
    }

}