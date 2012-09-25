package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.sample.domain.ITgBogieClass;
import ua.com.fielden.platform.sample.domain.TgBogieClass;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


@EntityType(TgBogieClass.class)
public class TgBogieClassRao extends CommonEntityRao<TgBogieClass> implements ITgBogieClass {

    @Inject
    public TgBogieClassRao(final RestClientUtil restUtil) {
	super(restUtil);
    }
}
