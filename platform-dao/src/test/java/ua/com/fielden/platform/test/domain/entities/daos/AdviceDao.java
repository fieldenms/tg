package ua.com.fielden.platform.test.domain.entities.daos;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.domain.entities.Advice;

import com.google.inject.Inject;

/**
 * DAO for {@link Advice} retrieval.
 *
 * @author TG Team
 */

@EntityType(Advice.class)
public class AdviceDao extends CommonEntityDao<Advice> implements IAdviceDao {

    @Inject
    protected AdviceDao(final IFilter filter) {
	super(filter);
    }
}
