package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.sample.domain.mixin.TgAuthorRoyaltyMixin;
import com.google.inject.Inject;

/** 
 * RAO implementation for companion object {@link ITgAuthorRoyalty} based on a common with DAO mixin.
 * 
 * @author Developers
 *
 */
@EntityType(TgAuthorRoyalty.class)
public class TgAuthorRoyaltyRao extends CommonEntityRao<TgAuthorRoyalty> implements ITgAuthorRoyalty {

    
    private final TgAuthorRoyaltyMixin mixin;
    
    @Inject
    public TgAuthorRoyaltyRao(final RestClientUtil restUtil) {
        super(restUtil);
        
        mixin = new TgAuthorRoyaltyMixin(this);
    }
    
    @Override
    public IPage<TgAuthorRoyalty> findDetails(final TgAuthorship masterEntity, final fetch<TgAuthorRoyalty> fetch, final int pageCapacity) {
        return mixin.findDetails(masterEntity, fetch, pageCapacity);
    }
    
    @Override
    public TgAuthorRoyalty saveDetails(final TgAuthorship masterEntity, final TgAuthorRoyalty detailEntity) {
        return mixin.saveDetails(masterEntity, detailEntity);
    }
    
    @Override
    public void deleteDetails(final TgAuthorship masterEntity, final TgAuthorRoyalty detailEntity) {
        mixin.deleteDetails(masterEntity, detailEntity);
    }
    
}