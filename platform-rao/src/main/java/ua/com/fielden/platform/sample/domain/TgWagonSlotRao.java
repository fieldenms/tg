package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.sample.domain.ITgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgWagonSlot.class)
public class TgWagonSlotRao extends CommonEntityRao<TgWagonSlot> implements ITgWagonSlot {

    @Inject
    public TgWagonSlotRao(final RestClientUtil restUtil) {
        super(restUtil);
    }
}
