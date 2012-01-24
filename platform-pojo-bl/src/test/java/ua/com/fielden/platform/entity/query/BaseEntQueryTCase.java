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
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

public class BaseEntQueryTCase {
    protected static final Class<TgVehicle> VEHICLE = TgVehicle.class;
    protected static final Class<TgVehicleModel> MODEL = TgVehicleModel.class;
    protected static final Class<TgVehicleMake> MAKE = TgVehicleMake.class;
    protected static final Class<TgOrgUnit5> ORG5 = TgOrgUnit5.class;
    protected static final Class<TgOrgUnit4> ORG4 = TgOrgUnit4.class;
    protected static final Class<TgOrgUnit3> ORG3 = TgOrgUnit3.class;
    protected static final Class<TgOrgUnit2> ORG2 = TgOrgUnit2.class;
    protected static final Class<TgOrgUnit1> ORG1 = TgOrgUnit1.class;

    protected final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2);

    protected EntQuery entQuery1(final QueryModel qryModel) {
	return qb.generateEntQuery(qryModel);
    }

    protected EntQuery entQuery1(final QueryModel qryModel, final Map<String, Object> paramValues) {
	return qb.generateEntQuery(qryModel, paramValues);
    }

    protected EntQuery entValidQuery(final QueryModel qryModel) {
	final EntQuery result = qb.generateEntQuery(qryModel);
	result.validate();
	return result;
    }

    protected EntQuery entValidQuery(final QueryModel qryModel, final Map<String, Object> paramValues) {
	final EntQuery result = qb.generateEntQuery(qryModel, paramValues);
	result.validate();
	return result;
    }


    protected EntProp prop(final String propName) {
	return new EntProp(propName);
    }

    protected final ComparisonTestModel alwaysTrueCondition = new ComparisonTestModel(new EntValue(0), ComparisonOperator.EQ, new EntValue(0));
}
