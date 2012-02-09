package ua.com.fielden.platform.entity.query.fetching;

import java.util.List;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
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
	System.out.println("***************************** A A A *******************************");

	final List<TgVehicleModel> models = ef.list(session(), factory, new QueryExecutionModel(model, new fetch(TgVehicleModel.class).with("make")), false);
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/hibernate-query-test-case.flat.xml" };
    }
}