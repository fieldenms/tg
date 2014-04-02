package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgWagonSlot.class)
public class TgWagonSlotDao extends CommonEntityDao<TgWagonSlot> implements ITgWagonSlot {

    @Inject
    protected TgWagonSlotDao(final IFilter filter) {
        super(filter);
    }
}