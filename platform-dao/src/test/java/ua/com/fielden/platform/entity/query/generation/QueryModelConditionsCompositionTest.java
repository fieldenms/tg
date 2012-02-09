package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.entity.query.generation.elements.ComparisonTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundConditionModel;
import ua.com.fielden.platform.entity.query.generation.elements.ConditionsModel;
import ua.com.fielden.platform.entity.query.generation.elements.DayOfModel;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.EntQueryCompoundSourceModel;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuerySourceFromEntityType;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuerySourcesModel;
import ua.com.fielden.platform.entity.query.generation.elements.EntSetFromQryModel;
import ua.com.fielden.platform.entity.query.generation.elements.ExistenceTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.LikeTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.MonthOfModel;
import ua.com.fielden.platform.entity.query.generation.elements.NullTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.QuantifiedTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.Quantifier;
import ua.com.fielden.platform.entity.query.generation.elements.SetTestModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.expr;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QueryModelConditionsCompositionTest extends BaseEntQueryCompositionTCase {

    protected final IWhere0 where_veh = select(VEHICLE).where();
    protected final IWhere0 where_wo = select(WORK_ORDER).as("wo").where();

    @Test
    public void test_like() {
	assertModelsEquals( //
		conditions(new LikeTestModel(prop("model.desc"), val(mercLike), false, false)), //
		conditions(where_veh.prop("model.desc").like().val(mercLike)));
    }

    @Test
    public void test_notLike() {
	assertModelsEquals( //
		conditions(new LikeTestModel(prop("model.desc"), val(mercLike), true, false)), //
		conditions(where_veh.prop("model.desc").notLike().val(mercLike)));
    }

    @Test
    public void test_set_test_with_values() {
	assertModelsEquals(//
		conditions(new SetTestModel(prop("model"), false, set(val(merc), val(audi)))), //
		conditions(where_veh.prop("model").in().values(merc, audi)));
    }

    @Test
    public void test_set_test_with_query() {
	assertModelsEquals( //
		conditions(new SetTestModel(prop("model"), false, new EntSetFromQryModel(entSubQry(select(MODEL).model())))), //
		conditions(where_veh.prop("model").in().model(select(MODEL).model())));
    }

    @Test
    public void test_plain_quantified_test() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = select(MODEL).model();
	assertModelsEquals( //
		conditions(new QuantifiedTestModel(prop("model"), _eq, Quantifier.ANY, entSubQry(vehModels))), //
		conditions(where_veh.prop("model").eq().any(vehModels)));
    }

    @Test
    public void test_simple_query_model_01() {
	assertModelsEquals( //
		conditions(new NullTestModel(prop("model"), true), //
			compound(_and, new NullTestModel(prop("station"), false))), //
		conditions(where_veh.prop("model").isNotNull().and().prop("station").isNull()));
    }

    @Test
    public void test_simple_query_model_02() {
	assertModelsEquals(//
		conditions(new ComparisonTestModel(prop("price"), _gt, val(100)), //
			compound(_and, new ComparisonTestModel(prop("purchasePrice"), _lt, prop("price")))), //
		conditions(where_veh.prop("price").gt().val(100).and().prop("purchasePrice").lt().prop("price")));
    }

    @Test
    public void test_simple_query_model_03() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("eqclass", "CL1");

	assertModelsEquals(//
		conditions(new NullTestModel(prop("model"), true), //
			compound(_and, new NullTestModel(val("CL1"), false))), //
		conditions(where_veh.prop("model").isNotNull().and().param("eqclass").isNull(), paramValues));
    }

    @Test
    public void test_simple_query_model_04() {
	assertModelsEquals(//
		conditions(group(false, //
			new NullTestModel(prop("model"), true), //
			compound(_and, new NullTestModel(prop("station"), false)))), //
		conditions(where_veh.begin().prop("model").isNotNull().and().prop("station").isNull().end()));
    }

    @Test
    public void test_simple_query_model_05() {
	assertModelsEquals( //
	conditions(group(false, //
		new NullTestModel(prop("model"), true), //
		compound(_and, new NullTestModel(prop("station"), false))), //
		compound(_and, new NullTestModel(prop("price"), true))), //
		conditions(where_veh.begin().prop("model").isNotNull().and().prop("station").isNull().end().and().prop("price").isNotNull()));
    }

    @Test
    public void test_simple_query_model_06() {
	assertModelsEquals(//
	conditions(new NullTestModel(prop("model"), true), //
		compound(_and, new ExistenceTestModel(false, entSubQry(select(VEHICLE).model())))), //
		conditions(where_veh.prop("model").isNotNull().and().exists(select(VEHICLE).model())));
    }

    @Test
    public void test_simple_query_model_07() {
	assertModelsEquals(//
	conditions(group(false, new NullTestModel(prop("model"), true), //
		compound(_or, new NullTestModel(prop("station"), true)))), //
		conditions(where_veh.anyOfProps("model", "station").isNotNull()));
    }

    @Test
    public void test_simple_query_model_08() {
	assertModelsEquals(//
	conditions(new NullTestModel(expression(prop("price"), compound(_add, prop("purchasePrice"))), false)), //
		conditions(where_veh.beginExpr().prop("price").add().prop("purchasePrice").endExpr().isNull()));
    }

    @Test
    public void test_simple_query_model_09() {
	assertModelsEquals(//
	conditions(new NullTestModel(expression(prop("price.amount"), compound(_add, prop("purchasePrice.amount"))), false)), //
		conditions(where_veh.expr(expr().prop("price.amount").add().prop("purchasePrice.amount").model()).isNull()));
    }

    @Test
    public void test_simple_query_model_10() {
	assertModelsEquals(//
	conditions(new NullTestModel(expression(prop("model"), compound(_add, expression(prop("model"), compound(_mult, prop("station"))))), false)), //
		conditions(where_veh.expr(expr().prop("model").add().expr(expr().prop("model").mult().prop("station").model()).model()).isNull()));
    }

    @Test
    public void test_simple_query_model_11() {
	assertModelsEquals(//
		conditions(new ComparisonTestModel(new DayOfModel(prop("initDate")), _gt, val(15)), compound(_and, new ComparisonTestModel(prop("price"), _lt, prop("purchasePrice")))), //
		conditions(where_veh.dayOf().prop("initDate").gt().val(15).and().prop("price").lt().prop("purchasePrice")));
    }

    @Test
    public void test_simple_query_model_12() {
	assertModelsEquals(//
		conditions(new ComparisonTestModel(new MonthOfModel(prop("initDate")), _gt, val(3)), compound(_and, new ComparisonTestModel(prop("price"), _lt, prop("purchasePrice")))), //
		conditions(where_veh.monthOf().prop("initDate").gt().val(3).and().prop("price").lt().prop("purchasePrice")));
    }

    @Test
    public void test_simple_query_model_13() {
	assertModelsEquals(//
		conditions(group(false, //
			new ExistenceTestModel(false, entSubQry(select(VEHICLE).model())), //
			compound(_or, //
				new ExistenceTestModel(false, entSubQry(select(WORK_ORDER).model()))))), //
		conditions(where_veh.existsAnyOf(select(VEHICLE).model(), select(WORK_ORDER).model())));
    }

    @Test
    public void test_ignore_of_null_value_in_condition1() {
	assertModelsEquals(//
		conditions(new LikeTestModel(prop("model.desc"), val(mercLike), false, false)), //
		conditions(where_veh.prop("model.desc").like().iVal(mercLike)));
    }

    @Test
    public void test_ignore_of_null_value_in_condition2() {
	assertModelsEquals(//
		conditions(alwaysTrueCondition), //
		conditions(where_veh.prop("model.desc").like().iVal(null)));
    }

    @Test
    public void test_ignore_of_null_value_in_condition3() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", "MERC%");
	assertModelsEquals(//
		conditions(new LikeTestModel(prop("model.desc"), val("MERC%"), false, false)), //
		conditions(where_veh.prop("model.desc").like().param("param"), paramValues));
    }

    @Test
    public void test_ignore_of_null_value_in_condition4() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", null);
	assertModelsEquals(//
		conditions(alwaysTrueCondition), //
		conditions(where_veh.prop("model.desc").like().iParam("param"), paramValues));
    }

    @Test
    public void test_ignore_of_null_value_in_condition5() {
	assertModelsEquals(//
		conditions(group(false, alwaysTrueCondition)), //
		conditions(where_veh.prop("model.desc").like().anyOfValues()));
    }

    @Test
    public void test_expressions1() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("costMultiplier", 1);
	paramValues.put("costDivider", 2);

	assertModelsEquals(//
		conditions(new ComparisonTestModel(
			expression(
				expression(
					expression(
						expression(prop("wo.actCost.amount"), compound( _mult, val(1))), //
						compound(_add, prop("wo.estCost.amount")), compound(_div, val(2))), //
					compound(_add, prop("wo.yearlyCost.amount"))), //
				compound(_div, val(12))),
		_gt, val(1000))), //

		conditions(where_wo.
			beginExpr().beginExpr(). //
			beginExpr().beginExpr().prop("wo.actCost.amount").mult().param("costMultiplier").endExpr().add().prop("wo.estCost.amount").div().param("costDivider").endExpr(). //
			add().prop("wo.yearlyCost.amount").endExpr(). //
			div().val(12).endExpr(). //
			gt().val(1000), paramValues));
    }

    @Test
    public void test_query_sources() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(TgWorkOrder.class).as("wo").on().prop("v").eq().prop("wo.vehicle").leftJoin(TgWorkOrder.class).as("wo2").on().prop("v").eq().prop("wo2.vehicle"). //
	where().dayOf().prop("initDate").gt().val(15).and().prop("price").lt().prop("purchasePrice").model();

	final ConditionsModel condition1 = new ConditionsModel(new ComparisonTestModel(prop("v"), _eq, prop("wo.vehicle")), new ArrayList<CompoundConditionModel>());
	final ConditionsModel condition2 = new ConditionsModel(new ComparisonTestModel(prop("v"), _eq, prop("wo2.vehicle")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceFromEntityType(TgWorkOrder.class, "wo"), JoinType.IJ, condition1));
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceFromEntityType(TgWorkOrder.class, "wo2"), JoinType.LJ, condition2));

	final EntQuery act = entResultQry(qry);
	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceFromEntityType(VEHICLE, "v"), others);
	assertEquals("models are different", exp, act.getSources());

	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	others2.add(new CompoundConditionModel(_and, new ComparisonTestModel(prop("price"), _lt, prop("purchasePrice"))));
	final ConditionsModel exp2 = new ConditionsModel(new ComparisonTestModel(new DayOfModel(prop("initDate")), _gt, val(15)), others2);
	assertEquals(exp2, act.getConditions());
    }
}