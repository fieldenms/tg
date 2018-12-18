package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.mixin.TgFetchProviderTestEntityMixin;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link ITgFetchProviderTestEntity}.
 * 
 * @author Developers
 *
 */
@EntityType(TgFetchProviderTestEntity.class)
public class TgFetchProviderTestEntityDao extends CommonEntityDao<TgFetchProviderTestEntity> implements ITgFetchProviderTestEntity {
    
    private final TgFetchProviderTestEntityMixin mixin;
    
    @Inject
    public TgFetchProviderTestEntityDao(final IFilter filter) {
        super(filter);
        
        mixin = new TgFetchProviderTestEntityMixin(this);
    }
    
    @Override
    protected IFetchProvider<TgFetchProviderTestEntity> createFetchProvider() {
        return super.createFetchProvider().with("propForValidation");
    }
    
}