package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.Wheelset;

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
