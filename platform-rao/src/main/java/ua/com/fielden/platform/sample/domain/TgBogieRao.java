package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.sample.domain.ITgBogie;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


@EntityType(TgBogie.class)
public class TgBogieRao extends CommonEntityRao<TgBogie> implements ITgBogie {

    @Inject
    public TgBogieRao(final RestClientUtil restUtil) {
	super(restUtil);
    }
}
