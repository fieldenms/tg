package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.WagonSlot;

import com.google.inject.Inject;

/**
 * DAO for retrieving wagon related data: wagon itself, wagon with its rotables.
 *
 * @author TG Team
 *
 */
@EntityType(WagonSlot.class)
public class WagonSlotDao2 extends CommonEntityDao2<WagonSlot> implements IWagonSlotDao2 {

    @Inject
    protected WagonSlotDao2(final IFilter filter) {
	super(filter);
    }

}
