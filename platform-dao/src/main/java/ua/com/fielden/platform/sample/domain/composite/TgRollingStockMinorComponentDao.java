package ua.com.fielden.platform.sample.domain.composite;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgRollingStockMinorComponentCo}.
 *
 * @author TG Team
 *
 */
@EntityType(TgRollingStockMinorComponent.class)
public class TgRollingStockMinorComponentDao extends CommonEntityDao<TgRollingStockMinorComponent> implements TgRollingStockMinorComponentCo {

    @Inject
    public TgRollingStockMinorComponentDao(final IFilter filter) {
        super(filter);
    }

}