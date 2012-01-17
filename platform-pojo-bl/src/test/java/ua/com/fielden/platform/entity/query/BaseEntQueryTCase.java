package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.query.model.builders.DbVersion;
import ua.com.fielden.platform.entity.query.model.builders.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonOperator;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonTestModel;
import ua.com.fielden.platform.entity.query.model.elements.EntProp;
import ua.com.fielden.platform.entity.query.model.elements.EntValue;

public class BaseEntQueryTCase {
    protected final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2);

    protected EntProp prop(final String propName) {
	return new EntProp(propName);
    }

    protected final ComparisonTestModel alwaysTrueCondition = new ComparisonTestModel(new EntValue(0), ComparisonOperator.EQ, new EntValue(0));
}
