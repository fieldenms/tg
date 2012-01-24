package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.elements.EntProp;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertEquals;

public class CollectingQueryModelPropertiesTest extends BaseEntQueryTCase {

    @Test
    public void test_prop_collector() {
	final AggregatedResultQueryModel qry = query.select(TgVehicle.class).where().prop("model.desc").like().val("MERC%").
	groupBy().prop("eqClass.desc").
	yield().beginExpr().prop("volume").add().prop("weight").endExpr().as("calc").modelAsAggregate();
	final List<EntProp> exp = new ArrayList<EntProp>();
	// TODO needed to add these as generated properties are currently taken into account in getImmediateProps method
	exp.add(prop("model"));
	exp.add(prop("model.id"));

	exp.add(prop("model.desc"));
	exp.add(prop("eqClass.desc"));
	exp.add(prop("volume"));
	exp.add(prop("weight"));

	assertEquals("models are different", exp, qb.generateEntQuery(qry).getImmediateProps());
    }

    @Test
    public void test_prop_collector_with_subquery() {
	final EntityResultQueryModel<TgVehicleModel> vehModelsQry = query.select(TgVehicleModel.class).where().prop("make").isNotNull().model();
	final AggregatedResultQueryModel qry = query.select(TgVehicle.class).where().prop("model").in().model(vehModelsQry).
	groupBy().prop("eqClass.desc").
	yield().beginExpr().prop("volume").add().prop("weight").endExpr().as("calc").modelAsAggregate();
	final List<EntProp> exp = new ArrayList<EntProp>();
	exp.add(prop("model"));
	exp.add(prop("eqClass.desc"));
	exp.add(prop("volume"));
	exp.add(prop("weight"));
	//exp.add("make");
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getImmediateProps());
    }
}