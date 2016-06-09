package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.sample.domain.mixin.TgPersistentCompositeEntityMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgPersistentCompositeEntity} based on a common with DAO mixin.
 *
 * @author Developers
 *
 */
@EntityType(TgPersistentCompositeEntity.class)
public class TgPersistentCompositeEntityDao extends CommonEntityDao<TgPersistentCompositeEntity> implements ITgPersistentCompositeEntity {

    private final TgPersistentCompositeEntityMixin mixin;

    @Inject
    public TgPersistentCompositeEntityDao(final IFilter filter) {
        super(filter);

        mixin = new TgPersistentCompositeEntityMixin(this);
    }

    @Override
    public IPage<TgPersistentCompositeEntity> findDetails(final TgPersistentEntityWithProperties masterEntity, final fetch<TgPersistentCompositeEntity> fetch, final int pageCapacity) {
        return mixin.findDetails(masterEntity, fetch, pageCapacity);
    }

    @Override
    public TgPersistentCompositeEntity saveDetails(final TgPersistentEntityWithProperties masterEntity, final TgPersistentCompositeEntity detailEntity) {
        return mixin.saveDetails(masterEntity, detailEntity);
    }

    @Override
    public void deleteDetails(final TgPersistentEntityWithProperties masterEntity, final TgPersistentCompositeEntity detailEntity) {
        mixin.deleteDetails(masterEntity, detailEntity);
    }

    //    @Override
    //    public IFetchProvider<TgPersistentCompositeEntity> createFetchProvider() {
    //        return super.createFetchProvider()
    //                .with("key1")
    //                .with("key2");
    //    }
}