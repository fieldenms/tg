package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.BogieClass;

import com.google.inject.Inject;

/**
 * Class for retrieval of bogie eq.classes
*
* @author TG Team
*/
@EntityType(BogieClass.class)
public class BogieClassDao extends CommonEntityDao<BogieClass> implements IBogieClassDao {

    @Inject
    protected BogieClassDao(final IFilter filter) {
	super(filter);
    }



}

