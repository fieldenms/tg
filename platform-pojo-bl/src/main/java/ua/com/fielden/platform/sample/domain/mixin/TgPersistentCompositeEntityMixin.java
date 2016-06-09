package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.sample.domain.ITgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentCompositeEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

/**
 * Mixin implementation for companion object {@link ITgPersistentCompositeEntity}.
 *
 * @author Developers
 *
 */
public class TgPersistentCompositeEntityMixin {

    private final ITgPersistentCompositeEntity companion;

    public TgPersistentCompositeEntityMixin(final ITgPersistentCompositeEntity companion) {
        this.companion = companion;
    }

    public IPage<TgPersistentCompositeEntity> findDetails(final TgPersistentEntityWithProperties masterEntity, final fetch<TgPersistentCompositeEntity> fetch, final int pageCapacity) {
        final EntityResultQueryModel<TgPersistentCompositeEntity> selectModel = select(TgPersistentCompositeEntity.class).where().prop("key1").eq().val(masterEntity).model();
        return companion.firstPage(from(selectModel).with(orderBy().prop("key1").asc().prop("key2").desc().model()).with(fetch).model(), pageCapacity);
    }

    public TgPersistentCompositeEntity saveDetails(final TgPersistentEntityWithProperties masterEntity, final TgPersistentCompositeEntity detailEntity) {
        return companion.save(detailEntity);
    }

    public void deleteDetails(final TgPersistentEntityWithProperties masterEntity, final TgPersistentCompositeEntity detailEntity) {
        throw new UnsupportedOperationException("Entity deletion is unsupported by default.");
    }

}