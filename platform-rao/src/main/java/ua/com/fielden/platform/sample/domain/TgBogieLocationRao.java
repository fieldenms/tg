package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.sample.domain.ITgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


@EntityType(TgBogieLocation.class)
public class TgBogieLocationRao extends CommonEntityRao<TgBogieLocation> implements ITgBogieLocation {

    @Inject
    public TgBogieLocationRao(final RestClientUtil restUtil) {
	super(restUtil);
    }


}
