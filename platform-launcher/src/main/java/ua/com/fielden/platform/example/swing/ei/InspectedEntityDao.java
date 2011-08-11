package ua.com.fielden.platform.example.swing.ei;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;


/**
 * DAO for entity inspector example.
 *
 * @author TG Team
 *
 */
@EntityType(InspectedEntity.class)
public class InspectedEntityDao extends CommonEntityDao<InspectedEntity> {

    @Inject
    protected InspectedEntityDao(final IFilter filter) {
	super(filter);
    }
}
