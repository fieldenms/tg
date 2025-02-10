package ua.com.fielden.platform.sample.domain.composite;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgRollingStockMajorComponentCo}.
 *
 * @author TG Team
 *
 */
@EntityType(TgRollingStockMajorComponent.class)
public class TgRollingStockMajorComponentDao extends CommonEntityDao<TgRollingStockMajorComponent> implements TgRollingStockMajorComponentCo {

    @Inject
    public TgRollingStockMajorComponentDao(final IFilter filter) {
        super(filter);
    }

}