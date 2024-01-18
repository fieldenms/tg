package ua.com.fielden.platform.eql.execution;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.sample.domain.TeNamedValuesVector;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

public abstract class AbstractEqlExecutionTestCase extends AbstractDaoTestCase {

    protected final static String RESULT = "result";

    protected final IEntityAggregatesOperations aggregateDao = getInstance(IEntityAggregatesOperations.class);

    protected Object retrieveResult(final AggregatedResultQueryModel qry) {
        return aggregateDao.getEntity(from(qry).model()).get(RESULT);
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        save(new_(TeNamedValuesVector.class, "SINGLETON").setDateOf20010911(date("2001-09-11 00:00:00")));
    }
}
