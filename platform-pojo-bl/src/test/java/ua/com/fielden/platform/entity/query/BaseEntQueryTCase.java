package ua.com.fielden.platform.entity.query;

import java.util.Map;

import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.builders.DbVersion;
import ua.com.fielden.platform.entity.query.model.builders.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonOperator;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonTestModel;
import ua.com.fielden.platform.entity.query.model.elements.EntProp;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.elements.EntValue;

public class BaseEntQueryTCase {
    protected final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2);

    protected EntQuery entQuery(final QueryModel qryModel) {
	return qb.generateEntQuery(qryModel);
    }

    protected EntQuery entQuery(final QueryModel qryModel, final Map<String, Object> paramValues) {
	return qb.generateEntQuery(qryModel, paramValues);
    }

    protected EntProp prop(final String propName) {
	return new EntProp(propName);
    }

    protected final ComparisonTestModel alwaysTrueCondition = new ComparisonTestModel(new EntValue(0), ComparisonOperator.EQ, new EntValue(0));
}
