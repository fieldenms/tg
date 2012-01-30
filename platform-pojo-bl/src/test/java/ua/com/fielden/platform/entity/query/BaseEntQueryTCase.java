package ua.com.fielden.platform.entity.query;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.builders.DbVersion;
import ua.com.fielden.platform.entity.query.model.builders.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.model.elements.AbstractEntQuerySource.PropResolutionInfo;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonOperator;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonTestModel;
import ua.com.fielden.platform.entity.query.model.elements.EntProp;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.elements.EntValue;
import ua.com.fielden.platform.entity.query.model.elements.IEntQuerySource;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;

public class BaseEntQueryTCase {
    protected static final Class<TgWorkOrder> WORK_ORDER = TgWorkOrder.class;
    protected static final Class<TgVehicle> VEHICLE = TgVehicle.class;
    protected static final Class<TgVehicleModel> MODEL = TgVehicleModel.class;
    protected static final Class<TgVehicleMake> MAKE = TgVehicleMake.class;
    protected static final Class<TgOrgUnit5> ORG5 = TgOrgUnit5.class;
    protected static final Class<TgOrgUnit4> ORG4 = TgOrgUnit4.class;
    protected static final Class<TgOrgUnit3> ORG3 = TgOrgUnit3.class;
    protected static final Class<TgOrgUnit2> ORG2 = TgOrgUnit2.class;
    protected static final Class<TgOrgUnit1> ORG1 = TgOrgUnit1.class;
    protected static final Class<String> STRING = String.class;
    protected static final Class<Long> LONG = Long.class;
    protected static final Class<BigInteger> BIG_INTEGER = BigInteger.class;

    private final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2);

    protected EntQuery entQry(final QueryModel qryModel) {
	return qb.generateEntQuery(qryModel);
    }

    protected EntQuery entSubQuery1(final QueryModel qryModel) {
	return qb.generateEntQueryAsSubquery(qryModel);
    }

    protected EntQuery entQuery1(final QueryModel qryModel, final Map<String, Object> paramValues) {
	return qb.generateEntQuery(qryModel, paramValues);
    }

    protected EntProp prop(final String propName) {
	return new EntProp(propName);
    }

    protected PropResolutionInfo propResInf(final String propName, final String aliasPart, final String propPart, final boolean implicitId, final Class propType) {
	return new PropResolutionInfo(prop(propName), aliasPart, propPart, implicitId, propType);
    }

    protected final ComparisonTestModel alwaysTrueCondition = new ComparisonTestModel(new EntValue(0), ComparisonOperator.EQ, new EntValue(0));

    protected List<List<PropResolutionInfo>> getSourcesReferencingProps(final EntQuery entQry) {
	final List<List<PropResolutionInfo>> result = new ArrayList<List<PropResolutionInfo>>();
	for (final IEntQuerySource source : entQry.getSources().getAllSources()) {
	    if (!source.generated()) {
		result.add(source.getReferencingProps());
	    }
	}

	return result;
    }

    protected List<List<PropResolutionInfo>> getSourcesFinalReferencingProps(final EntQuery entQry) {
	final List<List<PropResolutionInfo>> result = new ArrayList<List<PropResolutionInfo>>();
	for (final IEntQuerySource source : entQry.getSources().getAllSources()) {
	    result.add(source.getFinalReferencingProps());
	}

	return result;
    }

    protected final List<PropResolutionInfo> prepare(final PropResolutionInfo ...infos ) {
	return Arrays.asList(infos);
    }

    protected final List<List<PropResolutionInfo>> compose(final List<PropResolutionInfo> ...srcLists) {
	return Arrays.asList(srcLists);
    }
}
