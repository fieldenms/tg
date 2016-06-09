package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.sample.domain.mixin.TgONStatusActivationFunctionalEntityMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO implementation for master object {@link ITgONStatusActivationFunctionalEntity} based on a common with DAO mixin.
 *
 * @author Developers
 *
 */
@EntityType(TgONStatusActivationFunctionalEntity.class)
public class TgONStatusActivationFunctionalEntityRao extends CommonEntityRao<TgONStatusActivationFunctionalEntity> implements ITgONStatusActivationFunctionalEntity {

    private final TgONStatusActivationFunctionalEntityMixin mixin;

    @Inject
    public TgONStatusActivationFunctionalEntityRao(final RestClientUtil restUtil) {
        super(restUtil);

        mixin = new TgONStatusActivationFunctionalEntityMixin(this);
    }

}