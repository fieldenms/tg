package ua.com.fielden.platform.sample.domain;

import java.util.Collection;
import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.continuation.NeedMoreData;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * DAO implementation for companion object {@link ITgDeletionTestEntity}.
 *
 * @author TG Team
 */
@EntityType(TgDeletionTestEntity.class)
public class TgDeletionTestEntityDao extends CommonEntityDao<TgDeletionTestEntity> implements ITgDeletionTestEntity {

    @Override
    @SessionRequired
    public TgDeletionTestEntity save(final TgDeletionTestEntity entity) {
        if (entity.isPersisted() && !moreData("newDeleteEntity").isPresent()) {
            throw new NeedMoreData("Need to specify number from 1 to 10", MoreDataForDeleteEntity.class,
                                   "newDeleteEntity");
        }
        return super.save(entity);
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

    @Override
    protected IFetchProvider<TgDeletionTestEntity> createFetchProvider() {
        return super.createFetchProvider().with("key", "desc");
    }

}
