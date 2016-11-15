package ua.com.fielden.platform.test;

import static java.lang.String.format;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.data.IDomainDrivenData;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * This is a base class for all test cases in TG based applications. Each application module should provide file <b>src/test/resources/test.properties</b> with property
 * <code>config-domain</code> assigned an application specific class implementing contract {@link IDomainDrivenTestCaseConfiguration}.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainDrivenTestCase implements IDomainDrivenData {

    private static final Map<Class<? extends AbstractDomainDrivenTestCase>, DbCreator<?>> dbCreators = new ConcurrentHashMap<>();
    
    protected final DbCreator<?> dbCreator() {
        final Class<? extends AbstractDomainDrivenTestCase> testCaseType = this.getClass();
        if (!dbCreators.containsKey(testCaseType)) {
            System.out.println("CREATED DB CREATOR FOR TEST CASE " + testCaseType.getSimpleName());
            final DbCreator<?> dbCreator = new DbCreator<>(testCaseType);
            dbCreators.put(testCaseType, dbCreator);
        }
        
        return dbCreators.get(testCaseType);
    }

    private final ICompanionObjectFinder provider = dbCreator().config.getInstance(ICompanionObjectFinder.class);
    private final EntityFactory factory = dbCreator().config.getEntityFactory();
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
        dbCreator().beforeTest(this);
    }
   
    @After
    public final void afterTest() throws Exception {
        dbCreator().afterTest();
    }

   
    @Override
    public final <T> T getInstance(final Class<T> type) {
        return dbCreator().config.getInstance(type);
    }

    @Override
    public <T extends AbstractEntity<?>> T save(final T instance) {
        @SuppressWarnings("unchecked")
        final IEntityDao<T> pp = provider.find((Class<T>) instance.getType());
        return pp.save(instance);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEntityDao<E>, E extends AbstractEntity<?>> T co(final Class<E> type) {
        return (T) provider.find(type);
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
        return factory.newEntity(entityClass, key, desc);
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
        return factory.newByKey(entityClass, key);
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
        return keys.length == 0 ? new_(entityClass) : factory.newByKey(entityClass, keys);
    }

    /**
     * Instantiates a new entity based on the provided type only, which leads to creation of a completely empty instance without any of entity properties assigned.
     *
     * @param entityClass
     * @return
     */
    @Override
    public <T extends AbstractEntity<K>, K extends Comparable> T new_(final Class<T> entityClass) {
        return factory.newEntity(entityClass);
    }
}