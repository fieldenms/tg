package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.Bogie;

import com.google.inject.Inject;

/**
 * Class for retrieval bogies
 *
 * @author TG Team
 */
@EntityType(Bogie.class)
public class BogieDao extends CommonEntityDao<Bogie> implements IBogieDao {

    @Inject
    protected BogieDao(final IFilter filter) {
	super(filter);
    }


}
