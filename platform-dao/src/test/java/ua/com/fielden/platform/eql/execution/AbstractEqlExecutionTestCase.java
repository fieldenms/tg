package ua.com.fielden.platform.eql.execution;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.sample.domain.TeNamedValuesVector;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.Map;

/**
 * Should be used as a convenient base class for EQL database interaction test cases.
 *
 * @author TG Team
 *
 */
public abstract class AbstractEqlExecutionTestCase extends AbstractDaoTestCase {

    protected final static String RESULT = "result";

    protected final IEntityAggregatesOperations aggregateDao = getInstance(IEntityAggregatesOperations.class);

    protected Object retrieveResult(final AggregatedResultQueryModel qry) {
        return aggregateDao.getEntity(from(qry).model()).get(RESULT);
    }

    protected Object retrieveResult(final AggregatedResultQueryModel qry, final Map<String, Object> params) {
        return aggregateDao.getEntity(from(qry).with(params).model()).get(RESULT);
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();
        save(new_(TeNamedValuesVector.class, "SINGLETON").
                setDateOf20010911(date("2001-09-11 00:00:00")).
                setDateAndTimeOf20010911084640(date("2001-09-11 08:46:40")).
                setStringOfSadEvent("sad event").
                setUppercasedStringOfAbc("ABC").
                setLowercasedStringOfAbc("abc").
                setIntegerOfZero(0)
                );
    }
}
