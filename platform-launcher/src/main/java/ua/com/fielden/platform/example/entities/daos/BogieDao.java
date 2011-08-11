package ua.com.fielden.platform.example.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.Bogie;
import ua.com.fielden.platform.example.entities.IBogieDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

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
