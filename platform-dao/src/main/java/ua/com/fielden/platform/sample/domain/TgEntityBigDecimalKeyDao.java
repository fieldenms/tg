package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgEntityBigDecimalKey}.
 *
 * @author TG Team
 *
 */
@EntityType(TgEntityBigDecimalKey.class)
public class TgEntityBigDecimalKeyDao extends CommonEntityDao<TgEntityBigDecimalKey> implements ITgEntityBigDecimalKey {

    @Inject
    public TgEntityBigDecimalKeyDao(final IFilter filter) {
        super(filter);
    }

}