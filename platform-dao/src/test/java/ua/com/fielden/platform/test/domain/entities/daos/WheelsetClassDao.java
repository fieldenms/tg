package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.WheelsetClass;

import com.google.inject.Inject;

/**
 * Class for retrieval of wheelset eq.classes
*
* @author TG Team
*/
@EntityType(WheelsetClass.class)
public class WheelsetClassDao extends CommonEntityDao<WheelsetClass> implements IWheelsetClassDao {

    @Inject
    protected WheelsetClassDao(final IFilter filter) {
	super(filter);
    }


}

