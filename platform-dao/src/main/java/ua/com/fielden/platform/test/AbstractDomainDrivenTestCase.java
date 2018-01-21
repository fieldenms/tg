package ua.com.fielden.platform.test;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
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
public abstract class AbstractDomainDrivenTestCase implements IDomainDrivenData, ISessionEnabled {

    private DbCreator dbCreator;
    private static ICompanionObjectFinder coFinder;
    private static EntityFactory factory;
    private static Function<Class<?>, Object> instantiator;
    
    private static final DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    
    private Session session;
    private String transactionGuid;

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
        dbCreator.populateOrRestoreData(this);
    }
   
    @After
    public final void afterTest() {
        dbCreator.clearData();
    }
   
    public AbstractDomainDrivenTestCase setDbCreator(final DbCreator dbCreator) {
        this.dbCreator = dbCreator;
        return this;
    }
    
    public DbCreator getDbCreator() {
        return dbCreator;
    }
    
    @Override
    public final <T> T getInstance(final Class<T> type) {
        return (T) instantiator.apply(type);
    }
    
    @Override
    public <T extends AbstractEntity<?>> T save(final T instance) {
        @SuppressWarnings("unchecked")
        final IEntityDao<T> pp = coFinder.find((Class<T>) instance.getType());
        return pp.save(instance);
    }

    
    private final Map<Class<? extends AbstractEntity<?>>, IEntityDao<?>> co$Cache = new HashMap<>();
    private final Map<Class<? extends AbstractEntity<?>>, IEntityDao<?>> coCache = new HashMap<>();    

    @Override
    @SuppressWarnings("unchecked")
    public <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co$(final Class<E> type) {
        IEntityDao<?> co = co$Cache.get(type);
        if (co == null) {
            co = coFinder.find(type, false);
            co$Cache.put(type, co);
        }
        return (C) co;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co(final Class<E> type) {
        return (C) coCache.computeIfAbsent(type, k -> coFinder.find(k, true));

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
    public <T extends AbstractEntity<K>, K extends Comparable<?>> T new_(final Class<T> entityClass, final K key, final String desc) {
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
    public <T extends AbstractEntity<K>, K extends Comparable<?>> T new_(final Class<T> entityClass, final K key) {
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
    public <T extends AbstractEntity<K>, K extends Comparable<?>> T new_(final Class<T> entityClass) {
        final IEntityDao<T> co = co$(entityClass);
        return co != null ? co.new_() : factory.newEntity(entityClass);
    }
    
    /////////////// @SessionRequired support ///////////////
    @Override
    public Session getSession() {
        if (session == null) {
            throw new EntityCompanionException("Session is missing, most likely, due to missing @SessionRequired annotation.");
        }
        return session;
    }

    @Override
    public void setSession(final Session session) {
        this.session = session;
    }
    
    @Override
    public String getTransactionGuid() {
        if (StringUtils.isEmpty(transactionGuid)) {
            throw new EntityCompanionException("Transaction GUID is missing.");
        }
        return transactionGuid;
    }
    
    @Override
    public void setTransactionGuid(final String guid) {
        this.transactionGuid = guid;
    }

}