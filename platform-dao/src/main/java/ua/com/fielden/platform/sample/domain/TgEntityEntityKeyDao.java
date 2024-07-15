package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgEntityEntityKey}.
 *
 * @author TG Team
 *
 */
@EntityType(TgEntityEntityKey.class)
public class TgEntityEntityKeyDao extends CommonEntityDao<TgEntityEntityKey> implements ITgEntityEntityKey {

    @Inject
    public TgEntityEntityKeyDao(final IFilter filter) {
        super(filter);
    }

}