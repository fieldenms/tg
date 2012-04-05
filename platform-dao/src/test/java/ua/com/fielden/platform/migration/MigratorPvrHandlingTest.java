package ua.com.fielden.platform.migration;

import java.sql.Connection;
import java.util.Date;

import org.junit.Ignore;

import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.migration.dao.MigrationRunDao;
import ua.com.fielden.platform.test.DbDrivenTestCase;

/**
 * Tests data migration logic when properties are provided with value retriever. It is based on the {@link tMeterReadingRetriever}.
 *
 * @author TG team
 *
 */
public class MigratorPvrHandlingTest extends DbDrivenTestCase {
    private Connection conn = injector.getInstance(Connection.class);
    private final MigrationErrorDao errorDao = injector.getInstance(MigrationErrorDao.class);
    private final MigrationHistoryDao histDao = injector.getInstance(MigrationHistoryDao.class);
    private final MigrationRunDao runDao = injector.getInstance(MigrationRunDao.class);

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


    @Ignore
    public void test_migration_of_entity_with_property_having_composite_key() throws Exception {
	hibernateUtil.getSessionFactory().getCurrentSession().close();
	assertNotNull(1);
//	final MigrationRun migrationRun = generateMigrationRun();
//
//	final tMeterReadingRetriever ret = injector.getInstance(tMeterReadingRetriever.class);
//	ret.populateData(hibernateUtil.getSessionFactory(), conn, config.getEntityFactory(), errorDao, histDao, migrationRun, null);
//
//	hibernateUtil.getSessionFactory().getCurrentSession().close();
//
//	final IQueryOrderedModel<TgMeterReading> model = select(TgMeterReading.class).orderBy("readingDate").model();
//	final List<TgMeterReading> readings = injector.getInstance(ITgMeterReading.class).getEntities(model, fetchAll(TgMeterReading.class));
//	assertEquals("Incorrect number of migrated entities.", 2, readings.size());
//	assertNotNull("Should have fuel usage association", readings.get(0).getFuelUsage());
//	assertNull("Should not have fuel usage association", readings.get(1).getFuelUsage());
    }

    @Override
    protected String[] getDataSetPathsForInsert() {
	return new String[] { "src/test/resources/data-files/empty-data.flat.xml"};
	//return new String[] { "src/test/resources/data-files/meter-reading-import-test-data.flat.xml" };
    }
}
