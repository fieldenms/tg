package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.sample.domain.mixin.TgISStatusActivationFunctionalEntityMixin;
import com.google.inject.Inject;

/** 
 * RAO implementation for master object {@link ITgISStatusActivationFunctionalEntity} based on a common with DAO mixin.
 * 
 * @author Developers
 *
 */
@EntityType(TgISStatusActivationFunctionalEntity.class)
public class TgISStatusActivationFunctionalEntityRao extends CommonEntityRao<TgISStatusActivationFunctionalEntity> implements ITgISStatusActivationFunctionalEntity {

    
    private final TgISStatusActivationFunctionalEntityMixin mixin;
    
    @Inject
    public TgISStatusActivationFunctionalEntityRao(final RestClientUtil restUtil) {
        super(restUtil);
        
        mixin = new TgISStatusActivationFunctionalEntityMixin(this);
    }
    
}