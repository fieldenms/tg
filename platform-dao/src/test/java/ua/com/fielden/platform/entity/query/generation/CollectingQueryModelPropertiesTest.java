package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.generation.elements.EntProp;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class CollectingQueryModelPropertiesTest extends BaseEntQueryTCase {

    @Test
    public void test_prop_collector() {
	final AggregatedResultQueryModel qry = select(VEHICLE).where().prop("model.desc").like().val("MERC%").
	groupBy().prop("model.desc").
	yield().beginExpr().yearOf().prop("initDate").add().monthOf().prop("initDate").endExpr().as("calc").modelAsAggregate();
	final List<EntProp> exp = new ArrayList<EntProp>();
	// TODO needed to add these as generated properties are currently taken into account in getImmediateProps method
	exp.add(prop("model"));
	exp.add(prop("model.id"));

	exp.add(prop("model.desc"));
	exp.add(prop("model.desc"));
	exp.add(prop("initDate"));
	exp.add(prop("initDate"));

	assertEquals("models are different", exp, entQry(qry).getImmediateProps());
    }

    @Test
    public void test_prop_collector_with_subquery() {
	final EntityResultQueryModel<TgVehicleModel> vehModelSubQry = select(MODEL).where().prop("make").isNotNull().model();
	final AggregatedResultQueryModel qry = select(VEHICLE).where().prop("model").in().model(vehModelSubQry).
	groupBy().prop("model.desc").
	yield().beginExpr().yearOf().prop("initDate").add().monthOf().prop("initDate").endExpr().as("calc").modelAsAggregate();
	final List<EntProp> exp = new ArrayList<EntProp>();

	// TODO needed to add these as generated properties are currently taken into account in getImmediateProps method
	exp.add(prop("model"));
	exp.add(prop("model.id"));

	exp.add(prop("model"));
	exp.add(prop("model.desc"));
	exp.add(prop("initDate"));
	exp.add(prop("initDate"));
	assertEquals("models are different", exp, entQry(qry).getImmediateProps());
    }

    @Test
    public void test_prop_collector_with_correlates_subquery() {
	final EntityResultQueryModel<TgVehicleModel> vehModelSubQry = query.select(MODEL).where().prop("make").isNotNull().and().prop("id").eq().prop("model").model();
	final AggregatedResultQueryModel qry = select(VEHICLE).where().exists(vehModelSubQry).
	groupBy().prop("model.desc").
	yield().beginExpr().yearOf().prop("initDate").add().monthOf().prop("initDate").endExpr().as("calc").modelAsAggregate();
	final List<EntProp> exp = new ArrayList<EntProp>();

	// TODO needed to add these as generated properties are currently taken into account in getImmediateProps method
	exp.add(prop("model"));
	exp.add(prop("model.id"));

	exp.add(prop("model.desc"));
	exp.add(prop("initDate"));
	exp.add(prop("initDate"));

	assertEquals("models are different", exp, entQry(qry).getImmediateProps());
    }

}