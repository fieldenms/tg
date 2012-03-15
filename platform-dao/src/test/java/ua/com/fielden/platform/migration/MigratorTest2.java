package ua.com.fielden.platform.migration;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.entity.query.fetchAll;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao2;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao2;
import ua.com.fielden.platform.migration.dao.MigrationRunDao2;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake2;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel2;
import ua.com.fielden.platform.test.DbDrivenTestCase2;

/**
 * Test the basic data migration logic.
 *
 * @author TG team
 *
 */
public class MigratorTest2 extends DbDrivenTestCase2 {
    private final Connection conn = injector.getInstance(Connection.class);
    private final MigrationErrorDao2 errorDao = injector.getInstance(MigrationErrorDao2.class);
    private final MigrationHistoryDao2 histDao = injector.getInstance(MigrationHistoryDao2.class);
    private final MigrationRunDao2 runDao = injector.getInstance(MigrationRunDao2.class);


    @Override
    public void setUp() throws Exception {
        super.setUp();
	conn.createStatement().execute("DROP ALL OBJECTS;");
	conn.createStatement().execute("RUNSCRIPT FROM 'src/test/resources/db/legacy-db.ddl'");
    }

    @Override
    public void tearDown() throws Exception {
	conn.close();
        super.tearDown();
    }

    private MigrationRun generateMigrationRun() {
	final Date now = new Date();
	final MigrationRun migrationRun = entityFactory.newByKey(MigrationRun.class, now.toString());
	migrationRun.setStarted(now);
	runDao.save(migrationRun);
	return migrationRun;
    }

    public void test_migrating_of_simple_entity() throws Exception {
	hibernateUtil.getSessionFactory().getCurrentSession().close();
	final MigrationRun migrationRun = generateMigrationRun();

	final tMakeRetriever2 makeRet = injector.getInstance(tMakeRetriever2.class);
	makeRet.populateData(hibernateUtil.getSessionFactory(), conn, config.getEntityFactory(), errorDao, histDao, migrationRun, null);

	hibernateUtil.getSessionFactory().getCurrentSession().close();

	final ITgVehicleMake2 dao = injector.getInstance(ITgVehicleMake2.class);
	assertEquals("Incorrect number of migrated entities.", 4, dao.getPage(0, 4).data().size());

	final List<MigrationHistory> hist = histDao.firstPage(100).data();
	assertEquals("There should be only one migration history record.", 1, hist.size());
	assertEquals("Incorrect hist record entity type name.", TgVehicleMake.class.getName(), hist.get(0).getEntityTypeName());
	assertEquals("Incorrect hist record retriever type name.", tMakeRetriever2.class.getName(), hist.get(0).getRetrieverTypeName());
    }

    public void test_migrating_of_entity_with_another_entity_association() throws Exception {
	hibernateUtil.getSessionFactory().getCurrentSession().close();
	final MigrationRun migrationRun = generateMigrationRun();
	// populate makes
	final tMakeRetriever2 makeRet = injector.getInstance(tMakeRetriever2.class);
	makeRet.populateData(hibernateUtil.getSessionFactory(), conn, config.getEntityFactory(), errorDao, histDao, migrationRun, null);

	// populate models, which depend on makes
	final ITgVehicleModel2 dao = injector.getInstance(ITgVehicleModel2.class);

	final tModelRetriever2 ret = injector.getInstance(tModelRetriever2.class);

	ret.populateData(hibernateUtil.getSessionFactory(), conn, config.getEntityFactory(), errorDao, histDao, migrationRun, null);
	hibernateUtil.getSessionFactory().getCurrentSession().close();

	assertEquals("Incorrect number of migrated entities.", 14, dao.getPage(0, 100).data().size());
	assertNotNull("Not-null association has not been migrated correctly.", dao.findByKeyAndFetch(new fetchAll(TgVehicleModel.class), "MODEL1").getMake());
	assertNotNull("Not-null association has not been migrated correctly.", dao.findByKeyAndFetch(new fetchAll(TgVehicleModel.class), "MODEL2").getMake());
	//FIXME
	//assertNull("Null association has not been migrated correctly.", dao.findByKeyAndFetch(new fetchAll(TgVehicleModel.class), "MODEL3").getMake());
    }

    //FIXME
//    public void test_updating_of_entity_with_another_entity_association() throws Exception {
//	hibernateUtil.getSessionFactory().getCurrentSession().close();
//	final MigrationRun migrationRun = generateMigrationRun();
//	// populate makes
//	final tMakeRetriever makeRet = injector.getInstance(tMakeRetriever.class);
//
//	makeRet.populateData(hibernateUtil.getSessionFactory(), conn, config.getEntityFactory(), errorDao, histDao, migrationRun, null);
//
//	// populate models, which depend on makes
//	final ITgVehicleModel dao = injector.getInstance(ITgVehicleModel.class);
//
//	final tModelWithMakeNotPopulatedRetriever ret = injector.getInstance(tModelWithMakeNotPopulatedRetriever.class);
//
//	ret.populateData(hibernateUtil.getSessionFactory(), conn, config.getEntityFactory(), errorDao, histDao, migrationRun, null);
//	hibernateUtil.getSessionFactory().getCurrentSession().close();
//
//	final tModelUpdaterWithMake upd = injector.getInstance(tModelUpdaterWithMake.class);
//	upd.populateData(hibernateUtil.getSessionFactory(), conn, config.getEntityFactory(), errorDao, histDao, migrationRun, null);
//
//	assertEquals("Incorrect number of migrated entities.", 15, dao.getPage(0, 15).data().size());
//	assertNotNull("Not-null association has not been migrated correctly.", dao.findByKeyAndFetch(new fetchAll(TgVehicleModel.class), "MODEL1").getMake());
//	assertNotNull("Not-null association has not been migrated correctly.", dao.findByKeyAndFetch(new fetchAll(TgVehicleModel.class), "MODEL2").getMake());
//	assertNull("Null association has not been migrated correctly.", dao.findByKeyAndFetch(new fetchAll(TgVehicleModel.class), "MODEL3").getMake());
//    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/empty-data.flat.xml" };
    }
}
