package ua.com.fielden.platform.test;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.data.IDomainDrivenData;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.Finder;

/**
 * This is a base class for all test cases in TG based applications. Each application module should provide file <b>src/test/resources/test.properties</b> with property
 * <code>config-domain</code> assigned an application specific class implementing contract {@link IDomainDrivenTestCaseConfiguration}.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainDrivenTestCase implements IDomainDrivenData {

    /**
     * This static map holds references to DB related information, including data population and truncation scripts, for each test case type.
     * It gets populated at the time of test case class loading using a <code<ClassRule</code> below. 
     */
    private static final Cache<String, DbCreator> dbCreators = CacheBuilder.newBuilder().build();
    
    /**
     * A convenient factory/accessor method for instances of {@link DbCreator} that are associated with a passed in test case type.
     * 
     * @param testCaseType
     * @return
     */
    protected static final DbCreator dbCreator(final String uuid) {
        if (dbCreators.getIfPresent(uuid) == null) {
            final DbCreator dbCreator = new DbCreator(uuid);
            dbCreators.put(uuid, dbCreator);
        }
        
        return dbCreators.getIfPresent(uuid);
    }

    private static String uuid() {
        return ManagementFactory.getRuntimeMXBean().getName() + "_" + Thread.currentThread().getId();
    }
    
    /**
     * A class rule (this is a new concept introduced in JUnit 4.x) that acts similar to <code>@BeforeClass/@AfterClass</code> by executing at the time of loading/off-loading a test case class.
     * Its main purpose is to generate and populate a database specific for a test case with a class that is being loaded.
     * The use of a class rule has an advantage over static method initializers by providing additional information such as a test case class.
     * This provides a way to encapsulate test case specific database creation and population logic at the level of this base class that all other test cases inherit from. 
     */
    @ClassRule
    public static final ExternalResource resource = new ExternalResource() {
        public Statement apply(final Statement base, final Description description) {
            try {
                // this call populates the above static map dbCreators
                // the created instance holds the data population and truncation scripts that get reused by individual test
                // at the same time, the actual database can be created ad-hoc even on per test (method) basis if needed
                dbCreator(uuid());
            } catch (Exception ex) {
                throw new Error(format("Could not populate data for test case %s.", description), ex);
            }

            return super.apply(base, description);
        };
        
    };

    @ClassRule
    public static final TestWatcher watcher = new TestWatcher() {
        protected void finished(final Description description) {
            final DbCreator dbCreator = dbCreators.getIfPresent(uuid());
            try {
                final Path rootPath = Paths.get(DbCreator.baseDir);
                Files.walk(rootPath)
                .filter(path -> path.getFileName().toString().contains(dbCreator.dbName()))
                    .map(Path::toFile)
                    .peek(file -> System.out.println(format("Removing %s", file.getName())))
                    .forEach(File::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }

        };
    };
    
    private final ICompanionObjectFinder provider = dbCreator(uuid()).config.getInstance(ICompanionObjectFinder.class);
    private final EntityFactory factory = dbCreator(uuid()).config.getEntityFactory();
    private final DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Should be implemented in order to provide domain driven data population.
     */
    protected abstract void populateDomain();

    /**
     * Should return a complete list of domain entity types.
     *
     * @return
     */
    protected abstract List<Class<? extends AbstractEntity<?>>> domainEntityTypes();

    @Before
    public final void beforeTest() throws Exception {
        dbCreator(uuid()).populateOrRestoreData(this);
    }
   
    @After
    public final void afterTest() throws Exception {
        dbCreator(uuid()).clearData(this.getClass());
    }

   
    @Override
    public final <T> T getInstance(final Class<T> type) {
        return dbCreator(uuid()).config.getInstance(type);
    }

    @Override
    public <T extends AbstractEntity<?>> T save(final T instance) {
        @SuppressWarnings("unchecked")
        final IEntityDao<T> pp = provider.find((Class<T>) instance.getType());
        return pp.save(instance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEntityDao<E>, E extends AbstractEntity<?>> T co$(final Class<E> type) {
        return (T) provider.find(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEntityDao<E>, E extends AbstractEntity<?>> T co(final Class<E> type) {
        return (T) provider.find(type, true);
    }

    @Override
    public final Date date(final String dateTime) {
        try {
            return formatter.parse(dateTime);
        } catch (ParseException e) {
            throw new IllegalArgumentException(format("Could not parse value [%s].", dateTime));
        }
    }

    @Override
    public final DateTime dateTime(final String dateTime) {
        return jodaFormatter.parseDateTime(dateTime);
    }

    /**
     * Instantiates a new entity with a non-composite key, the value for which is provided as the second argument, and description -- provided as the value for the third argument.
     *
     * @param entityClass
     * @param key
     * @param desc
     * @return
     */
    @Override
    public <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key, final String desc) {
        final T entity = new_(entityClass);
        entity.setKey(key);
        entity.setDesc(desc);
        return entity;
    }

    /**
     * Instantiates a new entity with a non-composite key, the value for which is provided as the second argument.
     *
     * @param entityClass
     * @param key
     * @return
     */
    @Override
    public <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass, final K key) {
        final T entity = new_(entityClass);
        entity.setKey(key);
        return entity;
    }

    /**
     * Instantiates a new entity with composite key, where composite key members are assigned based on the provide value. The order of values must match the order specified in key
     * member definitions. An empty list of key values is permitted.
     *
     * @param entityClass
     * @param keys
     * @return
     */
    @Override
    public <T extends AbstractEntity<DynamicEntityKey>> T new_composite(final Class<T> entityClass, final Object... keys) {
        final T entity = new_(entityClass);
        if (keys.length > 0) {
            // setting composite key fields
            final List<Field> fieldList = Finder.getKeyMembers(entityClass);
            if (fieldList.size() != keys.length) {
                throw new IllegalArgumentException(format("Number of key values is %s but should be %s", keys.length, fieldList.size()));
            }
            for (int index = 0; index < fieldList.size(); index++) {
                final Field keyField = fieldList.get(index);
                final Object keyValue = keys[index];
                entity.set(keyField.getName(), keyValue);
            }
        }
        return entity;
    }

    /**
     * Instantiates a new entity based on the provided type only, which leads to creation of a completely empty instance without any of entity properties assigned.
     *
     * @param entityClass
     * @return
     */
    @Override
    public <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass) {
        final IEntityDao<T> co = co$(entityClass);
        return co != null ? co.new_() : factory.newEntity(entityClass);
    }
}