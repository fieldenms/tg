package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.sample.domain.mixin.TgSRStatusActivationFunctionalEntityMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO implementation for master object {@link ITgSRStatusActivationFunctionalEntity} based on a common with DAO mixin.
 *
 * @author Developers
 *
 */
@EntityType(TgSRStatusActivationFunctionalEntity.class)
public class TgSRStatusActivationFunctionalEntityRao extends CommonEntityRao<TgSRStatusActivationFunctionalEntity> implements ITgSRStatusActivationFunctionalEntity {

    private final TgSRStatusActivationFunctionalEntityMixin mixin;

    @Inject
    public TgSRStatusActivationFunctionalEntityRao(final RestClientUtil restUtil) {
        super(restUtil);

        mixin = new TgSRStatusActivationFunctionalEntityMixin(this);
    }

}