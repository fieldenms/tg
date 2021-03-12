package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgWebApiEntitySyntheticMulti}.
 *
 * @author TG Team
 *
 */
@EntityType(TgWebApiEntitySyntheticMulti.class)
public class TgWebApiEntitySyntheticMultiDao extends CommonEntityDao<TgWebApiEntitySyntheticMulti> implements ITgWebApiEntitySyntheticMulti {
    
    @Inject
    public TgWebApiEntitySyntheticMultiDao(final IFilter filter) {
        super(filter);
    }
    
}