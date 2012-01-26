package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.entity.query.model.elements.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.model.elements.CompoundSingleOperand;
import ua.com.fielden.platform.entity.query.model.elements.EntProp;
import ua.com.fielden.platform.entity.query.model.elements.EntValue;
import ua.com.fielden.platform.entity.query.model.elements.Expression;
import ua.com.fielden.platform.entity.query.model.elements.YieldModel;
import ua.com.fielden.platform.entity.query.model.elements.YieldsModel;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QueryModelYieldingCompositionTest extends BaseEntQueryTCase {

    @Test
    public void test_simple_query_model_20() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", 20);
	final AggregatedResultQueryModel qry = select(VEHICLE).yield().prop("station").as("st").yield().beginExpr().prop("model").add().param("param").endExpr().as("m").modelAsAggregate();
	final Map<String, YieldModel> yields = new HashMap<String, YieldModel>();
	yields.put("st", new YieldModel(new EntProp("station"), "st"));
	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(new EntValue(20), ArithmeticalOperator.ADD));
	final Expression expression = new Expression(new EntProp("model"), compSingleOperands);
	yields.put("m", new YieldModel(expression, "m"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, entQuery1(qry, paramValues).getYields());
    }

    @Test
    public void test_simple_query_model_18() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).yield().prop("model").modelAsEntity(MODEL);
	final Map<String, YieldModel> yields = new HashMap<String, YieldModel>();
	yields.put("id", new YieldModel(new EntProp("model"), "id"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, entQuery1(qry).getYields());
    }

    @Test
    public void test_simple_query_model_19() {
	final AggregatedResultQueryModel qry = select(VEHICLE).yield().prop("station").as("st").yield().prop("model").as("m").modelAsAggregate();
	final Map<String, YieldModel> yields = new HashMap<String, YieldModel>();
	yields.put("st", new YieldModel(new EntProp("station"), "st"));
	yields.put("m", new YieldModel(new EntProp("model"), "m"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, entQuery1(qry).getYields());
    }

    @Test
    public void test_query_1() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).as("v").yield().prop("v.model").modelAsEntity(MODEL);
	final Map<String, YieldModel> yields = new HashMap<String, YieldModel>();
	yields.put("id", new YieldModel(new EntProp("v.model"), "id"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, entQuery1(qry).getYields());
    }

    @Test
    public void test_query_2() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).as("v").yield().prop("v.model").modelAsPrimitive(Long.class);
	final Map<String, YieldModel> yields = new HashMap<String, YieldModel>();
	yields.put(null, new YieldModel(new EntProp("v.model"), null));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, entQuery1(qry).getYields());
    }

    @Test
    @Ignore
    public void test_validation_of_yielded_tree_with_broken_hierarchy() {
	// TODO YieldsModel should be validate to prevent misuse of dot.notated convention - broken hierarchy in yields
	final AggregatedResultQueryModel sourceQry = select(TgFuelUsage.class). //
	groupBy().yearOf().prop("readingDate"). //
	groupBy().prop("vehicle"). //
	yield().prop("vehicle").as("vehicle"). //
	yield().yearOf().prop("readingDate").as("readingYear"). //
	yield().prop("vehicle.model.make").as("vehicle.model.make"). //
	modelAsAggregate();
	try {
	    entQuery1(sourceQry);
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }

    @Test
    @Ignore
    public void test_validation_of_yielded_tree_with_not_existing_prop_in_hierarchy() {
	// TODO YieldsModel should be validate to prevent misuse of dot.notated convention - not-existing/invalid property in hierarchy
	final AggregatedResultQueryModel sourceQry = select(TgFuelUsage.class). //
	groupBy().yearOf().prop("readingDate"). //
	groupBy().prop("vehicle"). //
	yield().prop("vehicle").as("vehicle"). //
	yield().yearOf().prop("readingDate").as("readingYear"). //
	yield().prop("vehicle.model.make").as("vehicle.mordor"). //
	modelAsAggregate();
	try {
	    entQuery1(sourceQry);
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }
}