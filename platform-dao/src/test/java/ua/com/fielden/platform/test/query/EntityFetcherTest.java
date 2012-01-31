package ua.com.fielden.platform.test.query;

import java.util.List;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.DomainMetaPropertyConfig;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.domain.entities.Bogie;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkorderDao;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class EntityFetcherTest extends DbDrivenTestCase {
    private final IEntityAggregatesDao aggregatesDao = injector.getInstance(IEntityAggregatesDao.class);
    private final IWorkorderDao workOrderDao = injector.getInstance(IWorkorderDao.class);

    private final static DomainMetaPropertyConfig domainConfig = config.getInjector().getInstance(DomainMetaPropertyConfig.class);

    static {
	domainConfig.setDefiner(Bogie.class, "location", null);
    }

    public void test_vehicle_model_retrieval() {
	final Session session = hibernateUtil.getSessionFactory().getCurrentSession();
	final EntityFetcher ef = new EntityFetcher(session, injector.getInstance(EntityFactory.class), injector.getInstance(MappingsGenerator.class), null);
	final EntityResultQueryModel<TgVehicleModel> model = select(TgVehicleModel.class). //
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

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/hibernate-query-test-case.flat.xml" };
    }

}
