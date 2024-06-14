package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgEntityCompositeKey}.
 *
 * @author TG Team
 *
 */
@EntityType(TgEntityCompositeKey.class)
public class TgEntityCompositeKeyDao extends CommonEntityDao<TgEntityCompositeKey> implements ITgEntityCompositeKey {

    @Inject
    public TgEntityCompositeKeyDao(final IFilter filter) {
        super(filter);
    }

}