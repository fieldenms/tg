package ua.com.fielden.platform.entity.query.fetching;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.ISecurityRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgBogieLocation;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
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
import ua.com.fielden.platform.sample.domain.controller.ITgBogie;
import ua.com.fielden.platform.sample.domain.controller.ITgFuelUsage;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicle;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.controller.ITgWagon;
import ua.com.fielden.platform.sample.domain.controller.ITgWagonSlot;
import ua.com.fielden.platform.sample.domain.controller.ITgWorkshop;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Money;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class EntityQueryExecutionTest extends AbstractDomainDrivenTestCase {

    private final ITgBogie bogieDao = getInstance(ITgBogie.class);
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


    @Test
    public void test_query_with_union_property() {
	final EntityResultQueryModel<TgBogie> qry = select(TgBogie.class).where().prop("location.workshop.key").eq().val("WSHOP1").or().prop("location.wagonSlot.wagon.key").eq().val("WAGON1").model();
	final List<TgBogie> models = bogieDao.getAllEntities(from(qry).with(fetchAll(TgBogie.class).with("location", fetch(TgBogieLocation.class).with("workshop"))).build()); //.without("location").without("location.wagonSlot").without("location.workshop")
	assertEquals("Incorrect key 1", "BOGIE1", models.get(0).getKey());
	assertEquals("Incorrect key 1", "WSHOP1", models.get(0).getLocation().getWorkshop().getKey());
    }

    @Test
    public void test_query_with_union_property1() {
	final EntityResultQueryModel<TgBogie> qry = select(select(TgBogie.class).model()).where().prop("location.workshop.key").eq().val("WSHOP1").or().prop("location.wagonSlot.wagon.key").eq().val("WAGON1").model();
	final List<TgBogie> models = bogieDao.getAllEntities(from(qry).build());
	assertEquals("Incorrect key 1", "BOGIE1", models.get(0).getKey());
    }

    @Test
    @Ignore
    public void test_query_with_union_property2() {
	final EntityResultQueryModel<TgWorkshop> qry = select(select(TgBogie.class).model()).where().prop("location.workshop.key").eq().val("WSHOP1").yield().prop("location.workshop").modelAsEntity(TgWorkshop.class);
	final List<TgWorkshop> models = workshopDao.getAllEntities(from(qry).with(fetch(TgWorkshop.class)).build());
	assertEquals("Incorrect key 1", "WSHOP1", models.get(0).getKey());
    }

    @Test
    public void test_query_with_virtual_property() {
	final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().prop("key").like().val("WAGON%1").model();
	final List<TgWagonSlot> models = wagonSlotDao.getAllEntities(from(qry).with(fetchAll(TgWagonSlot.class)).with(orderBy().prop("key").desc().model()).build());
	assertEquals("Incorrect key", 2, models.size());
	assertEquals("Incorrect key 1", "WAGON2", models.get(0).getWagon().getKey());
	assertEquals("Incorrect key 2", "1", models.get(0).getPosition().toString());
	assertEquals("Incorrect key 2", "WAGON2 1", models.get(0).getKey().toString());
    }

    @Test
    public void test_query_with_concat_function() {
	final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().concat().prop("wagon.key").with().val("-").with().prop("wagon.desc").end().eq().val("WAGON2-Wagon 2").model();
	final List<TgWagonSlot> models = wagonSlotDao.getAllEntities(from(qry).with(fetchAll(TgWagonSlot.class)).build());
	assertEquals("Incorrect key", 3, models.size());
    }

    @Test
    public void test_query_with_concat_function_with_non_string_argument() {
	final EntityResultQueryModel<TgWagonSlot> qry = select(TgWagonSlot.class).where().concat().prop("wagon.key").with().val(2).end().eq().val("WAGON22").model();
	final List<TgWagonSlot> models = wagonSlotDao.getAllEntities(from(qry).with(fetchAll(TgWagonSlot.class)).build());
	assertEquals("Incorrect key", 3, models.size());
    }

    @Test
    public void test_fetch_with_sorted_collection() {
	final EntityResultQueryModel<TgWagon> qry = select(TgWagon.class).where().prop("key").eq().val("WAGON1").model();
	final List<TgWagon> models = wagonDao.getAllEntities(from(qry).with(fetch(TgWagon.class).with("slots", fetch(TgWagonSlot.class).with("bogie"))).build());
	assertEquals("Incorrect key", 1, models.size());
	assertEquals("Incorrect key", 8, models.get(0).getSlots().size());
	assertEquals("Incorrect slot position", new Integer("1"), models.get(0).getSlots().iterator().next().getPosition());
	assertNotNull("Bogie should be present", models.get(0).getSlots().iterator().next().getBogie());
	assertEquals("Incorrect key", "BOGIE4", models.get(0).getSlots().iterator().next().getBogie().getKey());
    }

    @Test
    public void test_sql_injection() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("desc").eq().val("x'; DROP TABLE members; --").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry).with(fetch(TgVehicle.class)).build());
	assertEquals("Incorrect key", 0, models.size());
    }

    @Test
    public void test_yielding_const_value() {
	final AggregatedResultQueryModel makeModel = select(TgVehicleMake.class).where().prop("key").eq().val("MERC").yield().prop("key").as("key").yield().val("MERC").as("konst").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(makeModel).build());
	assertEquals("Incorrect key", 1, models.size());
     }

    @Test
    public void test_nested_uncorrelated_subqueries() {
	final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("model").eq().extProp("id").model();
	final EntityResultQueryModel<TgVehicleModel> vehModelSubqry = select(TgVehicleModel.class).where().prop("key").eq().val("316").and().exists(vehSubqry).model();
	final EntityResultQueryModel<TgVehicleMake> makeModel = select(TgVehicleMake.class).where().exists(vehModelSubqry).model();
	final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(makeModel).build());
	assertEquals("Incorrect key", 4, models.size());
     }

    @Test
    public void test_nested_subqueries_with_ext_props() {
	final EntityResultQueryModel<TgVehicle> vehSubqry = select(TgVehicle.class).where().prop("model").eq().extProp("id").model();
	final EntityResultQueryModel<TgVehicleModel> vehModelSubqry = select(TgVehicleModel.class).where().prop("make").eq().extProp("id").and().exists(vehSubqry).model();
	final EntityResultQueryModel<TgVehicleMake> makeModel = select(TgVehicleMake.class).where().exists(vehModelSubqry).model();
	final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(makeModel).build());
	assertEquals("Incorrect key", 2, models.size());
     }

    @Test
    public void test0_0() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("lastFuelUsageQty").as("lq").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).build());
    	final EntityAggregates item = models.get(0);
	assertEquals("Incorrect key", new BigDecimal("120"), item.get("lq"));
    }

    @Test
    public void test0_1() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("sumOfPrices").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).build());
    	final TgVehicle vehicle = models.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
	assertTrue("Values of props sumOfPrices [" + vehicle.getSumOfPrices() + "] and calc0 [" + vehicle.getCalc0() + "] should be equal", vehicle.getSumOfPrices().equals(vehicle.getCalc0()));
	assertEquals("Incorrect key", new Integer(30), vehicle.getConstValueProp());
    }

    @Test
    public void test0_2() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc2").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).build());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test0_4() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc3").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).build());
	assertEquals("Incorrect key", 1, models.size());
    }

    @Test
    public void test0_6() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc4").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).build());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test0_7() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc5").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).build());
    	final TgVehicle vehicle = models.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
    }

    @Test
    public void test0_8() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("calc6").ge().val("100").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).build());
	assertEquals("Incorrect key", 0, models.size());
    }

    @Test
    public void test_111() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("MERC", "AUDI").yield().val(1).as("1").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).build());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test_112() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("model.make.key").eq().val("MERC").or().prop("model.make.key").eq().val("AUDI").yield().val(1).as("1").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).build());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test_113() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().begin().prop("model.make.key").eq().val("MERC").or().prop("model.make.key").eq().val("AUDI").end().yield().val(1).as("1").modelAsAggregate();
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).build());
	assertEquals("Incorrect key", 2, models.size());
    }

    @Test
    public void test0_3() {
	final EntityResultQueryModel<TgFuelUsage> model = select(TgFuelUsage.class).where().prop("vehicle.sumOfPrices").ge().val("100").model();
	final List<TgFuelUsage> models = fuelUsageDao.getAllEntities(from(model).with(fetch(TgFuelUsage.class)).build());
    	final TgFuelUsage fuelUsage = models.get(0);
	assertEquals("Incorrect key", "CAR2", fuelUsage.getVehicle().getKey());
    }

    @Test
    public void test0_5() {
	final EntityResultQueryModel<TgFuelUsage> model = select(TgFuelUsage.class).where().prop("vehicle.calc3").ge().val("100").model();
	final List<TgFuelUsage> models = fuelUsageDao.getAllEntities(from(model).with(fetch(TgFuelUsage.class)).build());
    	final TgFuelUsage fuelUsage = models.get(0);
	assertEquals("Incorrect key", "CAR2", fuelUsage.getVehicle().getKey());
    }

    @Test
    public void test1() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("key").eq().val("316").model();
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).build());
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
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).build());
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
        final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(fetchAll(TgVehicleModel.class)).build());
            final TgVehicleModel vehModel = models.get(0);
        assertEquals("Incorrect key", "316", vehModel.getKey());
        assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
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
	final List<EntityAggregates> models = aggregateDao.getAllEntities(from(model).build());
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
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(model).with(fetch(TgVehicle.class).with("model", fetch(TgVehicleModel.class).with("make"))).build());
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
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).build());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test4() {
        final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("make").eq().model(select(TgVehicleMake.class).where().prop("key").eq().val("MERC").yield().prop("id").modelAsPrimitive()). //
                modelAsEntity(TgVehicleModel.class);
        final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(fetch(TgVehicleModel.class).with("make")).build());
            final TgVehicleModel vehModel = models.get(0);
        assertEquals("Incorrect key", "316", vehModel.getKey());
        assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test5() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("make.key").eq().val("MERC"). //
		modelAsEntity(TgVehicleModel.class);
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(fetch(TgVehicleModel.class).with("make")).build());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test6() {
	final EntityResultQueryModel<TgVehicleMake> model = select(TgVehicleMake.class).where().prop("key").in().params("param1", "param2").model();
	final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(model).with("param1", "MERC").with("param2", "BMW").build());
    	assertEquals("Incorrect count", 2, models.size());
    }

    @Test
    public void test7() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).as("v").where().prop("v.key").in().values("CAR1", "CAR2").and().prop("v.price.amount").ge().val(100). //
		yield().prop("v.price.amount").as("pa").yield().prop("v.lastMeterReading").as("lmr").modelAsAggregate();
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
//    	assertEquals("Incorrect value", new BigDecimal("105.75"), values.get(0).get("lmr"));
//    	assertEquals("Incorrect value", new BigDecimal("200.00"), values.get(0).get("pa"));
    }

    @Test
    public void test8() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("price.amount").ge().val(100).model();
	final List<TgVehicle> values = vehicleDao.getAllEntities(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect count", new Money("100.00"), values.get(0).getPurchasePrice());
    }

    @Test
    public void test8a() {
	final EntityResultQueryModel<EntityWithMoney> model = select(EntityWithMoney.class).where().prop("money").isNotNull().model();
	final List<EntityWithMoney> values = entityWithMoneyDao.getAllEntities(from(model).build());
    	assertEquals("Incorrect count", 0, values.size());
    }

    @Test
    public void test9() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).yield(). //
		avgOf().beginExpr().prop("price.amount").add().prop("purchasePrice.amount").endExpr().as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", new BigDecimal("165"), values.get(0).get("aa"));
    }

    @Test
    public void test_all_quantified_condition() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().val(100).lt().all(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").yield().prop("qty").modelAsPrimitive()).model();
	final List<TgVehicle> values = vehicleDao.getAllEntities(from(model).build());
    	assertEquals("Incorrect count", 0, values.size());
    }

    @Test
    public void test_any_quantified_condition() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().val(100).lt().any(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").yield().prop("qty").modelAsPrimitive()).model();
	final List<TgVehicle> values = vehicleDao.getAllEntities(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    }

    @Test
    public void test10() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).yield(). //
		beginExpr().avgOf().prop("price.amount").add().avgOf().prop("purchasePrice.amount").endExpr().as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", new BigDecimal("165"), values.get(0).get("aa"));
    }

    @Test
    public void test11() {
	final AggregatedResultQueryModel model = select(TgVehicleModel.class).yield().countOfDistinct().prop("make").as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).build());
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
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).build());
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

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test13a() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("model.make.key").eq().val("MERC").yield().prop("id").as("aa").modelAsAggregate();
	final AggregatedResultQueryModel countModel = select(model). //
		yield().countAll().as("aa"). //
		modelAsAggregate();

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test14() {
	final PrimitiveResultQueryModel subQry = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").yield().prop("key").modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("key").in().model(subQry). //
		yield().countAll().as("aa"). //
		modelAsAggregate();

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test15_() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("model.make").eq().prop("make").model();

	try {
	    vehicleDao.getAllEntities(from(qry).build());
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

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).build());
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

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).build());
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

	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(countModel).with("model_param", "316").build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test18() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
	final OrderingModel ordering = orderBy().prop("model.make.key").desc().model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry).with(ordering).build());
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
		yield().countDays().between().now().and().now().as("zero-days").
		yield().caseWhen().prop("price.amount").ge().prop("purchasePrice.amount").then().
		beginExpr().beginExpr().prop("price.amount").add().prop("purchasePrice.amount").endExpr().div().val(2).endExpr().end().as("avgPrice"). //
		yield().round().beginExpr().prop("price.amount").div().val(3).endExpr().to(1).as("third-of-price"). //

		modelAsAggregate();
	final List<EntityAggregates> values = aggregateDao.getAllEntities(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "merc", values.get(0).get("make"));
    	assertEquals("Incorrect value", "1", values.get(0).get("not-replaced-yet").toString());
    	assertEquals("Incorrect value", "MERC", values.get(0).get("make-key"));
    	assertEquals("Incorrect value", "0", values.get(0).get("zero-days").toString());
    	assertEquals("Incorrect value", "150", values.get(0).get("avgPrice").toString());
    	assertEquals("Incorrect value", "66.7", values.get(0).get("third-of-price").toString());
    }

    @Test
    public void test20() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class).where().prop("key").eq().val("MERC").model();
	final List<TgVehicleMake> makes = vehicleMakeDao.getAllEntities(from(qry).build());
    	final TgVehicleMake make = makes.get(0);

	final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make").eq().val(make).model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry2).build());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    public void test21() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class).where().prop("key").eq().val("MERC").model();
	final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make").eq().model(qry).model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry2).build());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    public void test22() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("model.make").modelAsEntity(TgVehicleMake.class);
	final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make").eq().model(qry).model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry2).build());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    @Ignore
    public void test22a() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("model.make").modelAsEntity(TgVehicleMake.class);
	final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(qry).with(fetch(TgVehicleMake.class)).build());
	assertEquals("Incorrect key", "AUDI", models.get(0).getKey());
    }

    @Test
    public void test23() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("model.make.key").iLike().val("me%").and().prop("key").iLike().val("%2").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry).build());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    public void test24() {
	final EntityResultQueryModel<UserAndRoleAssociation> model = select(UserAndRoleAssociation.class).where().prop("user.key").eq().val("user1").and().prop("userRole.key").eq().val("MANAGER").model();
	final List<UserAndRoleAssociation> entities = userAndRoleAssociationDao.getAllEntities(from(model).with(fetch(UserAndRoleAssociation.class)).build());
	assertEquals("Incorrect count", 1, entities.size());
	assertEquals("Incorrect user", "user1", entities.get(0).getUser().getKey());
    }

    @Test
    public void test_vehicle_fetching() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
	final fetch<TgVehicle> fetchModel = fetch(TgVehicle.class).with("model", fetch(TgVehicleModel.class).with("make"));
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).with(fetchModel).build());
	final TgVehicle vehicle = vehicles.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
	assertEquals("Incorrect key", "316", vehicle.getModel().getKey());
	assertEquals("Incorrect key", "MERC", vehicle.getModel().getMake().getKey());
    }

    @Test
    public void test_vehicle_with_collection_fetching() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
	final fetch<TgVehicle> fetchModel = fetch(TgVehicle.class).with("model", fetch(TgVehicleModel.class).with("make")).with("fuelUsages", fetch(TgFuelUsage.class));
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).with(fetchModel).build());
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
	final EntityAggregates value = aggregateDao.getAllEntities(from(model).with(fetchModel).build()).get(0);
	assertEquals("Incorrect key", "316", ((TgVehicleModel) value.get("model")).getKey());
	assertEquals("Incorrect key", "MERC", ((TgVehicleModel) value.get("model")).getMake().getKey());
    }

    @Test
    public void test_aggregates_fetching_with_nullable_props() {
	final AggregatedResultQueryModel model = select(TgFuelUsage.class).yield().prop("vehicle.station").as("station").yield().sumOf().prop("qty").as("totalQty").modelAsAggregate();
	final fetch<EntityAggregates> fetchModel = fetch(EntityAggregates.class).with("station", fetch(TgOrgUnit5.class));
	final EntityAggregates value = aggregateDao.getAllEntities(from(model).with(fetchModel).build()).get(0);
	assertEquals("Incorrect key", "orgunit5", ((TgOrgUnit5) value.get("station")).getKey());
    }

    @Test
    public void test_calculated_entity_props_in_condition() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).build());
	assertEquals("Incorrect count", 1, vehicles.size());
	final TgVehicle vehicle = vehicles.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());

    }

    @Test
    public void test_calculated_entity_props_in_condition_() {
	final AggregatedResultQueryModel qry = select(TgVehicle.class).where().prop("lastFuelUsage.qty").gt().val(100).yield().countAll().as("aa").modelAsAggregate();
	final List<EntityAggregates> vehicles = aggregateDao.getAllEntities(from(qry).build());
	assertEquals("Incorrect count", 1, vehicles.size());
//	final TgVehicle vehicle = vehicles.get(0);
//	assertEquals("Incorrect key", "CAR2", vehicle.getKey());

    }

    @Test
    public void test_calculated_entity_props_in_condition2() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).leftJoin(TgFuelUsage.class).as("lastFuelUsage").on().prop("lastFuelUsage").eq().prop("lastFuelUsage.id").where().prop("lastFuelUsage.qty").gt().val(100).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).build());
	assertEquals("Incorrect count", 1, vehicles.size());
	final TgVehicle vehicle = vehicles.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());

    }


    @Test
    public void test_parameter_setting() {
	final EntityResultQueryModel<TgVehicleMake> queryModel = select(TgVehicleMake.class).where().prop("key").eq().param("makeParam").model();

	assertEquals("Mercedes", vehicleMakeDao.getAllEntities(from(queryModel).with("makeParam", "MERC").build()).get(0).getDesc());
	assertEquals("Audi", vehicleMakeDao.getAllEntities(from(queryModel).with("makeParam", "AUDI").build()).get(0).getDesc());

	try {
	    vehicleMakeDao.getAllEntities(from(queryModel).with("wrongParam", "AUDI").build());
	    fail("Setting param value with wrong param name should not lead to exception");
	} catch (final RuntimeException e) {
	}
    }

    @Test
    public void test_that_can_query_with_list_params() {
	final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param1", "param2", "param3").model();
	assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param1", "316").with("param2", "317").with("param3", "318").build()).size());
    }

    @Test
    public void test_that_can_query_with_primitive_boolean_params() {
	final EntityResultQueryModel<TgVehicle> queryModel = select(TgVehicle.class).where().prop("active").eq().param("param").model();
	assertEquals("Incorrect key.", 1, vehicleDao.getAllEntities(from(queryModel).with("param", true).build()).size());
    }

    @Test
    public void test_that_can_query_with_boolean_params() {
	final EntityResultQueryModel<TgVehicle> queryModel = select(TgVehicle.class).where().prop("active").eq().param("param").model();
	assertEquals("Incorrect key.", 1, vehicleDao.getAllEntities(from(queryModel).with("param", Boolean.TRUE).build()).size());
    }

    @Test
    public void test_that_can_query_with_entity_params() {
	final TgVehicleModel m316 = vehicleModelDao.findByKey("316");
	final EntityResultQueryModel<TgVehicle> queryModel = select(TgVehicle.class).where().prop("model").eq().param("param").model();
	assertEquals("Incorrect key.", 1, vehicleDao.getAllEntities(from(queryModel).with("param", m316).build()).size());
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
	assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).build()).size());
    }

    @Test
    public void test_that_can_query_with_array_param() {
	final String[] modelKeys = new String[] { "316", "317", "318", "318" };
	final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param").model();
	assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).build()).size());
    }

    @Test
    public void test_that_can_query_with_list_param() {
	final List<String> modelKeys = Arrays.asList(new String[] { "316", "317", "318", "318" });
	final EntityResultQueryModel<TgVehicleModel> queryModel = select(TgVehicleModel.class).where().prop("key").in().params("param").model();
	assertEquals("Incorrect number of retrieved veh models.", 3, vehicleModelDao.getAllEntities(from(queryModel).with("param", modelKeys).build()).size());
    }

    @Test
    public void test_mutiple_queries_as_query_source() {
	final EntityResultQueryModel<TgVehicleModel> sourceModel1 = select(TgVehicleModel.class).where().prop("key").eq().val("316").model();
	final EntityResultQueryModel<TgVehicleModel> sourceModel2 = select(TgVehicleModel.class).where().prop("key").eq().val("317").model();
	final EntityResultQueryModel<TgVehicleModel> model = select(sourceModel1, sourceModel2).where().prop("key").in().values("316", "317").model();
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(orderBy().prop("key").asc().model()).build());
	assertEquals("Incorrect key", "316", models.get(0).getKey());
	assertEquals("Incorrect key", "317", models.get(1).getKey());
    }

    @Test
    public void test_subqueries_in_yield_stmt() {
	final PrimitiveResultQueryModel avgPriceModel = select(TgVehicle.class).yield().avgOf().prop("price.amount").modelAsPrimitive();
	final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class).where().beginExpr().model(avgPriceModel).add().prop("price.amount").endExpr().ge().val(10).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(query).with(orderBy().prop("key").asc().model()).build());
	assertEquals("Incorrect key", "CAR1", vehicles.get(0).getKey());
	assertEquals("Incorrect key", "CAR2", vehicles.get(1).getKey());
    }

    @Test
    public void test_subqueries_in_yield_stmt2() {
	final PrimitiveResultQueryModel sumPriceModel = select(TgVehicle.class).yield().sumOf().prop("price.amount").modelAsPrimitive();
	final PrimitiveResultQueryModel avgPriceModel = select(TgVehicle.class).yield().beginExpr().avgOf().prop("price.amount").div().model(sumPriceModel).endExpr().modelAsPrimitive();
	final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class).where().beginExpr().model(avgPriceModel).add().prop("price.amount").endExpr().ge().val(10).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(query).with(orderBy().prop("key").asc().model()).build());
	assertEquals("Incorrect key", "CAR1", vehicles.get(0).getKey());
	assertEquals("Incorrect key", "CAR2", vehicles.get(1).getKey());
    }

    @Test
    public void test_subqueries_in_yield_stmt3() {
	final PrimitiveResultQueryModel sumPriceModel = select(TgVehicle.class).yield().sumOf().prop("price.amount").modelAsPrimitive();
	final PrimitiveResultQueryModel avgPriceModel = select(TgVehicle.class).yield().sumOf().model(sumPriceModel).modelAsPrimitive();
	final EntityResultQueryModel<TgVehicle> query = select(TgVehicle.class).where().beginExpr().model(avgPriceModel).add().prop("price.amount").endExpr().ge().val(10).model();
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(query).with(orderBy().prop("key").asc().model()).build());
	assertEquals("Incorrect key", "CAR1", vehicles.get(0).getKey());
	assertEquals("Incorrect key", "CAR2", vehicles.get(1).getKey());
    }

    @Override
    protected void populateDomain() {
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

	save(new_composite(TgFuelUsage.class, car2, date("2006-02-09 00:00:00")).setQty(new BigDecimal("100")));
	save(new_composite(TgFuelUsage.class, car2, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")));

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