package ua.com.fielden.platform.example.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.IWagonClassDao;
import ua.com.fielden.platform.example.entities.WagonClass;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * Class for retrieval of wagon eq.classes
*
* @author TG Team
*/
@EntityType(WagonClass.class)
public class WagonClassDao extends CommonEntityDao<WagonClass> implements IWagonClassDao {

    @Inject
    protected WagonClassDao(final IFilter filter) {
	super(filter);
    }
}

