package ua.com.fielden.platform.dao.username;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * A DAO for {@link EntityWithMoney} used for testing.
 *
 * @author TG Team
 *
 */
@EntityType(EntityWithMoney.class)
public class TopLevelDao extends CommonEntityDao<EntityWithMoney> {

    private final EmbeddedDao dao;

    public EmbeddedDao getDao() {
        return dao;
    }

    @Inject
    public TopLevelDao(final EmbeddedDao dao, final IFilter filter) {
	super(filter);
	this.dao = dao;
    }

}
