package ua.com.fielden.platform.entity.query.fetching;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.ITgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.ITgBogie;
import ua.com.fielden.platform.sample.domain.ITgBogieLocation;
import ua.com.fielden.platform.sample.domain.ITgFuelUsage;
import ua.com.fielden.platform.sample.domain.ITgMakeCount;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit5;
import ua.com.fielden.platform.sample.domain.ITgVehicle;
import ua.com.fielden.platform.sample.domain.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.ITgWagon;
import ua.com.fielden.platform.sample.domain.ITgWagonSlot;
import ua.com.fielden.platform.sample.domain.ITgWorkshop;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgMakeCount;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWagon;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class EntityQueryExecutionTest extends AbstractDomainDrivenTestCase {

    private final ITgBogie bogieDao = getInstance(ITgBogie.class);
    private final ITgBogieLocation bogieLocationDao = getInstance(ITgBogieLocation.class);
    private final ITgWagon wagonDao = getInstance(ITgWagon.class);
    private final ITgWorkshop workshopDao = getInstance(ITgWorkshop.class);
    private final ITgWagonSlot wagonSlotDao = getInstance(ITgWagonSlot.class);
    private final ITgVehicleModel vehicleModelDao = getInstance(ITgVehicleModel.class);
    private final ITgVehicleMake vehicleMakeDao = getInstance(ITgVehicleMake.class);
    private final ITgVehicle vehicleDao = getInstance(ITgVehicle.class);
    private final ITgFuelUsage fuelUsageDao = getInstance(ITgFuelUsage.class);
    private final IUserDao userDao = getInstance(IUserDao.class);
    private final IUserRoleDao userRoleDao = getInstance(IUserRoleDao.class);
    private final IUserAndRoleAssociationDao userAndRoleAssociationDao = getInstance(IUserAndRoleAssociationDao.class);
    private final IEntityAggregatesDao aggregateDao = getInstance(IEntityAggregatesDao.class);
    private final EntityWithMoneyDao entityWithMoneyDao = getInstance(EntityWithMoneyDao.class);
    private final ISecurityRoleAssociationDao secRolAssociationDao = getInstance(ISecurityRoleAssociationDao.class);
    private final ITgMakeCount makeCountDao = getInstance(ITgMakeCount.class);
    private final ITgAverageFuelUsage averageFuelUsageDao = getInstance(ITgAverageFuelUsage.class);
    private final ITgOrgUnit5 orgUnit5Dao = getInstance(ITgOrgUnit5.class);


    @Test
    public void test_query_based_entities_with_composite_props() {
	final EntityResultQueryModel<TgMakeCount> qry = select(TgMakeCount.class).model();
	makeCountDao.getAllEntities(from(qry).model());
    }

//    @Test
//    public void test_query_111() {
//	final EntityResultQueryModel<TgVehicle> qry = select(select(TgVehicle.class).model()).
//		where().prop("key").eq().val("CAR1").
//		model();
//	final TgVehicle vehicle1 = vehicleDao.getEntity(from(qry).with(fetchOnly(TgVehicle.class). //
//		with("key"). //
//		with("purchasePrice")).model());
//	assertEquals(new Money(new BigDecimal(10)), vehicle1.getPurchasePrice());
//
//	final TgVehicle vehicle2 = vehicleDao.getEntity(from(qry). //
//		with(fetchOnly(TgVehicle.class). //
//			with("aggregated"). //
//			without("id"). //
//			without("version")). //
//		model());
//	assertEquals(new BigDecimal("10.0000"), vehicle2.getAggregated());
//    }


    @Test
    public void test_query_for_correct_fetch_model_autoenhancement_in_case_of_composite_user_type_with_single_subproperty() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).
		where().prop("key").eq().val("CAR1").
		model();
	final TgVehicle vehicle1 = vehicleDao.getEntity(from(qry).with(fetchOnly(TgVehicle.class). //
		with("key"). //
		with("purchasePrice")).model());
	assertEquals(new Money(new BigDecimal(10)), vehicle1.getPurchasePrice());
    }

    @Test
    public void test_query_for_correct_fetching_adjustment() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).
		where().prop("key").eq().val("CAR1").
		yield().prop("key").as("key").
		yield().prop("desc").as("desc").
		yield().prop("model").as("model").
		yield().prop("model.make").as("model.make").
		yield().prop("model.make.key").as("model.make.key").
		modelAsEntity(TgVehicle.class);
	final TgVehicle vehicle1 = vehicleDao.getEntity(from(qry).with(fetchOnly(TgVehicle.class). //
		with("key"). //
		with("desc"). //
		with("model", fetchOnly(TgVehicleModel.class).with("key").with("make", fetchOnly(TgVehicleMake.class).with("key")))).model());
	assertNotNull(vehicle1.getModel().getMake().getKey());
    }

    @Test
    public void test_ordering_by_yielded_prop() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).groupBy().prop("model.make.key"). //
									yield().prop("model.make.key").as("makeKey"). //
									yield().countAll().as("count"). //
									modelAsAggregate();

	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(qry).with(orderBy().yield("count").desc().model()).model());
    }

    @Test
    public void test_ordering_by_non_yielded_prop() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).groupBy().prop("model.make.key"). //
									yield().prop("model.make.key").as("makeKey"). //
									yield().countAll().as("count"). //
									modelAsAggregate();
	try {
	    aggregateDao.getAllEntities(from(qry).with(orderBy().yield("count1").desc().model()).model());
	    fail("Should have failed while trying to order by not yielden property");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_query_query_with_grouping_and_subproperties() {
	final AggregatedResultQueryModel qry1 = select(TgVehicleModel.class).where().condition(null).or().allOfProps("1","@").ge().val(222).and(). //
		condition(null).and().begin().condition(null).end().and().beginExpr().caseWhen().condition(null).then().now().when().condition(null).then().val(1).end().endExpr().isNotNull().modelAsAggregate();

	final AggregatedResultQueryModel qry = select(TgVehicleModel.class).
		groupBy().prop("make"). //
		yield().countOf().prop("make").as("dmakes"). //
		yield().prop("make.key").as("key").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(qry).model());


	final ConditionModel c1 = cond().prop("aaa").eq().val(111).or().prop("bbb").isNotNull().model();
	final List<Pair<TokenCategory, Object>> expected = new ArrayList<>();
	expected.add(new Pair(TokenCategory.PROP, "aaa"));
	expected.add(new Pair(TokenCategory.COMPARISON_OPERATOR, ComparisonOperator.EQ));
	expected.add(new Pair(TokenCategory.VAL, 111));
	expected.add(new Pair(TokenCategory.LOGICAL_OPERATOR, LogicalOperator.OR));
	expected.add(new Pair(TokenCategory.PROP, "bbb"));
	expected.add(new Pair(TokenCategory.NULL_OPERATOR, true));
	assertEquals(expected, c1.getTokens());

	final IStandAloneConditionOperand<AbstractEntity<?>> s = cond();

	final IStandAloneConditionComparisonOperator<AbstractEntity<?>> d = s.prop("aaa");
	final ConditionModel c2 = d.eq().val(111).or().prop("bbb").isNotNull().model();
	assertEquals(expected, c2.getTokens());

	System.out.println(
		cond().round().prop("a").to(3).eq().val(0).and().beginExpr().prop("a").endExpr().isNotNull().and().now().eq().val(1).and().exists(null).and().condition(null).and().concat().prop("a").with().prop("b").with().prop("c").end().eq().all(null).and().condition(null).model().getTokens()
);
    }

    @Ignore
    @Test
    public void test_query_query_with_grouping_and_aggregation1() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(TgVehicleModel.class).as("AAA").where().prop("make.key").eq().val("AA").model();
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(qry).model());
    }

    ////////////////////////////////////////////////////////////////   UNION ENTITIES ////////////////////////////////////////////////////////////
    @Test
    public void test_query_union_entity_implied_query_model() {
	final EntityResultQueryModel<TgBogieLocation> qry = select(TgBogieLocation.class).model();
	assertEquals(workshopDao.count(select(TgWorkshop.class).model()) + wagonSlotDao.count(select(TgWagonSlot.class).model()), bogieLocationDao.count(qry));
    }

    @Test
    public void test_query_with_union_property_being_null() {
	final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("key").eq().val("BOGIE2").model();
	assertNull(bogieDao.getEntity(from(qry).model()).getLocation());
    }

    @Test
    public void test_query_with_union_property_subproperties() {
	final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.workshop.key").eq().val("WSHOP1").or().prop("location.wagonSlot.wagon.key").eq().val("WAGON1").model();
	assertEquals(bogieDao.findByKey("BOGIE1"), bogieDao.getEntity(from(qry).model()));
    }

    @Test
    public void test_query_with_union_property_subproperties_via_query_based_source() {
	final EntityResultQueryModel<TgBogie> qry = select(select(TgBogie.class).model()).where().prop("location.workshop.key").eq().val("WSHOP1").or().prop("location.wagonSlot.wagon.key").eq().val("WAGON1").model();
	assertEquals(bogieDao.findByKey("BOGIE1"), bogieDao.getEntity(from(qry).model()));
    }

    @Test
    public void test_query_with_union_entity_id_property() {
	final Long workshopId = workshopDao.findByKey("WSHOP1").getId();
	final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.id").eq().val(workshopId).model();
	final EntityResultQueryModel<TgBogie> expQry = select(TgBogie.class).where().prop("location.workshop.id").eq().val(workshopId).model();
	assertEquals(bogieDao.getEntity(from(expQry).model()), bogieDao.getEntity(from(qry).model()));
    }

    @Test
    public void test_query_with_union_entity_key_property() {
	final String workshopKey = "WSHOP1";
	final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.key").eq().val(workshopKey).model();
	final EntityResultQueryModel<TgBogie> expQry = select(TgBogie.class).where().prop("location.workshop.key").eq().val(workshopKey).model();
	assertEquals(bogieDao.getEntity(from(expQry).model()), bogieDao.getEntity(from(qry).model()));
    }

    @Test
    public void test_query_with_union_property0b() {
	final ExpressionModel idModel = expr().caseWhen().prop("wagonSlot").isNotNull().then().prop("wagonSlot"). //
		when().prop("workshop").isNotNull().then().prop("workshop").otherwise().val(null).end().model();

	final EntityResultQueryModel<TgBogieLocation> qry = select(TgBogieLocation.class).where().expr(idModel).eq().val(workshopDao.findByKey("WSHOP1")).model();
	final List<TgBogieLocation> models = bogieLocationDao.getAllEntities(from(qry).with(fetchAll(TgBogieLocation.class)).model());
	assertEquals("Incorrect key 1", "WSHOP1", models.get(0).getKey());
    }


    @Test
    @Ignore
    public void test_query_with_union_property2() {
	final EntityResultQueryModel<TgWorkshop> qry = select(select(TgBogie.class).model()).where().prop("location.workshop.key").eq().val("WSHOP1").yield().prop("location.workshop").modelAsEntity(TgWorkshop.class);
	final List<TgWorkshop> models = workshopDao.getAllEntities(from(qry).with(fetch(TgWorkshop.class)).model());
	assertEquals("Incorrect key 1", "WSHOP1", models.get(0).getKey());
    }

    @Test
    public void test_query_union_entity_() {
	final EntityResultQueryModel<TgBogieLocation> qry1 = select(TgWagonSlot.class).as("a").yield().prop("a").as("wagonSlot").yield().val(null).as("workshop").modelAsEntity(TgBogieLocation.class);
	final EntityResultQueryModel<TgBogieLocation> qry2 = select(TgWorkshop.class).as("a").yield().val(null).as("wagonSlot").yield().prop("a").as("workshop").modelAsEntity(TgBogieLocation.class);
	final EntityResultQueryModel<TgBogieLocation> qry3 = select(qry1, qry2).model();

	bogieLocationDao.getAllEntities(from(qry1)/*.with(fetchAll(TgBogieLocation.class))*/.model());
	bogieLocationDao.getAllEntities(from(qry2)/*.with(fetchAll(TgBogieLocation.class))*/.model());

	final List<TgBogieLocation> models = bogieLocationDao.getAllEntities(from(qry3).model());
	assertEquals("Incorrect key", 13, models.size());
    }

    @Test
    public void test_query_union_entityA_() {
	final EntityResultQueryModel<TgBogieLocation> qry1 = select(TgWagonSlot.class).as("a").yield().prop("a").as("wagonSlot").modelAsEntity(TgBogieLocation.class);
	final List<TgBogieLocation> models = bogieLocationDao.getAllEntities(from(qry1).model());
    }


    ////////////////////////////////////////////////////////////////   SYNTHETIC ENTITIES ////////////////////////////////////////////////////////////

    @Test
    public void test_retrieval_of_synthetic_entity6() {
	final EntityResultQueryModel<TgAverageFuelUsage> qry = select(TgAverageFuelUsage.class).where().prop("key.key").eq().val("CAR2").model();
	final List<TgAverageFuelUsage> models = averageFuelUsageDao.getAllEntities(from(qry). //
		with("datePeriod.from", new DateTime(2008, 01, 01, 0, 0).toDate()). //
		with("datePeriod.to", new DateTime(2010, 01, 01, 0, 0).toDate()). //
		model());
	assertEquals("Incorrect key", 1, models.size());
	assertEquals("Incorrect key", vehicleDao.findByKey("CAR2").getId(), models.get(0).getId());
    }

    @Test
    public void test_retrieval_of_synthetic_entity5() {
	final EntityResultQueryModel<TgAverageFuelUsage> qry = select(TgAverageFuelUsage.class).where().prop("key.key").eq().val("CAR2").model();
	final List<TgAverageFuelUsage> models = averageFuelUsageDao.getAllEntities(from(qry). //
		with("datePeriod.from", new DateTime(2008, 01, 01, 0, 0).toDate()). //
		with("datePeriod.to", new DateTime(2010, 01, 01, 0, 0).toDate()). //
		model());
	assertEquals("Incorrect key", 1, models.size());
	assertEquals("Incorrect key", "120", models.get(0).getQty().toString());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey().getKey());
    }

    @Test
    public void test_retrieval_of_synthetic_entity4() {
	final EntityResultQueryModel<TgAverageFuelUsage> qry = select(TgAverageFuelUsage.class).where().prop("key.key").eq().val("CAR2").model();
	final List<TgAverageFuelUsage> models = averageFuelUsageDao.getAllEntities(from(qry). //
		with("datePeriod.from", null). //
		with("datePeriod.to", null). //
		model());
	assertEquals("Incorrect key", 1, models.size());
	assertEquals("Incorrect key", "220", models.get(0).getQty().toString());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey().getKey());
    }

    @Test
    public void test_retrieval_of_synthetic_entity3() {
	final EntityResultQueryModel<TgMakeCount> qry = select(TgMakeCount.class).where().prop("key.key").in().values("MERC", "BMW").model();
	final List<TgMakeCount> models = makeCountDao.getAllEntities(from(qry).with(fetchOnly(TgMakeCount.class).with("key").with("count")).model());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test_retrieval_of_synthetic_entity2() {
	final EntityResultQueryModel<TgMakeCount> qry = select(TgMakeCount.class).where().prop("key.key").in().values("MERC", "BMW"). //
	yield().prop("key").as("key").
	yield().prop("count").as("count").
	modelAsEntity(TgMakeCount.class);

	final List<TgMakeCount> models = makeCountDao.getAllEntities(from(qry).model());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test_retrieval_of_synthetic_entity() {
	final AggregatedResultQueryModel model = select(TgMakeCount.class).where().prop("key.key").in().values("MERC", "BMW").yield().prop("key").as("make").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
	assertEquals("Incorrect key", 2, models.size());
    }



    ////////////////////////////////////////////////////////////////   CALCULATED PROPS ////////////////////////////////////////////////////////////

    @Test
    public void test_calculated_entity_props_in_condition() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).model());
	assertEquals("Incorrect count", 1, vehicles.size());
	final TgVehicle vehicle = vehicles.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
    }

    @Test
    public void test_calculated_entity_prop_in_fetching() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).with(fetch(TgVehicle.class).with("lastFuelUsage", fetchAll(TgFuelUsage.class))).model());
	assertEquals("Incorrect count", 1, vehicles.size());
	final TgVehicle vehicle = vehicles.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
	assertEquals("Incorrect key", "P", vehicle.getLastFuelUsage().getFuelType().getKey());
    }

    @Test
    public void test_calculated_entity_props_in_condition_() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100).yield().countAll().as("aa").modelAsAggregate();
	final List<EntityAggregates> vehicles = aggregateDao.getAllEntities(from(qry).model());
	assertEquals("Incorrect count", 1, vehicles.size());
//	final TgVehicle vehicle = vehicles.get(0);
//	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
    }

    @Test
    public void test_calculated_entity_props_in_condition2() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).leftJoin(TgFuelUsage.class).as("lastFuelUsage").on().prop("lastFuelUsage").eq().prop("lastFuelUsage.id").where().prop("lastFuelUsage.qty").gt().val(100).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).model());
	assertEquals("Incorrect count", 1, vehicles.size());
	final TgVehicle vehicle = vehicles.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());

    }

    @Test
    public void test_calculated_entity_props_in_condition2a() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).leftJoin(TgFuelUsage.class).as("lastFuelUsage").on().condition(cond().prop("lastFuelUsage").eq().prop("lastFuelUsage.id").model()).where().condition(cond().prop("lastFuelUsage.qty").gt().val(100).model()).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).model());
	assertEquals("Incorrect count", 1, vehicles.size());
	final TgVehicle vehicle = vehicles.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());

    }

    @Test
    public void test0_0() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("lastFuelUsageQty").as("lq").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
    	final EntityAggregates item = models.get(0);
	assertEquals("Incorrect key", new BigDecimal("120"), item.get("lq"));
    }

    @Test
    public void test0_0a() {
	final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("lastFuelUsageQty").eq().val(120).model();
	final AggregatedResultQueryModel model = select(vehSubqry).where().prop("key").eq().val("CAR2").yield().prop("lastFuelUsageQty").as("lq").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
    	final EntityAggregates item = models.get(0);
	assertEquals("Incorrect key", new BigDecimal("120"), item.get("lq"));
    }

    @Test
    public void test0_0b() {
	// FIXME
	final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("lastFuelUsageQty").eq().val(120).//
	yield().prop("key").as("key"). //
	yield().prop("id").as("id"). //
	modelAsEntity(TgVehicle.class);
	final AggregatedResultQueryModel model = select(vehSubqry).where().prop("key").eq().val("CAR2").yield().prop("lastFuelUsageQty").as("lq").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
    	final EntityAggregates item = models.get(0);
	assertEquals("Incorrect key", new BigDecimal("120"), item.get("lq"));
    }

    @Test
    public void test0_0c() {
	final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("lastFuelUsageQty").eq().val(120).//
	yield().prop("key").as("key"). //
	yield().prop("id").as("id"). //
	modelAsEntity(TgVehicle.class);
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(vehSubqry).model());
    	final TgVehicle item = models.get(0);
	assertEquals("Incorrect key", "CAR2", item.getKey());
    }

    @Test
    public void test0_1() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("sumOfPrices.amount").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).model());
    	final TgVehicle vehicle = models.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
	assertTrue("Values of props sumOfPrices [" + vehicle.getSumOfPrices().getAmount() + "] and calc0 [" + vehicle.getCalc0() + "] should be equal", vehicle.getSumOfPrices().getAmount().equals(vehicle.getCalc0()));
	assertEquals("Incorrect key", new Integer(30), vehicle.getConstValueProp());
    }

    @Test
    public void test0_2() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc2").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).model());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test0_4() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc3").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).model());
	assertEquals("Incorrect key", 1, models.size());
    }

    @Test
    public void test0_6() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc4").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).model());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test0_7() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc5").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).model());
    	final TgVehicle vehicle = models.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
    }

    @Test
    public void test0_8() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc6").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).model());
	assertEquals("Incorrect key", 0, models.size());
    }



    ////////////////////////////////////////////////////////////////   FUNCTIONS  ////////////////////////////////////////////////////////////

    @Test
    public void test_query_with_concat_function() {
	final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().concat().prop("wagon.key").with().val("-").with().prop("wagon.desc").end().eq().val("WAGON2-Wagon 2").model();
	final List<TgWagonSlot> models = wagonSlotDao.getAllEntities(from(qry).with(fetchAll(TgWagonSlot.class)).model());
	assertEquals("Incorrect key", 3, models.size());
    }

    @Test
    public void test_query_with_concat_function2() {
	final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().concat().ifNull().prop("wagon.key").then().val("NULL").with().val("-").with().prop("wagon.desc").end().eq().val("WAGON2-Wagon 2").model();
	final List<TgWagonSlot> models = wagonSlotDao.getAllEntities(from(qry).with(fetchAll(TgWagonSlot.class)).model());
	assertEquals("Incorrect key", 3, models.size());
    }

    @Test
    public void test_query_with_concat_function_with_non_string_argument() {
	final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().concat().prop("wagon.key").with().val(2).end().eq().val("WAGON22").model();
	final List<TgWagonSlot> models = wagonSlotDao.getAllEntities(from(qry).with(fetchAll(TgWagonSlot.class)).model());
	assertEquals("Incorrect key", 3, models.size());
    }

    @Test
    public void test_case_when_function() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --").then().prop("key").end().isNotNull().model();
        final IFunctionCompoundCondition0<IComparisonOperator0<TgVehicle>,TgVehicle> qry2 = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --");
        final EntityResultQueryModel<TgVehicle> qry3 = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --"). //
                then().prop("key").//
                when().prop("a").isNotNull().then().prop("a").end()
                .isNotNull().model();

        final EntityResultQueryModel<TgVehicle> qry4 = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --"). //
                then().prop("key").//
                when().prop("a").isNotNull().then().prop("a").otherwise().prop("a").end(). //
                isNotNull().model();

        final EntityResultQueryModel<TgVehicle> qry5 = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --"). //
                then().prop("key").//
                when().prop("a").isNotNull().then().prop("a").otherwise().prop("a").end(). //
                isNotNull().model();

        final EntityResultQueryModel<TgVehicle> qry6 = select(TgVehicle.class).where().caseWhen().prop("finDetails.capitalWorksNo").eq().val("x"). //
        then().prop("key").otherwise().prop("a").end(). //
        isNotNull().model();
    }


    ////////////////////////////////////////////////////////////////   OTHERS  ////////////////////////////////////////////////////////////

    @Test
    public void test_condition_on_121_property() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("finDetails.capitalWorksNo").eq().val("x'; DROP TABLE members; --").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry).with(fetch(TgVehicle.class)).model());
	assertEquals("Incorrect key", 0, models.size());
    }

    @Test
    public void test_fetching_of_121_property() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR1").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry).with(fetch(TgVehicle.class).with("finDetails")).model());
	assertEquals("Incorrect key", "CAP_NO1", models.get(0).getFinDetails().getCapitalWorksNo());
    }


    @Test
    public void test_validation_of_duplicate_yields() {
	final AggregatedResultQueryModel model = select(TgVehicle.class). //
		yield().prop("id").as("id"). //
		yield().prop("key").as("key"). //
		yield().prop("version").as("id"). //
		modelAsAggregate();
	try {
	    aggregateDao.getAllEntities(from(model).model());
	    fail("Should have failed while trying to yield duplicates");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_retrieval_of_non_persisted_prop_from_type() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class).where().prop("npProp").eq().val("val").model();
	try {
	    vehicleMakeDao.getAllEntities(from(qry).model());
	    fail("Should have failed while trying to resolve property [npProp]");
	} catch (final Exception e) {
	}
    }

    @Test
    public void test_retrieval_of_non_persisted_prop_from_model() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class). //
	yield().prop("id").as("id"). //
	yield().prop("version").as("version"). //
	yield().prop("key").as("key"). //
	yield().prop("desc").as("desc"). //
	yield().val("val").as("npProp"). //
	modelAsEntity(TgVehicleMake.class);
	assertEquals("Incorrect key", 4, vehicleMakeDao.getAllEntities(from(qry).model()).size());
    }

    @Test
    public void test_retrieval_of_non_persisted_entity_prop_from_model() {
	final EntityResultQueryModel<TgVehicleMake> makeQry = select(TgVehicleMake.class). //
		where().prop("key").eq().val("MERC"). //
	yield().prop("id").as("id"). //
	yield().prop("version").as("version"). //
	yield().prop("key").as("key"). //
	yield().prop("desc").as("desc"). //
	yield().beginExpr().val(vehicleMakeDao.findByKey("BMW")).add().val(1).sub().val(1).endExpr().as("competitor"). //
	modelAsEntity(TgVehicleMake.class);
	final EntityResultQueryModel<TgVehicleMake> qry = select(makeQry). //
		where().prop("competitor.key").eq().val("BMW").model();
	final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(qry).with(fetchAll(TgVehicleMake.class).with("competitor")).model());
	assertEquals("Incorrect size", 1, models.size());
	assertEquals("Incorrect key", "BMW", models.get(0).getCompetitor().getKey());
    }


    @Test
    public void test_query_with_virtual_property() {
	final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().prop("key").like().val("WAGON%1").model();
	final List<TgWagonSlot> models = wagonSlotDao.getAllEntities(from(qry).with(fetchAll(TgWagonSlot.class)).with(orderBy().prop("key").desc().model()).model());
	assertEquals("Incorrect key", 2, models.size());
	assertEquals("Incorrect key 1", "WAGON2", models.get(0).getWagon().getKey());
	assertEquals("Incorrect key 2", "1", models.get(0).getPosition().toString());
	assertEquals("Incorrect key 2", "WAGON2 1", models.get(0).getKey().toString());
    }


    @Test
    public void test_fetch_with_sorted_collection() {
	final EntityResultQueryModel<TgWagon> qry = select(TgWagon.class).where().prop("key").eq().val("WAGON1").model();
	final List<TgWagon> models = wagonDao.getAllEntities(from(qry).with(fetch(TgWagon.class).with("slots", fetch(TgWagonSlot.class).with("bogie"))).model());
	assertEquals("Incorrect key", 1, models.size());
	assertEquals("Incorrect key", 8, models.get(0).getSlots().size());
	assertEquals("Incorrect slot position", new Integer("1"), models.get(0).getSlots().iterator().next().getPosition());
	assertNotNull("Bogie should be present", models.get(0).getSlots().iterator().next().getBogie());
	assertEquals("Incorrect key", "BOGIE4", models.get(0).getSlots().iterator().next().getBogie().getKey());
    }

    @Test
    public void test_sql_injection() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("desc").eq().val("x'; DROP TABLE members; --").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry).with(fetch(TgVehicle.class)).model());
	assertEquals("Incorrect key", 0, models.size());
    }

    @Test
    public void test_yielding_const_value() {
	final AggregatedResultQueryModel makeModel = select(TgVehicleMake.class).where().prop("key").eq().val("MERC").yield().prop("key").as("key").yield().val("MERC").as("konst").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(makeModel).model());
	assertEquals("Incorrect key", 1, models.size());
     }

    @Test
    public void test_nested_uncorrelated_subqueries() {
	final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("model").eq().extProp("id").model();
	final EntityResultQueryModel<TgVehicleModel> vehModelSubqry = select(TgVehicleModel.class).where().prop("key").eq().val("316").and().exists(vehSubqry).model();
	final EntityResultQueryModel<TgVehicleMake> makeModel = select(TgVehicleMake.class).where().exists(vehModelSubqry).model();
	final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(makeModel).model());
	assertEquals("Incorrect key", 4, models.size());
     }

    @Test
    public void test_nested_subqueries_with_ext_props() {
	final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("model").eq().extProp("id").model();
	final EntityResultQueryModel<TgVehicleModel> vehModelSubqry = select(TgVehicleModel.class).where().prop("make").eq().extProp("id").and().exists(vehSubqry).model();
	final EntityResultQueryModel<TgVehicleMake> makeModel = select(TgVehicleMake.class).where().exists(vehModelSubqry).model();
	final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(makeModel).model());
	assertEquals("Incorrect key", 2, models.size());
     }


    @Test
    public void test_111() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("MERC", "AUDI").yield().val(1).as("1").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test_112() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("model.make.key").eq().val("MERC").or().prop("model.make.key").eq().val("AUDI").yield().val(1).as("1").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test_113() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().begin().prop("model.make.key").eq().val("MERC").or().prop("model.make.key").eq().val("AUDI").end().yield().val(1).as("1").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test0_3() {
	final EntityResultQueryModel<TgFuelUsage> model = select(TgFuelUsage.class).where().prop("vehicle.sumOfPrices").ge().val("100").model();
	final List<TgFuelUsage> models = fuelUsageDao.getAllEntities(from(model).with(fetch(TgFuelUsage.class)).model());
    	final TgFuelUsage fuelUsage = models.get(0);
	assertEquals("Incorrect key", "CAR2", fuelUsage.getVehicle().getKey());
    }

    @Test
    public void test0_5() {
	final EntityResultQueryModel<TgFuelUsage> model = select(TgFuelUsage.class).where().prop("vehicle.calc3").ge().val("100").model();
	final List<TgFuelUsage> models = fuelUsageDao.getAllEntities(from(model).with(fetch(TgFuelUsage.class)).model());
    	final TgFuelUsage fuelUsage = models.get(0);
	assertEquals("Incorrect key", "CAR2", fuelUsage.getVehicle().getKey());
    }

    @Test
    public void test1() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("key").eq().val("316").model();
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).model());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test2() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("key").eq().val("316"). //
		yield().prop("id").as("id").
		yield().prop("version").as("version").
		yield().prop("key").as("key").
		yield().prop("desc").as("desc").
		yield().prop("make").as("make").
		yield().prop("make.id").as("make.id").
		yield().prop("make.version").as("make.version").
		yield().prop("make.key").as("make.key").
		yield().prop("make.desc").as("make.desc").
		modelAsEntity(TgVehicleModel.class);
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).model());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    @Ignore
    public void test2_() {
        final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("key").eq().val("316"). //
                yield().prop("id").as("id").
                yield().prop("version").as("version").
                yield().prop("key").as("key").
                yield().prop("desc").as("desc").
                modelAsEntity(TgVehicleModel.class);
        final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(fetchAll(TgVehicleModel.class)).model());
            final TgVehicleModel vehModel = models.get(0);
        assertEquals("Incorrect key", "316", vehModel.getKey());
        assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test2__() {
        final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).//
                yield().prop("id").as("id").
                yield().prop("version").as("version").
                yield().prop("key").as("key").
                yield().prop("desc").as("desc").
                modelAsEntity(TgVehicleModel.class);

        final EntityResultQueryModel<TgVehicleModel> model2 = select(model).where().prop("key").eq().val("316"). //
        model();

        final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model2).with(fetchAll(TgVehicleModel.class)).model());
            final TgVehicleModel vehModel = models.get(0);
        assertEquals("Incorrect key", "316", vehModel.getKey());
    }

    @Test
    public void test2a() {
	final AggregatedResultQueryModel model = select(TgVehicleModel.class).where().prop("key").eq().val("316"). //
		yield().prop("id").as("id").
		yield().prop("version").as("version").
		yield().prop("key").as("key").
		yield().prop("desc").as("desc").
		yield().prop("make.key").as("make.key").
		yield().prop("make.desc").as("make.desc").
		modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).model());
    	final EntityAggregates vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.get("key"));
	assertEquals("Incorrect key", "MERC", ((EntityAggregates) vehModel.get("make")).get("key"));
    }

    @Test
    public void test_partial_fetching() {
	final EntityResultQueryModel<TgVehicle> model = select(select(TgVehicle.class).where().prop("key").eq().val("CAR1").model()). //
		yield().prop("id").as("id").
		yield().prop("version").as("version").
		yield().prop("key").as("key").
		yield().prop("desc").as("desc").
		yield().prop("model").as("model").
		yield().prop("model.id").as("model.id").
		yield().prop("model.version").as("model.version").
		yield().prop("model.key").as("model.key").
		yield().prop("model.desc").as("model.desc").
		yield().prop("model.make").as("model.make").
		modelAsEntity(TgVehicle.class);
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).with(fetch(TgVehicle.class).with("model", fetch(TgVehicleModel.class).with("make"))).model());
    	final TgVehicle vehModel = models.get(0);
	assertEquals("Incorrect key", "318", vehModel.getModel().getKey());
	assertEquals("Incorrect key", "AUDI", vehModel.getModel().getMake().getKey());
    }

    @Test
    public void test3() {
	final EntityResultQueryModel<TgVehicleModel> model = select(select(TgVehicleModel.class).where().prop("make.key").eq().val("MERC").model()). //
		yield().prop("id").as("id").
		yield().prop("version").as("version").
		yield().prop("key").as("key").
		yield().prop("desc").as("desc").
		yield().prop("make").as("make").
		yield().prop("make.id").as("make.id").
		yield().prop("make.version").as("make.version").
		yield().prop("make.key").as("make.key").
		yield().prop("make.desc").as("make.desc").
		modelAsEntity(TgVehicleModel.class);
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).model());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test4() {
        final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("make").eq().model(select(TgVehicleMake.class).where().prop("key").eq().val("MERC").yield().prop("id").modelAsPrimitive()). //
                modelAsEntity(TgVehicleModel.class);
        final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(fetch(TgVehicleModel.class).with("make")).model());
            final TgVehicleModel vehModel = models.get(0);
        assertEquals("Incorrect key", "316", vehModel.getKey());
        assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test5() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("make.key").eq().val("MERC"). //
		modelAsEntity(TgVehicleModel.class);
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(fetch(TgVehicleModel.class).with("make")).model());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test6() {
	final EntityResultQueryModel<TgVehicleMake> model = select(TgVehicleMake.class).where().prop("key").in().params("param1", "param2").model();
	final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(model).with("param1", "MERC").with("param2", "BMW").model());
    	assertEquals("Incorrect count", 2, models.size());
    }

    @Test
    public void test7() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).as("v").where().prop("v.key").in().values("CAR1", "CAR2").and().prop("v.price.amount").ge().val(100). //
		yield().prop("v.price.amount").as("pa").yield().prop("v.lastMeterReading").as("lmr").modelAsAggregate();
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
    	assertEquals("Incorrect count", 1, values.size());
//    	assertEquals("Incorrect value", new BigDecimal("105.75"), values.get(0).get("lmr"));
//    	assertEquals("Incorrect value", new BigDecimal("200.00"), values.get(0).get("pa"));
    }

    @Test
    public void test8() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("price.amount").ge().val(100).model();
	final List<TgVehicle> values = vehicleDao.getAllEntities(from(model).model());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect count", new Money("100.00"), values.get(0).getPurchasePrice());
    }

    @Test
    public void test8a() {
	final EntityResultQueryModel<EntityWithMoney> model = select(EntityWithMoney.class).where().prop("money").isNotNull().model();
	final List<EntityWithMoney> values = entityWithMoneyDao.getAllEntities(from(model).model());
    	assertEquals("Incorrect count", 0, values.size());
    }

    @Test
    public void test9() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).yield(). //
		avgOf().beginExpr().prop("price.amount").add().prop("purchasePrice.amount").endExpr().as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "165.0", values.get(0).get("aa").toString());
    }

    @Test
    public void test_all_quantified_condition() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().val(100).lt().all(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").yield().prop("qty").modelAsPrimitive()).model();
	final List<TgVehicle> values = vehicleDao.getAllEntities(from(model).model());
    	assertEquals("Incorrect count", 0, values.size());
    }

    @Test
    public void test_any_quantified_condition() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().val(100).lt().any(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").yield().prop("qty").modelAsPrimitive()).model();
	final List<TgVehicle> values = vehicleDao.getAllEntities(from(model).model());
    	assertEquals("Incorrect count", 1, values.size());
    }

    @Test
    public void test10() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).yield(). //
		beginExpr().avgOf().prop("price.amount").add().avgOf().prop("purchasePrice.amount").endExpr().as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "165.0", values.get(0).get("aa").toString());
    }

    @Test
    public void test11() {
	final AggregatedResultQueryModel model = select(TgVehicleModel.class).yield().countOfDistinct().prop("make").as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "3", values.get(0).get("aa").toString());
    }

    @Test
    public void test12() {
	final AggregatedResultQueryModel model = select(TgVehicleModel.class). //
		yield().countAll().as("aa"). //
		yield().countOfDistinct().prop("make").as("bb"). //
		yield().now().as("cc"). //
		modelAsAggregate();
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "7", values.get(0).get("aa").toString());
    	assertEquals("Incorrect value", "3", values.get(0).get("bb").toString());
    }

    @Test
    public void test13() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").model();
	final AggregatedResultQueryModel countModel = select(model). //
		yield().countAll().as("aa"). //
		modelAsAggregate();

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).model());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test13a() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("model.make.key").eq().val("MERC").yield().prop("id").as("aa").modelAsAggregate();
	final AggregatedResultQueryModel countModel = select(model). //
		yield().countAll().as("aa"). //
		modelAsAggregate();

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).model());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test14() {
	final PrimitiveResultQueryModel subQry = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").yield().prop("key").modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("key").in().model(subQry). //
		yield().countAll().as("aa"). //
		modelAsAggregate();

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).model());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test15_() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("model.make").eq().prop("make").model();

	try {
	    vehicleDao.getAllEntities(from(qry).model());
	    fail("Prop make should not be resolved and lead to exception");
	} catch (final RuntimeException e) {
	}

    }

    @Test
    public void test15() {
	final PrimitiveResultQueryModel subQry = select(TgVehicle.class).where().prop("model.make").eq().prop("make.id")/*extProp("id")*/.yield().countAll().modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicleMake.class).as("make").yield().prop("key").as("make"). //
	yield().model(subQry).as("vehicleCount"). //
	modelAsAggregate();

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).model());
	assertEquals("Incorrect count", 4, values.size());
	for (final EntityAggregates result : values) {
	    if (result.get("make").equals("MERC") || result.get("make").equals("AUDI")) {
		assertEquals("Incorrect value for make " + result.get("make"), "1", result.get("vehicleCount").toString());
	    } else {
		assertEquals("Incorrect value", "0", result.get("vehicleCount").toString());
	    }
	}
    }

    @Test
    public void test16() {
	final PrimitiveResultQueryModel makeQry = select(TgVehicleMake.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").yield().prop("key").modelAsPrimitive();
	final PrimitiveResultQueryModel modelQry = select(TgVehicleModel.class).where().prop("make.key").in().model(makeQry).yield().prop("key").modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("model.key").in().model(modelQry). //
	yield().countAll().as("aa"). //
	modelAsAggregate();

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).model());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test17() {
	final PrimitiveResultQueryModel makeQry = select(TgVehicleMake.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").and().prop("key").eq().prop("model.make.key").yield().prop("key").modelAsPrimitive();
	final PrimitiveResultQueryModel modelQry = select(TgVehicleModel.class).where().prop("make.key").in().model(makeQry).and().prop("key").eq().param("model_param").yield().prop("key").modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("model.key").in().model(modelQry). //
	yield().countAll().as("aa"). //
	modelAsAggregate();

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).with("model_param", "316").model());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test18() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
	final OrderingModel ordering = orderBy().prop("model.make.key").desc().model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry).with(ordering).model());
    	final TgVehicle veh = models.get(0);
	assertEquals("Incorrect key", "CAR2", veh.getKey());
    }

    @Test
    public void test19() {
	final AggregatedResultQueryModel model = select(TgVehicle.class). //
		where().prop("model.make.key").eq().val("MERC"). //
		and().prop("active").eq().val(false). //
		and().prop("leased").eq().val(true). //
		yield().lowerCase().prop("model.make.key").as("make").
		yield().ifNull().prop("replacedBy").then().val(1).as("not-replaced-yet").
		yield().ifNull().prop("model.make.key").then().val("unknown").as("make-key").
		yield().count().days().between().now().and().now().as("zero-days").
		yield().count().months().between().now().and().now().as("zero-months").
		yield().count().years().between().now().and().now().as("zero-years").
		yield().caseWhen().prop("price.amount").ge().prop("purchasePrice.amount").then().
		beginExpr().beginExpr().prop("price.amount").add().prop("purchasePrice.amount").endExpr().div().val(2).endExpr().end().as("avgPrice"). //
		yield().round().beginExpr().prop("price.amount").div().val(3).endExpr().to(1).as("third-of-price"). //

		modelAsAggregate();
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).model());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "merc", values.get(0).get("make"));
    	assertEquals("Incorrect value", "1", values.get(0).get("not-replaced-yet").toString());
    	assertEquals("Incorrect value", "MERC", values.get(0).get("make-key"));
    	assertEquals("Incorrect value", "0", values.get(0).get("zero-days").toString());
    	assertEquals("Incorrect value", "0", values.get(0).get("zero-months").toString());
    	assertEquals("Incorrect value", "0", values.get(0).get("zero-years").toString());
    	assertEquals("Incorrect value", "150", values.get(0).get("avgPrice").toString());
    	assertEquals("Incorrect value", "66.7", values.get(0).get("third-of-price").toString());
    }

    @Test
    public void test20() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class).where().prop("key").eq().val("MERC").model();
	final List<TgVehicleMake> makes = vehicleMakeDao.getAllEntities(from(qry).model());
    	final TgVehicleMake make = makes.get(0);

	final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make").eq().val(make).model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry2).model());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    public void test21() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class).where().prop("key").eq().val("MERC").model();
	final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make").eq().model(qry).model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry2).model());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    public void test22() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("model.make").modelAsEntity(TgVehicleMake.class);
	final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make").eq().model(qry).model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry2).model());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    @Ignore
    public void test22a() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("model.make").modelAsEntity(TgVehicleMake.class);
	final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(qry).with(fetch(TgVehicleMake.class)).model());
	assertEquals("Incorrect key", "AUDI", models.get(0).getKey());
    }

    @Test
    public void test23() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("model.make.key").iLike().val("me%").and().prop("key").iLike().val("%2").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry).model());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    public void test24() {
	final EntityResultQueryModel<UserAndRoleAssociation> model = select(UserAndRoleAssociation.class).where().prop("user.key").eq().val("user1").and().prop("userRole.key").eq().val("MANAGER").model();
	final List<UserAndRoleAssociation> entities = userAndRoleAssociationDao.getAllEntities(from(model).with(fetch(UserAndRoleAssociation.class)).model());
	assertEquals("Incorrect count", 1, entities.size());
	assertEquals("Incorrect user", "user1", entities.get(0).getUser().getKey());
    }

    @Test
    public void test_vehicle_fetching() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
	final fetch<TgVehicle> fetchModel = fetch(TgVehicle.class).with("model", fetch(TgVehicleModel.class).with("make"));
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).with(fetchModel).model());
	final TgVehicle vehicle = vehicles.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
	assertEquals("Incorrect key", "316", vehicle.getModel().getKey());
	assertEquals("Incorrect key", "MERC", vehicle.getModel().getMake().getKey());
    }

    @Test
    public void test_vehicle_with_collection_fetching() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
	final fetch<TgVehicle> fetchModel = fetch(TgVehicle.class).with("model", fetch(TgVehicleModel.class).with("make")).with("fuelUsages", fetch(TgFuelUsage.class));
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).with(fetchModel).model());
	final TgVehicle vehicle = vehicles.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
	assertEquals("Incorrect key", "316", vehicle.getModel().getKey());
	assertEquals("Incorrect key", "MERC", vehicle.getModel().getMake().getKey());
	assertEquals("Incorrect number of fuel-usages", 2, vehicle.getFuelUsages().size());
    }

    @Test
    public void test_aggregates_fetching() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("model").as("model").modelAsAggregate();
	final fetch<EntityAggregates> fetchModel = fetch(EntityAggregates.class).with("model", fetch(TgVehicleModel.class).with("make"));
	final EntityAggregates value = aggregateDao.getAllEntities(from(model).with(fetchModel).model()).get(0);
	assertEquals("Incorrect key", "316", ((TgVehicleModel) value.get("model")).getKey());
	assertEquals("Incorrect key", "MERC", ((TgVehicleModel) value.get("model")).getMake().getKey());
    }

    @Test
    public void test_aggregates_fetching_with_nullable_props() {
	final AggregatedResultQueryModel model = select(TgFuelUsage.class).yield().prop("vehicle.station").as("station").yield().sumOf().prop("qty").as("totalQty").modelAsAggregate();
	final fetch<EntityAggregates> fetchModel = fetch(EntityAggregates.class).with("station", fetch(TgOrgUnit5.class));
	final EntityAggregates value = aggregateDao.getAllEntities(from(model).with(fetchModel).model()).get(0);
	assertEquals("Incorrect key", "orgunit5", ((TgOrgUnit5) value.get("station")).getKey());
    }

    @Test
    public void test_parameter_setting() {
	final EntityResultQueryModel<TgVehicleMake> queryModel = select(TgVehicleMake.class).where().prop("key").eq().param("makeParam").model();

	assertEquals("Mercedes", vehicleMakeDao.getAllEntities(from(queryModel).with("makeParam", "MERC").model()).get(0).getDesc());
	assertEquals("Audi", vehicleMakeDao.getAllEntities(from(queryModel).with("makeParam", "AUDI").model()).get(0).getDesc());

//	try {
//	    vehicleMakeDao.getAllEntities(from(queryModel).with("wrongParam", "AUDI").model());
//	    fail("Setting param value with wrong param name should not lead to exception");
//	} catch (final RuntimeException e) {
//	}
    }

    @Test
    public void test_that_can_query_with_list_params() {
	final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param1", "param2", "param3").model();
	assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param1", "316").with("param2", "317").with("param3", "318").model()).size());
    }

    @Test
    public void test_that_can_query_with_primitive_boolean_params() {
	final EntityResultQueryModel<TgVehicle> queryModel = select(TgVehicle.class).where().prop("active").eq().param("param").model();
	assertEquals("Incorrect key.", 1, vehicleDao.getAllEntities(from(queryModel).with("param", true).model()).size());
    }

    @Test
    public void test_that_can_query_with_boolean_params() {
	final EntityResultQueryModel<TgVehicle> queryModel = select(TgVehicle.class).where().prop("active").eq().param("param").model();
	assertEquals("Incorrect key.", 1, vehicleDao.getAllEntities(from(queryModel).with("param", Boolean.TRUE).model()).size());
    }

    @Test
    public void test_that_can_query_with_entity_params() {
	final TgVehicleModel m316 = vehicleModelDao.findByKey("316");
	final EntityResultQueryModel<TgVehicle> queryModel = select(TgVehicle.class).where().prop("model").eq().param("param").model();
	assertEquals("Incorrect key.", 1, vehicleDao.getAllEntities(from(queryModel).with("param", m316).model()).size());
    }

    @Test
    public void test_that_query_count_model_works() {
	final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().values("316", "317", "318").model();
	assertEquals("Incorrect number of veh models.", 3, vehicleModelDao.count(queryModel));
    }

    @Test
    public void test_that_can_query_with_arrays() {
	final String[] modelKeys = new String[] { "316", "317", "318", "318" };
	final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().values(modelKeys).model();
	assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).model()).size());
    }

    @Test
    public void test_that_can_query_with_array_param() {
	final String[] modelKeys = new String[] { "316", "317", "318", "318" };
	final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param").model();
	assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).model()).size());
    }

    @Test
    public void test_that_can_query_with_list_param() {
	final List<String> modelKeys = Arrays.asList(new String[] { "316", "317", "318", "318" });
	final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param").model();
	assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).model()).size());
    }

    @Test
    public void test_mutiple_queries_as_query_source() {
	final EntityResultQueryModel<TgVehicleModel> sourceModel1 = select(TgVehicleModel.class).where().prop("key").eq().val("316").model();
	final EntityResultQueryModel<TgVehicleModel> sourceModel2 = select(TgVehicleModel.class).where().prop("key").eq().val("317").model();
	final EntityResultQueryModel<TgVehicleModel> model = select(sourceModel1, sourceModel2).where().prop("key").in().values("316", "317").model();
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(orderBy().prop("key").asc().model()).model());
	assertEquals("Incorrect key", "316", models.get(0).getKey());
	assertEquals("Incorrect key", "317", models.get(1).getKey());
    }

    @Test
    public void test_subqueries_in_yield_stmt() {
	final PrimitiveResultQueryModel avgPriceModel = select(TgVehicle.class).yield().avgOf().prop("price.amount").modelAsPrimitive();
	final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class).where().beginExpr().model(avgPriceModel).add().prop("price.amount").endExpr().ge().val(10).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(query).with(orderBy().prop("key").asc().model()).model());
	assertEquals("Incorrect key", "CAR1", vehicles.get(0).getKey());
	assertEquals("Incorrect key", "CAR2", vehicles.get(1).getKey());
    }

    @Test
    public void test_subqueries_in_yield_stmt2() {
	final PrimitiveResultQueryModel sumPriceModel = select(TgVehicle.class).yield().sumOf().prop("price.amount").modelAsPrimitive();
	final PrimitiveResultQueryModel avgPriceModel = select(TgVehicle.class).yield().beginExpr().avgOf().prop("price.amount").div().model(sumPriceModel).endExpr().modelAsPrimitive();
	final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class).where().beginExpr().model(avgPriceModel).add().prop("price.amount").endExpr().ge().val(10).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(query).with(orderBy().prop("key").asc().model()).model());
	assertEquals("Incorrect key", "CAR1", vehicles.get(0).getKey());
	assertEquals("Incorrect key", "CAR2", vehicles.get(1).getKey());
    }

    @Test
    public void test_subqueries_in_yield_stmt3() {
	final PrimitiveResultQueryModel sumPriceModel = select(TgVehicle.class).yield().sumOf().prop("price.amount").modelAsPrimitive();
	final PrimitiveResultQueryModel avgPriceModel = select(TgVehicle.class).yield().sumOf().model(sumPriceModel).modelAsPrimitive();
	final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class).where().beginExpr().model(avgPriceModel).add().prop("price.amount").endExpr().ge().val(10).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(query).with(orderBy().prop("key").asc().model()).model());
	assertEquals("Incorrect key", "CAR1", vehicles.get(0).getKey());
	assertEquals("Incorrect key", "CAR2", vehicles.get(1).getKey());
    }

    @Override
    protected void populateDomain() {
	final TgFuelType unleadedFuelType = save(new_(TgFuelType.class, "U", "Unleaded"));
	final TgFuelType petrolFuelType = save(new_(TgFuelType.class, "P", "Petrol"));

	final TgWorkshop workshop1 = save(new_(TgWorkshop.class, "WSHOP1", "Workshop 1"));
	final TgWorkshop workshop2 = save(new_(TgWorkshop.class, "WSHOP2", "Workshop 2"));


	final TgBogieLocation location = config.getEntityFactory().newEntity(TgBogieLocation.class);
	location.setWorkshop(workshop1);
	final TgBogie bogie1 = save(new_(TgBogie.class, "BOGIE1", "Bogie 1").setLocation(location));
	final TgBogie bogie2 = save(new_(TgBogie.class, "BOGIE2", "Bogie 2"));
	final TgBogie bogie3 = save(new_(TgBogie.class, "BOGIE3", "Bogie 3"));
	final TgBogie bogie4 = save(new_(TgBogie.class, "BOGIE4", "Bogie 4"));
	final TgBogie bogie5 = save(new_(TgBogie.class, "BOGIE5", "Bogie 5"));
	final TgBogie bogie6 = save(new_(TgBogie.class, "BOGIE6", "Bogie 6"));
	final TgBogie bogie7 = save(new_(TgBogie.class, "BOGIE7", "Bogie 7"));

	final TgWagon wagon1 = save(new_(TgWagon.class, "WAGON1", "Wagon 1"));
	final TgWagon wagon2 = save(new_(TgWagon.class, "WAGON2", "Wagon 2"));

	save(new_composite(TgWagonSlot.class, wagon1, 5));
	save(new_composite(TgWagonSlot.class, wagon1, 6));
	save(new_composite(TgWagonSlot.class, wagon1, 7));
	save(new_composite(TgWagonSlot.class, wagon1, 8));
	save(new_composite(TgWagonSlot.class, wagon1, 4).setBogie(bogie1));
	save(new_composite(TgWagonSlot.class, wagon1, 3).setBogie(bogie2));
	save(new_composite(TgWagonSlot.class, wagon1, 2).setBogie(bogie3));
	save(new_composite(TgWagonSlot.class, wagon1, 1).setBogie(bogie4));

	save(new_composite(TgWagonSlot.class, wagon2, 1).setBogie(bogie5));
	save(new_composite(TgWagonSlot.class, wagon2, 2).setBogie(bogie6));
	save(new_composite(TgWagonSlot.class, wagon2, 3).setBogie(bogie7));

	final TgOrgUnit1 orgUnit1 = save(new_(TgOrgUnit1.class, "orgunit1", "desc orgunit1"));
	final TgOrgUnit2 orgUnit2 = save(new_(TgOrgUnit2.class, "orgunit2", "desc orgunit2").setParent(orgUnit1));
	final TgOrgUnit3 orgUnit3 = save(new_(TgOrgUnit3.class, "orgunit3", "desc orgunit3").setParent(orgUnit2));
	final TgOrgUnit4 orgUnit4 = save(new_(TgOrgUnit4.class, "orgunit4", "desc orgunit4").setParent(orgUnit3));
	final TgOrgUnit5 orgUnit5 = save(new_(TgOrgUnit5.class, "orgunit5", "desc orgunit5").setParent(orgUnit4));

	final TgVehicleMake merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
	final TgVehicleMake audi = save(new_(TgVehicleMake.class, "AUDI", "Audi"));
	final TgVehicleMake bmw = save(new_(TgVehicleMake.class, "BMW", "BMW"));
	final TgVehicleMake subaro = save(new_(TgVehicleMake.class, "SUBARO", "Subaro"));

	final TgVehicleModel m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
	final TgVehicleModel m317 = save(new_(TgVehicleModel.class, "317", "317").setMake(audi));
	final TgVehicleModel m318 = save(new_(TgVehicleModel.class, "318", "318").setMake(audi));
	final TgVehicleModel m319 = save(new_(TgVehicleModel.class, "319", "319").setMake(bmw));
	final TgVehicleModel m320 = save(new_(TgVehicleModel.class, "320", "320").setMake(bmw));
	final TgVehicleModel m321 = save(new_(TgVehicleModel.class, "321", "321").setMake(bmw));
	final TgVehicleModel m322 = save(new_(TgVehicleModel.class, "322", "322").setMake(bmw));

	final TgVehicle car1 = save(new_(TgVehicle.class, "CAR1", "CAR1 DESC").setInitDate(date("2001-01-01 00:00:00")).setModel(m318).setPrice(new Money("20")).setPurchasePrice(new Money("10")).setActive(true).setLeased(false));
	final TgVehicle car2 = save(new_(TgVehicle.class, "CAR2", "CAR2 DESC").setInitDate(date("2007-01-01 00:00:00")).setModel(m316).setPrice(new Money("200")).setPurchasePrice(new Money("100")).setActive(false).setLeased(true).setLastMeterReading(new BigDecimal("105")).setStation(orgUnit5));

	save(new_(TgVehicleFinDetails.class, car1).setCapitalWorksNo("CAP_NO1"));

	save(new_composite(TgFuelUsage.class, car2, date("2006-02-09 00:00:00")).setQty(new BigDecimal("100")).setFuelType(unleadedFuelType));
	save(new_composite(TgFuelUsage.class, car2, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")).setFuelType(petrolFuelType));

	save(new_composite(TgTimesheet.class, "USER1", date("2011-11-01 13:00:00")).setFinishDate(date("2011-11-01 15:00:00")).setIncident("002"));

	final UserRole managerRole = save(new_(UserRole.class, "MANAGER", "Managerial role"));
	final UserRole dataEntryRole = save(new_(UserRole.class, "DATAENTRY", "Data entry role"));
	final UserRole analyticRole = save(new_(UserRole.class, "ANALYTIC", "Analytic role"));
	final UserRole fleetOperatorRole = save(new_(UserRole.class, "FLEET_OPERATOR", "Fleet operator role"));
	final UserRole workshopOperatorRole = save(new_(UserRole.class, "WORKSHOP_OPERATOR", "Workshop operator role"));
	final UserRole warehouseOperatorRole = save(new_(UserRole.class, "WAREHOUSE_OPERATOR", "Warehouse operator role"));

	final User baseUser1 = save(new_(User.class, "base_user1", "base user1").setBase(true).setPassword("password1"));
	final User user1 = save(new_(User.class, "user1", "user1 desc").setBase(false).setBasedOnUser(baseUser1).setPassword("password1"));
	final User user2 = save(new_(User.class, "user2", "user2 desc").setBase(false).setBasedOnUser(baseUser1).setPassword("password1"));
	final User user3 = save(new_(User.class, "user3", "user3 desc").setBase(false).setBasedOnUser(baseUser1).setPassword("password1"));

	save(new_composite(UserAndRoleAssociation.class, user1, managerRole));
	save(new_composite(UserAndRoleAssociation.class, user1, analyticRole));
	save(new_composite(UserAndRoleAssociation.class, user2, dataEntryRole));
	save(new_composite(UserAndRoleAssociation.class, user2, fleetOperatorRole));
	save(new_composite(UserAndRoleAssociation.class, user2, warehouseOperatorRole));
	save(new_composite(UserAndRoleAssociation.class, user3, dataEntryRole));
	save(new_composite(UserAndRoleAssociation.class, user3, fleetOperatorRole));
	save(new_composite(UserAndRoleAssociation.class, user3, warehouseOperatorRole));

	System.out.println("\n   DATA POPULATED SUCCESSFULLY\n\n\n\n\n\n\n\n\n");
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
	return PlatformTestDomainTypes.entityTypes;
    }
}