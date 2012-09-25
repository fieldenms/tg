package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.sample.domain.ITgWagonClassCompatibility;
import ua.com.fielden.platform.sample.domain.TgWagonClassCompatibility;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


@EntityType(TgWagonClassCompatibility.class)
public class TgWagonClassCompatibilityRao extends CommonEntityRao<TgWagonClassCompatibility> implements ITgWagonClassCompatibility {

    @Inject
    public TgWagonClassCompatibilityRao(final RestClientUtil restUtil) {
	super(restUtil);
    }
}
