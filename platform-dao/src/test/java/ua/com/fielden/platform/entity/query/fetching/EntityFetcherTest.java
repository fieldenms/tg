package ua.com.fielden.platform.entity.query.fetching;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class EntityFetcherTest extends DbDrivenTestCase {

    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    private final EntityFetcher ef = new EntityFetcher(session(), factory, injector.getInstance(MappingsGenerator.class), injector.getInstance(MappingExtractor.class), null);
    private Session session() {
	return hibernateUtil.getSessionFactory().getCurrentSession();
    }

    public void test_vehicle_model_retrieval0() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("key").eq().val("316").model();
	final List<TgVehicleModel> models = ef.list(session(), factory, new QueryExecutionModel(model, null), false);
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
	final List<TgVehicleModel> models = ef.list(session(), factory, new QueryExecutionModel(model, null), false);
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
	final List<TgVehicleModel> models = ef.list(session(), factory, new QueryExecutionModel(model, null), false);
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    public void test_vehicle_model_retrieval3() {
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("make").eq().val(1). //
		modelAsEntity(TgVehicleModel.class);
	final List<TgVehicleModel> models = ef.list(session(), factory, new QueryExecutionModel(model, new fetch(TgVehicleModel.class).with("make")), false);
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    public void test_vehicle_model_retrieval4() {
	final EntityResultQueryModel<TgVehicleMake> model = select(TgVehicleMake.class).where().prop("key").in().params("param1", "param2").model();
	final Map<String, Object> params = new HashMap<String, Object>();
	params.put("param1", "MERC");//new String[]{"MERC", "BMW"});
	params.put("param2", "BMW");
	final List<TgVehicleModel> models = ef.list(session(), factory, new QueryExecutionModel(model, null, null, params), false);
    	assertEquals("Incorrect count", 2, models.size());
    }

    public void test_vehicle_model_retrieval5() {
	final AggregatedResultQueryModel model = select(TgVehicle.class).where().prop("price.amount").ge().val(100).yield().prop("price.amount").as("aa").modelAsAggregate();
	final List<EntityAggregates> values = ef.list(session(), factory, new QueryExecutionModel(model, null), false);
    	assertEquals("Incorrect count", 1, values.size());
    	assertEquals("Incorrect value", new BigDecimal("200.00"), values.get(0).get("aa"));
    }


    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/hibernate-query-test-case.flat.xml" };
    }
}