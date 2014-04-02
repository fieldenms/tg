package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.sample.domain.ITgWagon;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgWagon.class)
public class TgWagonRao extends CommonEntityRao<TgWagon> implements ITgWagon {

    @Inject
    public TgWagonRao(final RestClientUtil restUtil) {
        super(restUtil);
    }
}
