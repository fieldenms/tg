package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.sample.domain.mixin.TgIRStatusActivationFunctionalEntityMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO implementation for master object {@link ITgIRStatusActivationFunctionalEntity} based on a common with DAO mixin.
 *
 * @author Developers
 *
 */
@EntityType(TgIRStatusActivationFunctionalEntity.class)
public class TgIRStatusActivationFunctionalEntityRao extends CommonEntityRao<TgIRStatusActivationFunctionalEntity> implements ITgIRStatusActivationFunctionalEntity {

    private final TgIRStatusActivationFunctionalEntityMixin mixin;

    @Inject
    public TgIRStatusActivationFunctionalEntityRao(final RestClientUtil restUtil) {
        super(restUtil);

        mixin = new TgIRStatusActivationFunctionalEntityMixin(this);
    }

}