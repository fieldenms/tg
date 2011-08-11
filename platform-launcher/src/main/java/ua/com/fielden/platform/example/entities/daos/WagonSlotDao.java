package ua.com.fielden.platform.example.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.WagonSlot;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO for {@link WagonSlot}s retrieval
 *
 * @author TG Team
 */
@EntityType(WagonSlot.class)
public class WagonSlotDao extends CommonEntityDao<WagonSlot> {

    @Inject
    protected WagonSlotDao(final IFilter filter) {
	super(filter);
    }
}
