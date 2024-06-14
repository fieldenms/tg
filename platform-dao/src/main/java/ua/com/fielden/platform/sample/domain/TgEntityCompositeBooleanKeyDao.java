package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgEntityCompositeBooleanKey}.
 *
 * @author TG Team
 *
 */
@EntityType(TgEntityCompositeBooleanKey.class)
public class TgEntityCompositeBooleanKeyDao extends CommonEntityDao<TgEntityCompositeBooleanKey> implements ITgEntityCompositeBooleanKey {

    @Inject
    public TgEntityCompositeBooleanKeyDao(final IFilter filter) {
        super(filter);
    }

}