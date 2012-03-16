package ua.com.fielden.platform.entity.query.fetching;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.FirstLevelSecurityToken1;
import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao2.AggregatesQueryExecutionModel;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.AggregatesFetcher;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.test.DbDrivenTestCase2;
import ua.com.fielden.platform.types.Money;
import static ua.com.fielden.platform.entity.query.fluent.query.from;
import static ua.com.fielden.platform.entity.query.fluent.query.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class EntityFetcherTest extends DbDrivenTestCase2 {

    private static final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private static final EntityFetcher fetcher() {return new EntityFetcher(session(), factory, injector.getInstance(MappingsGenerator.class), null, null, null); }
    private static final AggregatesFetcher aggregatesFetcher() {return new AggregatesFetcher(session(), factory, injector.getInstance(MappingsGenerator.class), null, null, null); }

    private static final Session session() {return hibernateUtil.getSessionFactory().getCurrentSession();}

    public void test_vehicle_model_retrieval0() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("key").eq().val("316").model();
	final List<TgVehicleModel> models = fetcher().list(from(model).build());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", null, vehModel.getMake());
    }

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
	final List<TgVehicleModel> models = fetcher().list(from(model).build());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

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
	final List<TgVehicleModel> models = fetcher().list(from(model).build());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    public void test_vehicle_model_retrieval3() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("make").eq().val(1). //
		modelAsEntity(TgVehicleModel.class);
	final List<TgVehicleModel> models = fetcher().list(from(model).with(new fetch<TgVehicleModel>(TgVehicleModel.class).with("make")).build());
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    public void test_vehicle_model_retrieval4() {
	final EntityResultQueryModel<TgVehicleMake> model = select(TgVehicleMake.class).where().prop("key").in().params("param1", "param2").model();
	final List<TgVehicleModel> models = fetcher().list(from(model).with("param1", "MERC").with("param2", "BMW").build());
    	assertEquals("Incorrect count", 2, models.size());
    }

    public void test_vehicle_model_retrieval5() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).as("v").where().prop("v").in().values(1, 2).and().prop("v.price.amount").ge().val(100).yield().prop("v.price.amount").as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregatesFetcher().list(new AggregatesQueryExecutionModel.Builder(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", new BigDecimal("200.00"), values.get(0).get("aa"));
    }

    public void test_vehicle_model_retrieval6() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("price.amount").ge().val(100).model();
	final List<TgVehicle> values = fetcher().list(from(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect count", new Money("100.00"), values.get(0).getPurchasePrice());
    }

    public void test_vehicle_model_retrieval7() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).yield(). //
		avgOf().beginExpr().prop("price.amount").add().prop("purchasePrice.amount").endExpr().as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregatesFetcher().list(new AggregatesQueryExecutionModel.Builder(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", new BigDecimal("165"), values.get(0).get("aa"));
    }

    public void test_vehicle_model_retrieval8() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).yield(). //
		beginExpr().avgOf().prop("price.amount").add().avgOf().prop("purchasePrice.amount").endExpr().as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregatesFetcher().list(new AggregatesQueryExecutionModel.Builder(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", new BigDecimal("165"), values.get(0).get("aa"));
    }

    public void test_vehicle_model_retrieval9() {
	final AggregatedResultQueryModel model = select(TgVehicleModel.class).yield().countOfDistinct().prop("make").as("aa").modelAsAggregate();
	final List<EntityAggregates> values = aggregatesFetcher().list(new AggregatesQueryExecutionModel.Builder(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "3", values.get(0).get("aa").toString());
    }

    public void test_vehicle_model_retrieval10() {
	final AggregatedResultQueryModel model = select(TgVehicleModel.class). //
		yield().countAll().as("aa"). //
		yield().countOfDistinct().prop("make").as("bb"). //
		yield().now().as("cc"). //
		modelAsAggregate();
	final List<EntityAggregates> values = aggregatesFetcher().list(new AggregatesQueryExecutionModel.Builder(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "7", values.get(0).get("aa").toString());
    	assertEquals("Incorrect value", "3", values.get(0).get("bb").toString());
    }

    public void test_vehicle_model_retrieval11() {
	final EntityResultQueryModel<TgVehicle> model = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").model();
	final AggregatedResultQueryModel countModel = select(model). //
		yield().countAll().as("aa"). //
		modelAsAggregate();

	final List<EntityAggregates> values = aggregatesFetcher().list(new AggregatesQueryExecutionModel.Builder(countModel).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    public void test_vehicle_model_retrieval12() {
	final PrimitiveResultQueryModel subQry = select(TgVehicle.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").yield().prop("key").modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("key").in().model(subQry). //
		yield().countAll().as("aa"). //
		modelAsAggregate();

	final List<EntityAggregates> values = aggregatesFetcher().list(new AggregatesQueryExecutionModel.Builder(countModel).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    public void test_vehicle_model_retrieval13() {
	final PrimitiveResultQueryModel subQry = select(TgVehicle.class).where().prop("model.make").eq().prop("make.id").yield().countAll().modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicleMake.class).as("make").yield().prop("key").as("make"). //
	yield().model(subQry).as("vehicleCount"). //
	modelAsAggregate();

	final List<EntityAggregates> values = aggregatesFetcher().list(new AggregatesQueryExecutionModel.Builder(countModel).build());
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

	final List<EntityAggregates> values = aggregatesFetcher().list(new AggregatesQueryExecutionModel.Builder(countModel).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    public void test_vehicle_model_retrieval15() {
	final PrimitiveResultQueryModel makeQry = select(TgVehicleMake.class).where().prop("model.make.key").eq().anyOfValues("BMW", "MERC").and().prop("key").eq().prop("model.make.key").yield().prop("key").modelAsPrimitive();
	final PrimitiveResultQueryModel modelQry = select(TgVehicleModel.class).where().prop("make.key").in().model(makeQry).and().prop("key").eq().param("model_param").yield().prop("key").modelAsPrimitive();
	final AggregatedResultQueryModel countModel = select(TgVehicle.class).where().prop("model.key").in().model(modelQry). //
	yield().countAll().as("aa"). //
	modelAsAggregate();

	final List<EntityAggregates> values = aggregatesFetcher().list(new AggregatesQueryExecutionModel.Builder(countModel).paramValue("model_param", "316").build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "1", values.get(0).get("aa").toString());
    }

    public void test_ordered_vehicle_retrieval16() {
	final EntityResultQueryModel<TgVehicle> qry = select(TgVehicle.class).model();
	final OrderingModel ordering = orderBy().prop("model.make.key").desc().model();
	final List<TgVehicle> models = fetcher().list(from(qry).with(ordering).build());
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
	final List<EntityAggregates> values = aggregatesFetcher().list(new AggregatesQueryExecutionModel.Builder(model).build());
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", "merc", values.get(0).get("make"));
    	assertEquals("Incorrect value", "1", values.get(0).get("not-replaced-yet").toString());
    	assertEquals("Incorrect value", "MERC", values.get(0).get("make-key"));
    	assertEquals("Incorrect value", "0", values.get(0).get("zero-days").toString());
    	assertEquals("Incorrect value", "150", values.get(0).get("avgPrice").toString());
    	assertEquals("Incorrect value", "66.7", values.get(0).get("third-of-price").toString());
    }

    public void test_vehile_18() {
	final EntityResultQueryModel<TgVehicleMake> qry = select(TgVehicleMake.class).where().prop("key").eq().val("MERC").model();
	final List<TgVehicleMake> makes = fetcher().list(from(qry).build());
    	final TgVehicleMake make = makes.get(0);

	final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make").eq().val(make).model();
	final List<TgVehicle> models = fetcher().list(from(qry2).build());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    public void test_19() {
	final EntityResultQueryModel<SecurityRoleAssociation> associationModel = select(SecurityRoleAssociation.class). //
		where().prop("securityToken").eq().val(FirstLevelSecurityToken1.class.getName()).model();
	final List<SecurityRoleAssociation> entities = fetcher().list(from(associationModel).build());
	assertEquals("Incorrect count", 2, entities.size());
	assertEquals("Incorrect key", FirstLevelSecurityToken1.class, entities.get(0).getSecurityToken());

    }

    public void test_vehile_20() {
	final EntityResultQueryModel<TgVehicle> qry2 = select(TgVehicle.class).where().prop("model.make.key").iLike().val("me%").and().prop("key").iLike().val("%2").model();
	final List<TgVehicle> models = fetcher().list(from(qry2).build());
	assertEquals("Incorrect key", "CAR2", models.get(0).getKey());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/entity-fetcher-test.flat.xml",  "src/test/resources/data-files/user-user_role-test-case.flat.xml"};
    }
}