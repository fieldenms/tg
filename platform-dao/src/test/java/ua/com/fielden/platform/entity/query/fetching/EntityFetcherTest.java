package ua.com.fielden.platform.entity.query.fetching;

import java.util.List;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class EntityFetcherTest extends DbDrivenTestCase {

    public void test_vehicle_model_retrieval() {
	final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
	final EntityFetcher ef = new EntityFetcher(session, injector.getInstance(EntityFactory.class), injector.getInstance(MappingsGenerator.class), injector.getInstance(MappingExtractor.class), null);
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
	final List<TgVehicleModel> models = ef.list(session, injector.getInstance(EntityFactory.class), new QueryExecutionModel(model, null/*new fetch(TgVehicleModel.class).with("make")*/), false);
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    public void test_vehicle_model_retrieval2() {
	final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
	final EntityFetcher ef = new EntityFetcher(session, injector.getInstance(EntityFactory.class), injector.getInstance(MappingsGenerator.class), injector.getInstance(MappingExtractor.class), null);

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
	final List<TgVehicleModel> models = ef.list(session, injector.getInstance(EntityFactory.class), new QueryExecutionModel(model, null/*new fetch(TgVehicleModel.class).with("make")*/), false);
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
	assertEquals("Incorrect key", "MERC", vehModel.getMake().getKey());
    }

    public void test_vehicle_model_retrieval3() {
	final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
	final EntityFetcher ef = new EntityFetcher(session, injector.getInstance(EntityFactory.class), injector.getInstance(MappingsGenerator.class), injector.getInstance(MappingExtractor.class), null);

	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class).where().prop("make").eq().val(1). //
		yield().prop("id").as("id").
		yield().prop("version").as("version").
		yield().prop("key").as("key").
		yield().prop("desc").as("desc").
		modelAsEntity(TgVehicleModel.class);
	final List<TgVehicleModel> models = ef.list(session, injector.getInstance(EntityFactory.class), new QueryExecutionModel(model, null/*new fetch(TgVehicleModel.class).with("make")*/), false);
    	final TgVehicleModel vehModel = models.get(0);
	assertEquals("Incorrect key", "316", vehModel.getKey());
    }


//    @Test
//    @Ignore
//    public void test_vehicle_model_retrieval2() {
//	final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
//	final EntityFetcher ef = new EntityFetcher(session, injector.getInstance(EntityFactory.class), injector.getInstance(MappingsGenerator.class), injector.getInstance(MappingExtractor.class), null);
//	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class). //
//		yield().prop("id").as("id").
//		yield().prop("version").as("version").
//		yield().prop("key").as("key").
//		yield().prop("desc").as("desc").
//		yield().prop("make").as("make").
//		modelAsEntity(TgVehicleModel.class);
//	final List<TgVehicleModel> models = ef.list(session, injector.getInstance(EntityFactory.class), new QueryExecutionModel(model, new fetch(TgVehicleModel.class).with("make")), false);
//    	final TgVehicleModel vehModel = models.get(0);
//	assertEquals("Incorrect key", "316", vehModel.getKey());
//    }


    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/hibernate-query-test-case.flat.xml" };
    }

}
