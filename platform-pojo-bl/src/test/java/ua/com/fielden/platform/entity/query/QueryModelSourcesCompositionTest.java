package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonOperator;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonTestModel;
import ua.com.fielden.platform.entity.query.model.elements.CompoundConditionModel;
import ua.com.fielden.platform.entity.query.model.elements.ConditionsModel;
import ua.com.fielden.platform.entity.query.model.elements.EntProp;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.elements.EntQueryCompoundSourceModel;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourceAsEntity;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourceAsModel;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourcesModel;
import ua.com.fielden.platform.entity.query.model.elements.JoinType;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QueryModelSourcesCompositionTest extends BaseEntQueryTCase {

    @Test
    public void test_query_sources1() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(WORK_ORDER).as("wo").on().prop("v").eq().prop("vehicle").model();

	final ConditionsModel condition = new ConditionsModel(new ComparisonTestModel(new EntProp("v"), ComparisonOperator.EQ, new EntProp("vehicle")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(WORK_ORDER, "wo"), JoinType.IJ, condition));

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(VEHICLE, "v"), others);
	assertEquals("models are different", exp, entQuery1(qry).getSources());
    }

    @Test
    public void test_query_sources1a() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(WORK_ORDER).as("wo").on().prop("v").eq().prop("wo.vehicle").where().val(1).isNotNull().model();

	final ConditionsModel condition = new ConditionsModel(new ComparisonTestModel(new EntProp("v"), ComparisonOperator.EQ, new EntProp("wo.vehicle")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(WORK_ORDER, "wo"), JoinType.IJ, condition));

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(VEHICLE, "v"), others);
	assertEquals("models are different", exp, entQuery1(qry).getSources());
    }

    @Test
    public void test_query_sources_with_explicit_join_and_without_aliases() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(WORK_ORDER).on().prop("v").eq().prop("vehicle").model();

	final ConditionsModel condition = new ConditionsModel(new ComparisonTestModel(new EntProp("v"), ComparisonOperator.EQ, new EntProp("vehicle")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(WORK_ORDER, null), JoinType.IJ, condition));

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(VEHICLE, "v"), others);
	assertEquals("models are different", exp, entQuery1(qry).getSources());
    }

    @Test
    public void test_query_sources2() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(WORK_ORDER).as("wo").on().prop("v").eq().prop("wo.vehicle").leftJoin(WORK_ORDER).as("wo2").on().prop("v").eq().prop("wo2.vehicle").model();

	final ConditionsModel condition1 = new ConditionsModel(new ComparisonTestModel(new EntProp("v"), ComparisonOperator.EQ, new EntProp("wo.vehicle")), new ArrayList<CompoundConditionModel>());
	final ConditionsModel condition2 = new ConditionsModel(new ComparisonTestModel(new EntProp("v"), ComparisonOperator.EQ, new EntProp("wo2.vehicle")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(WORK_ORDER, "wo"), JoinType.IJ, condition1));
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(WORK_ORDER, "wo2"), JoinType.LJ, condition2));

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(VEHICLE, "v"), others);
	assertEquals("models are different", exp, entQuery1(qry).getSources());
    }

    @Test
    public void test_query_with_derived_sources() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).as("v").where().prop("v.model").isNotNull().model();
	final EntityResultQueryModel<TgVehicle> qry = select(sourceQry).as("v").where().prop("v.model").isNotNull().model();

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsModel("v", entQuery1(sourceQry)), new ArrayList<EntQueryCompoundSourceModel>());
	assertEquals("models are different", exp, entQuery1(qry).getSources());
    }

    @Test
    public void test_query_with_derived_sources2() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).as("v").where().prop("v.model").isNotNull().
		groupBy().prop("model").
		groupBy().yearOf().prop("initDate").
		yield().prop("model").as("vehModel").
		yield().yearOf().prop("initDate").as("modelYear").modelAsAggregate();
	final EntityResultQueryModel<TgVehicle> qry = select(sourceQry).as("v").where().prop("vehModel").isNotNull().and().prop("modelYear").ge().val(2000).model();
	final EntQuery entQry = entQuery1(qry);
	//final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsModel("v", entQuery1(sourceQry)), new ArrayList<EntQueryCompoundSourceModel>());
	//assertEquals("models are different", exp, entQuery1(qry).getSources());
    }

    @Test
    public void test_query_with_derived_sources3() {
	final AggregatedResultQueryModel sourceQry = select(VEHICLE).as("v").where().prop("v.model").isNotNull().
		groupBy().prop("model").
		yield().prop("model").as("vehModel").
		yield().minOf().yearOf().prop("initDate").as("modelYear").modelAsAggregate();
	final EntityResultQueryModel<TgVehicle> qry = select(sourceQry).as("v").where().prop("vehModel").isNotNull().and().prop("modelYear").ge().val(2000).model();
	final EntQuery entQry = entQuery1(qry);
	//final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsModel("v", entQuery1(sourceQry)), new ArrayList<EntQueryCompoundSourceModel>());
	//assertEquals("models are different", exp, entQuery1(qry).getSources());
    }

    @Test
    public void test_simple_query_model_13() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").model();
	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(VEHICLE, "v"), others);
	assertEquals("models are different", exp, entQuery1(qry).getSources());
    }

    @Test
    public void test_simple_query_model_14() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).model();
	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(VEHICLE, null), others);
	assertEquals("models are different", exp, entQuery1(qry).getSources());
    }
}