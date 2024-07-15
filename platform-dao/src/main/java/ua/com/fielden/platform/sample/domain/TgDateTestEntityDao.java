package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgDateTestEntity}.
 *
 * @author TG Team
 *
 */
@EntityType(TgDateTestEntity.class)
public class TgDateTestEntityDao extends CommonEntityDao<TgDateTestEntity> implements ITgDateTestEntity {

    @Inject
    public TgDateTestEntityDao(final IFilter filter) {
        super(filter);
    }
}