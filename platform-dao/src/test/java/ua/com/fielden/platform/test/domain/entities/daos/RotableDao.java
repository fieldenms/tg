package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.Rotable;

import com.google.inject.Inject;

/**
 * Class for retrieval of rotables
 *
 * @author TG Team
 */
@EntityType(Rotable.class)
public class RotableDao extends CommonEntityDao<Rotable> implements IRotableDao {

    @Inject
    protected RotableDao(final IFilter filter) {
	super(filter);
    }


}
