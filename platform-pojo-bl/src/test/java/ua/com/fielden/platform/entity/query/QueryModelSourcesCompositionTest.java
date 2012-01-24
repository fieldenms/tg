package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.query;
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
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import static org.junit.Assert.assertEquals;

public class QueryModelSourcesCompositionTest extends BaseEntQueryTCase {

    @Test
    public void test_query_sources1() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").join(TgWorkOrder.class).as("wo").on().prop("a").eq().prop("b").model();

	final ConditionsModel condition = new ConditionsModel(new ComparisonTestModel(new EntProp("a"), ComparisonOperator.EQ, new EntProp("b")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo"), JoinType.IJ, condition));

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, "v"), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_query_sources1a() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").join(TgWorkOrder.class).as("wo").on().prop("a").eq().prop("b").where().val(1).isNotNull().model();

	final ConditionsModel condition = new ConditionsModel(new ComparisonTestModel(new EntProp("a"), ComparisonOperator.EQ, new EntProp("b")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo"), JoinType.IJ, condition));

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, "v"), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_query_sources_with_explicit_join_and_without_aliases() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").join(TgWorkOrder.class).on().prop("a").eq().prop("b").model();

	final ConditionsModel condition = new ConditionsModel(new ComparisonTestModel(new EntProp("a"), ComparisonOperator.EQ, new EntProp("b")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, null), JoinType.IJ, condition));

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, "v"), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_query_sources2() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").join(TgWorkOrder.class).as("wo").on().prop("a").eq().prop("b").leftJoin(TgWorkOrder.class).as("wo2").on().prop("a2").eq().prop("b2").model();

	final ConditionsModel condition1 = new ConditionsModel(new ComparisonTestModel(new EntProp("a"), ComparisonOperator.EQ, new EntProp("b")), new ArrayList<CompoundConditionModel>());
	final ConditionsModel condition2 = new ConditionsModel(new ComparisonTestModel(new EntProp("a2"), ComparisonOperator.EQ, new EntProp("b2")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo"), JoinType.IJ, condition1));
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo2"), JoinType.LJ, condition2));

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, "v"), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_query_with_derived_sources() {
	final EntityResultQueryModel<TgVehicle> sourceQry = query.select(TgVehicle.class).as("v").where().prop("v.model").isNotNull().model();
	final EntityResultQueryModel<TgVehicle> qry = query.select(sourceQry).as("v").where().prop("v.model").isNotNull().model();

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsModel("v", qb.generateEntQuery(sourceQry)), new ArrayList<EntQueryCompoundSourceModel>());
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_query_with_derived_sources2() {
	final AggregatedResultQueryModel sourceQry = query.select(TgVehicle.class).as("v").where().prop("v.model").isNotNull().
		groupBy().prop("model").
		groupBy().yearOf().prop("initDate").
		yield().prop("model").as("vehModel").
		yield().yearOf().prop("initDate").as("modelYear").modelAsAggregate();
	final EntityResultQueryModel<TgVehicle> qry = query.select(sourceQry).as("v").where().prop("vehModel").isNotNull().and().prop("modelYear").ge().val(2000).model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	entQry.validate();
	//final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsModel("v", qb.generateEntQuery(sourceQry)), new ArrayList<EntQueryCompoundSourceModel>());
	//assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_query_with_derived_sources3() {
	final AggregatedResultQueryModel sourceQry = query.select(TgVehicle.class).as("v").where().prop("v.model").isNotNull().
		groupBy().prop("model").
		yield().prop("model").as("vehModel").
		yield().minOf().yearOf().prop("initDate").as("modelYear").modelAsAggregate();
	final EntityResultQueryModel<TgVehicle> qry = query.select(sourceQry).as("v").where().prop("vehModel").isNotNull().and().prop("modelYear").ge().val(2000).model();
	final EntQuery entQry = qb.generateEntQuery(qry);
	entQry.validate();
	//final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsModel("v", qb.generateEntQuery(sourceQry)), new ArrayList<EntQueryCompoundSourceModel>());
	//assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_simple_query_model_13() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").model();
	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, "v"), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_simple_query_model_14() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).model();
	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, null), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }
}