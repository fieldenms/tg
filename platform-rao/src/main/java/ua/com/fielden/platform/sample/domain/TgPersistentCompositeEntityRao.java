package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.sample.domain.mixin.TgPersistentCompositeEntityMixin;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link ITgPersistentCompositeEntity} based on a common with DAO mixin.
 * 
 * @author Developers
 *
 */
@EntityType(TgPersistentCompositeEntity.class)
public class TgPersistentCompositeEntityRao extends CommonEntityRao<TgPersistentCompositeEntity> implements ITgPersistentCompositeEntity {

    
    private final TgPersistentCompositeEntityMixin mixin;
    
    @Inject
    public TgPersistentCompositeEntityRao(final RestClientUtil restUtil) {
        super(restUtil);
        
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
    
}