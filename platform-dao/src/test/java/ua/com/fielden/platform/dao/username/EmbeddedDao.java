package ua.com.fielden.platform.dao.username;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
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
public class EmbeddedDao extends CommonEntityDao<EntityWithMoney> {

    private TopLevelDao dao;

    @Inject
    public EmbeddedDao(final IFilter filter) {
	super(filter);
    }

    public void setDao(final TopLevelDao dao) {
        this.dao = dao;
    }

    public TopLevelDao getDao() {
        return dao;
    }

}
