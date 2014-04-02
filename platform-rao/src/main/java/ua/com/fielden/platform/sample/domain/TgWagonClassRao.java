package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.sample.domain.ITgWagonClass;
import ua.com.fielden.platform.sample.domain.TgWagonClass;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgWagonClass.class)
public class TgWagonClassRao extends CommonEntityRao<TgWagonClass> implements ITgWagonClass {

    @Inject
    public TgWagonClassRao(final RestClientUtil restUtil) {
        super(restUtil);
    }
}
