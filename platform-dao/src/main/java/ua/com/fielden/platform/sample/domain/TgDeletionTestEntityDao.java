package ua.com.fielden.platform.sample.domain;

import java.util.Collection;
import java.util.Map;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgDeletionTestEntity}.
 *
 * @author Developers
 *
 */
@EntityType(TgDeletionTestEntity.class)
public class TgDeletionTestEntityDao extends CommonEntityDao<TgDeletionTestEntity> implements ITgDeletionTestEntity {
    @Inject
    public TgDeletionTestEntityDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    @SessionRequired
    public void delete(final TgDeletionTestEntity entity) {
        defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgDeletionTestEntity> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }
}