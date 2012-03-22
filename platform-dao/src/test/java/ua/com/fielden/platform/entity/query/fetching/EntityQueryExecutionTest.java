package ua.com.fielden.platform.entity.query.fetching;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.dao2.IEntityAggregatesDao2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgTimesheet;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicle;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake2;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel2;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Money;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class EntityQueryExecutionTest extends AbstractDomainDrivenTestCase {

    private final ITgVehicleModel2 vehicleModelDao = getInstance(ITgVehicleModel2.class);
    private final ITgVehicleMake2 vehicleMakeDao = getInstance(ITgVehicleMake2.class);
    private final ITgVehicle vehicleDao = getInstance(ITgVehicle.class);
    private final IEntityAggregatesDao2 aggregatesDao = getInstance(IEntityAggregatesDao2.class);

    @Test
    public void test_vehicle_model_retrieval0() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("key").eq().val("316").model();
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).build());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", null, vehModel.getMake());
    }

    @Test
    public void test_vehicle_model_retrieval() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("key").eq().val("316"). //
		yield().prop("id").as("id").
		yield().prop("version").as("version").
		yield().prop("key").as("key").
		yield().prop("desc").as("desc").
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
    public void test_vehicle_model_retrieval2() {
	final EntityResultQueryModel<TgVehicleModel> model = select(select(TgVehicleModel.class).where().prop("make.key").eq().val("MERC").model()). //
		yield().prop("id").as("id").
		yield().prop("version").as("version").
		yield().prop("key").as("key").
		yield().prop("desc").as("desc").
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

//    @Test
//    public void test_vehicle_model_retrieval3() {
//	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("make").eq().model(select(TgVehicleMake.class).where().prop("key").eq().val("MERC").model()). //
//		modelAsEntity(TgVehicleModel.class);
//	final List<TgVehicleModel> models = dao.getAllEntities(from(model).with(new fetch<TgVehicleModel>(TgVehicleModel.class).with("make")).build());
//    	final TgVehicleModel vehModel = models.get(0);
//	assertEquals("Incorrect key", "316", vehModel.getKey());
//	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
//    }

    @Test
    public void test_vehicle_model_retrieval3() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("make.key").eq().val("MERC"). //
		modelAsEntity(TgVehicleModel.class);
	final List<TgVehicleModel> models = vehicleModelDao.getAllEntities(from(model).with(new fetch<TgVehicleModel>(TgVehicleModel.class).with("make")).build());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Test
    public void test_vehicle_model_retrieval4() {
	final EntityResultQueryModel<TgVehicleMake> model = select(TgVehicleMake.class).where().prop("key").in().params("param1", "param2").model();
	final List<TgVehicleMake> models = vehicleMakeDao.getAllEntities(from(model).with("param1", "MERC").with("param2", "BMW").build());
    	assertEquals("Incorrect count", 2, models.size());
    }

    public void test_vehicle_model_retrieval5() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).as("v").where().prop("v").in().values(1, 2).and().prop("v.price.amount").ge().val(100).yield().prop("v.price.amount").as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregatesDao.getAggregates(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", new BigDecimal("200.00"), values.get(0).get("aa"));
    }

    @Test
    public void test_vehicle_model_retrieval6() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("price.amount").ge().val(100).model();
	final List<TgVehicle> values = vehicleDao.getAllEntities(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect count", new Money("100.00"), values.get(0).getPurchasePrice());
    }

    public void test_vehicle_model_retrieval7() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).yield(). //
		avgOf().beginExpr().prop("price.amount").add().prop("purchasePrice.amount").endExpr().as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregatesDao.getAggregates(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", new BigDecimal("165"), values.get(0).get("aa"));
    }

    public void test_vehicle_model_retrieval8() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).yield(). //
		beginExpr().avgOf().prop("price.amount").add().avgOf().prop("purchasePrice.amount").endExpr().as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregatesDao.getAggregates(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", new BigDecimal("165"), values.get(0).get("aa"));
    }

    public void test_vehicle_model_retrieval9() {
	final AggregatedResultQueryModel model = select(TgVehicleModel.class).yield().countOfDistinct().prop("make").as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregatesDao.getAggregates(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "3", values.get(0).get("aa").toString());
    }

    public void test_vehicle_model_retrieval10() {
	final AggregatedResultQueryModel model = select(TgVehicleModel.class). //
		yield().countAll().as("aa"). //
		yield().countOfDistinct().prop("make").as("bb"). //
		yield().now().as("cc"). //
		modelAsAggregate();
	final List<EntityAggregates> values = aggregatesDao.getAggregates(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "7", values.get(0).get("aa").toString());
    	assertEquals("Incorrect value", "3", values.get(0).get("bb").toString());
    }

    public void test_vehicle_model_retrieval11() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").model();
	final AggregatedResultQueryModel countModel = select(model). //
		yield().countAll().as("aa"). //
		modelAsAggregate();

	final List<EntityAggregates> values = aggregatesDao.getAggregates(from(countModel).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    public void test_vehicle_model_retrieval12() {
	final PrimitiveResultQueryModel subQry = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").yield().prop("key").modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("key").in().model(subQry). //
		yield().countAll().as("aa"). //
		modelAsAggregate();

	final List<EntityAggregates> values = aggregatesDao.getAggregates(from(countModel).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    public void test_vehicle_model_retrieval13() {
	final PrimitiveResultQueryModel subQry = select(TgVehicle.class).where().prop("model.make").eq().prop("make.id").yield().countAll().modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicleMake.class).as("make").yield().prop("key").as("make"). //
	yield().model(subQry).as("vehicleCount"). //
	modelAsAggregate();

	final List<EntityAggregates> values = aggregatesDao.getAggregates(from(countModel).build());
	assertEquals("Incorrect count", 4, values.size());
	for (final EntityAggregates result : values) {
	    if (result.get("make").equals("MERC") || result.get("make").equals("AUDI")) {
		assertEquals("Incorrect value for make " + result.get("make"), "1", result.get("vehicleCount").toString());
	    } else {
		assertEquals("Incorrect value", "0", result.get("vehicleCount").toString());
	    }
	}
    }

    public void test_vehicle_model_retrieval14() {
	final PrimitiveResultQueryModel makeQry = select(TgVehicleMake.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").yield().prop("key").modelAsPrimitive();
	final PrimitiveResultQueryModel modelQry = select(TgVehicleModel.class).where().prop("make.key").in().model(makeQry).yield().prop("key").modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("model.key").in().model(modelQry). //
	yield().countAll().as("aa"). //
	modelAsAggregate();

	final List<EntityAggregates> values = aggregatesDao.getAggregates(from(countModel).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    public void test_vehicle_model_retrieval15() {
	final PrimitiveResultQueryModel makeQry = select(TgVehicleMake.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").and().prop("key").eq().prop("model.make.key").yield().prop("key").modelAsPrimitive();
	final PrimitiveResultQueryModel modelQry = select(TgVehicleModel.class).where().prop("make.key").in().model(makeQry).and().prop("key").eq().param("model_param").yield().prop("key").modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("model.key").in().model(modelQry). //
	yield().countAll().as("aa"). //
	modelAsAggregate();

	final List<EntityAggregates> values = aggregatesDao.getAggregates(from(countModel).with("model_param", "316").build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    @Test
    public void test_ordered_vehicle_retrieval16() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
	final OrderingModel ordering = orderBy().prop("model.make.key").desc().model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry).with(ordering).build());
    	final TgVehicle veh = models.get(0);
	assertEquals("Incorrect key", "CAR2", veh.getKey());
    }

    public void test_vehicle_model_retrieval17() {
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
	final List<EntityAggregates> values = aggregatesDao.getAggregates(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "merc", values.get(0).get("make"));
    	assertEquals("Incorrect value", "1", values.get(0).get("not-replaced-yet").toString());
    	assertEquals("Incorrect value", "MERC", values.get(0).get("make-key"));
    	assertEquals("Incorrect value", "0", values.get(0).get("zero-days").toString());
    	assertEquals("Incorrect value", "150", values.get(0).get("avgPrice").toString());
    	assertEquals("Incorrect value", "66.7", values.get(0).get("third-of-price").toString());
    }

    @Test
    public void test_vehile_18() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class).where().prop("key").eq().val("MERC").model();
	final List<TgVehicleMake> makes = vehicleMakeDao.getAllEntities(from(qry).build());
    	final TgVehicleMake make = makes.get(0);

	final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make").eq().val(make).model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry2).build());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

//    public void test_19() {
//	final EntityResultQueryModel<SecurityRoleAssociation> associationModel = select(SecurityRoleAssociation.class). //
//		where().prop("securityToken").eq().val(FirstLevelSecurityToken1.class.getName()).model();
//	final List<SecurityRoleAssociation> entities = fetcher().getEntities(from(associationModel).build());
//	assertEquals("Incorrect count", 2, entities.size());
//	assertEquals("Incorrect key", FirstLevelSecurityToken1.class, entities.get(0).getSecurityToken());
//
//    }

    @Test
    public void test_vehile_20() {
	final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make.key").iLike().val("me%").and().prop("key").iLike().val("%2").model();
	final List<TgVehicle> models = vehicleDao.getAllEntities(from(qry2).build());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Test
    public void test_vehicle_fetching() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
	final fetch<TgVehicle> fetchModel = new fetch<TgVehicle>(TgVehicle.class).with("model", new fetch<TgVehicleModel>(TgVehicleModel.class).with("make"));
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).with(fetchModel).build());
	final TgVehicle vehicle = vehicles.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
	assertEquals("Incorrect key", "316", vehicle.getModel().getKey());
	assertEquals("Incorrect key", "MERC", vehicle.getModel().getMake().getKey());
    }

//    public void test_aggregates_fetching() {
//	final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("key").eq().val("CAR2").yield().prop("model").as("model").modelAsAggregate();
//	final fetch<EntityAggregates> fetchModel = new fetch<EntityAggregates>(EntityAggregates.class).with("model", new fetch<TgVehicleModel>(TgVehicleModel.class).with("make"));
//	final EntityAggregates value = aggregatesDao.getAggregates(from(model).with(fetchModel).build()).get(0);
//	assertEquals("Incorrect key", "316", ((TgVehicleModel) value.get("model")).getKey());
//	assertEquals("Incorrect key", "MERC", ((TgVehicleModel) value.get("model")).getMake().getKey());
//    }
//

    @Test
    public void test_vehicle_with_collection_fetching() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).where().prop("key").eq().val("CAR2").model();
	final fetch<TgVehicle> fetchModel = new fetch<TgVehicle>(TgVehicle.class).with("model", new fetch<TgVehicleModel>(TgVehicleModel.class).with("make")).with("fuelUsages", new fetch<TgFuelUsage>(TgFuelUsage.class));
	final List<TgVehicle> vehicles = vehicleDao.getAllEntities(from(qry).with(fetchModel).build());
	final TgVehicle vehicle = vehicles.get(0);
	assertEquals("Incorrect key", "CAR2", vehicle.getKey());
	assertEquals("Incorrect key", "316", vehicle.getModel().getKey());
	assertEquals("Incorrect key", "MERC", vehicle.getModel().getMake().getKey());
	System.out.println(vehicle.getFuelUsages());
    }

//    @Override
//    protected String[] getDataSetPathsForInsert() {
//	return new String[] { "src/test/resources/data-files/entity-fetcher-test.flat.xml",  "src/test/resources/data-files/user-user_role-test-case.flat.xml"};
//    }

    @Override
    protected void populateDomain() {
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

	final TgVehicle car1 = save(new_(TgVehicle.class, "CAR1", "CAR1 DESC").setModel(m318).setPrice(new Money("20")).setPurchasePrice(new Money("10")).setActive(true).setLeased(false));
	final TgVehicle car2 = save(new_(TgVehicle.class, "CAR2", "CAR2 DESC").setModel(m316).setPrice(new Money("200")).setPurchasePrice(new Money("100")).setActive(false).setLeased(true));

	save(new_(TgFuelUsage.class, car2, date("2008-02-09 00:00:00")).setQty(new BigDecimal("100")));
	save(new_(TgFuelUsage.class, car2, date("2008-02-10 00:00:00")).setQty(new BigDecimal("120")));

	save(new_(TgTimesheet.class, "USER1", date("2011-11-01 13:00:00")).setFinishDate(date("2011-11-01 15:00:00")).setIncident("002"));
	System.out.println("\n   DATA POPULATED SUCCESSFULLY\n\n\n");
    }

    @Override
    protected List<Class<? extends AbstractEntity>> domainEntityTypes() {
	return PlatformTestDomainTypes.entityTypes;
    }
}