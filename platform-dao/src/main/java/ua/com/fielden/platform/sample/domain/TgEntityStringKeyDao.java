package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgEntityStringKey}.
 *
 * @author TG Team
 *
 */
@EntityType(TgEntityStringKey.class)
public class TgEntityStringKeyDao extends CommonEntityDao<TgEntityStringKey> implements ITgEntityStringKey {

    @Inject
    public TgEntityStringKeyDao(final IFilter filter) {
        super(filter);
    }

}