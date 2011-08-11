package ua.com.fielden.platform.example.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.IWheelsetDao;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * Class for wheelset retrieval
 *
 * @author TG Team
 */
@EntityType(Wheelset.class)
public class WheelsetDao extends CommonEntityDao<Wheelset> implements IWheelsetDao {

    @Inject
    protected WheelsetDao(final IFilter filter) {
	super(filter);
    }
}
