package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.sample.domain.ITgWorkshop;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


@EntityType(TgWorkshop.class)
public class TgWorkshopRao extends CommonEntityRao<TgWorkshop> implements ITgWorkshop {

    @Inject
    public TgWorkshopRao(final RestClientUtil restUtil) {
	super(restUtil);
    }
}
