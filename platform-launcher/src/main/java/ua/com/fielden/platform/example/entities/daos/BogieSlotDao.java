package ua.com.fielden.platform.example.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.BogieSlot;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * Class for bogie slot retrieval
 *
 * @author TG Team
 */
@EntityType(BogieSlot.class)
public class BogieSlotDao extends CommonEntityDao<BogieSlot> {

    @Inject
    protected BogieSlotDao(final IFilter filter) {
	super(filter);
    }
}
