package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.generation.elements.ComparisonTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundConditionModel;
import ua.com.fielden.platform.entity.query.generation.elements.ConditionsModel;
import ua.com.fielden.platform.entity.query.generation.elements.EntProp;
import ua.com.fielden.platform.entity.query.generation.elements.EntSet;
import ua.com.fielden.platform.entity.query.generation.elements.EntValue;
import ua.com.fielden.platform.entity.query.generation.elements.GroupedConditionsModel;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.QuantifiedTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.Quantifier;
import ua.com.fielden.platform.entity.query.generation.elements.SetTestModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QueryModelConditionsShortcutsTransformationTest extends BaseEntQueryTCase {
    private final EntValue mercLikeValue = new EntValue("MERC%");
    private final EntValue mercValue = new EntValue("MERC");
    private final EntValue audiValue = new EntValue("AUDI");
    private final String message = "Condition models are different!";

    @Test
    public void test_multiple_vs_single_comparison_test_using_equivalent_model() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().anyOfProps("key", "desc").eq().val("MERC").model();
	final EntityResultQueryModel<TgVehicle> qry2 = select(VEHICLE).where().begin().prop("key").eq().val("MERC").or().prop("desc").eq().val("MERC").end().model();
	assertEquals("models are different", entQry(qry2), entQry(qry));
    }

    @Test
    public void test_multiple_vs_single_like_test_using_equivalent_model() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().allOfProps("model.key", "model.make.key").like().anyOfValues("MERC%", "AUDI%", "BMW%").model();
	final EntityResultQueryModel<TgVehicle> qry2 = select(VEHICLE).where().begin().
		begin().prop("model.key").like().val("MERC%").or().prop("model.key").like().val("AUDI%").or().prop("model.key").like().val("BMW%").end().and().
		begin().prop("model.make.key").like().val("MERC%").or().prop("model.make.key").like().val("AUDI%").or().prop("model.make.key").like().val("BMW%").end().
		end().model();
	assertEquals("models are different", entQry(qry2), entQry(qry));
    }

    @Ignore
    @Test
    public void test_ignore_in_multiple_vs_single_like_test_using_equivalent_model() {
	// TODO implement anyOfIValues
	final String [] values = new String[]{null, null, null};
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().allOfProps("model.key", "model.make.key").like().anyOfValues(values).model();
	final EntityResultQueryModel<TgVehicle> qry2 = select(VEHICLE).where().begin().
		begin().val(0).eq().val(0).or().val(0).eq().val(0).or().val(0).eq().val(0).end().and().
		begin().val(0).eq().val(0).or().val(0).eq().val(0).or().val(0).eq().val(0).end().
		end().model();
	assertEquals("models are different", entQry(qry2), entQry(qry));
    }

    @Test
    public void test_multiple_vs_single_comparison_test() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().anyOfProps("key", "desc").eq().val("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new ComparisonTestModel(new EntProp("desc"), ComparisonOperator.EQ, mercValue)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("key"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals(message, exp2, entQry(qry).getConditions());
    }

    @Test
    public void test_multiple_vs_single_comparison_test_with_only_one_operand() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().anyOfProps("model").eq().val("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals(message, exp2, entQry(qry).getConditions());
    }

    @Test
    public void test_single_vs_multiple_comparison_test() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model").eq().anyOfValues("MERC", "AUDI").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, audiValue)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals(message, exp2, entQry(qry).getConditions());
    }

    @Test
    public void test_single_vs_multiple_comparison_test_with_only_one_operand() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model").eq().anyOfValues("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals(message, exp2, entQry(qry).getConditions());
    }

    @Test
    public void test_set_test_with_values_and_multiple_operand() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().anyOfProps("key", "desc").in().values("sta1", "sta2").model();

	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new SetTestModel(new EntProp("desc"), false, new EntSet(Arrays.asList(new ISingleOperand[]{new EntValue("sta1"), new EntValue("sta2")})))));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new SetTestModel(new EntProp("key"), false, new EntSet(Arrays.asList(new ISingleOperand[]{new EntValue("sta1"), new EntValue("sta2")}))), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals(message, exp2, entQry(qry).getConditions());
    }

    @Test
    public void test_multiple_quantified_test() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = select(MODEL).model();
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().anyOfProps("key", "desc").eq().any(vehModels).model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new QuantifiedTestModel(new EntProp("desc"), ComparisonOperator.EQ, Quantifier.ANY, entSubQry(vehModels))));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new QuantifiedTestModel(new EntProp("key"), ComparisonOperator.EQ, Quantifier.ANY, entSubQry(vehModels)), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals(message, exp2, entQry(qry).getConditions());
    }
}