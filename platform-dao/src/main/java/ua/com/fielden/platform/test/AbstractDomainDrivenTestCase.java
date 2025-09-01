package ua.com.fielden.platform.test;

import static java.lang.String.format;
import static ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator.ID_SEQUENCE_NAME;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.data.IDomainDrivenData;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.test.exceptions.DomainDriventTestException;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.DbUtils;

/**
 * This is a base class for all test cases in TG based applications.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainDrivenTestCase implements IDomainDrivenData, ISessionEnabled {

    // The following fields are reflectlively assigned only once, by the platform test runner.
    private static ICompanionObjectFinder coFinder;
    private static EntityFactory factory;
    private static Function<Class<?>, Object> instantiator;

    private static final DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat DATE_TIME_FORMAT_WITHOUT_SECONDS = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final DateFormat DATE_TIME_FORMAT_WITHOUT_MILLIS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat DATE_TIME_FORMAT_WITH_MILLIS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private DbCreator dbCreator;
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

    @SessionRequired
    protected void resetIdGenerator() {
        DbUtils.resetSequenceGenerator(ID_SEQUENCE_NAME, 1000000, this.getSession());
    }

    @After
    public final void afterTest() {
        dbCreator.clearData();
    }

    public AbstractDomainDrivenTestCase setDbCreator(final DbCreator dbCreator) {
        this.dbCreator = dbCreator;
        return this;
    }

    public final DbCreator getDbCreator() {
        return dbCreator;
    }

    @Override
    public final <T> T getInstance(final Class<T> type) {
        return (T) instantiator.apply(type);
    }

    @Override
    public <T extends AbstractEntity<?>> T save(final T instance) {
        if (instance == null) {
            throw new DomainDriventTestException("Null instances cannot be saved.");
        }
        @SuppressWarnings("unchecked")
        final IEntityDao<T> pp = coFinder.find((Class<T>) instance.getType());
        if (pp == null) {
            throw new DomainDriventTestException(format("Could not find companion implementation for [%s].", instance.getType().getSimpleName()));
        }
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

    /**
     * Converts a date string to a {@link Date} using system's default time zone.
     * <p>
     * Supported formats:
     * <ul>
     *     <li>yyyy-MM-dd</li>
     *     <li>yyyy-MM-dd HH:mm</li>
     *     <li>yyyy-MM-dd HH:mm:ss</li>
     *     <li>yyyy-MM-dd HH:mm:ss.SSS</li>
     * </ul>
     */
    @Override
    public final Date date(final String dateTime) {
        try {
            // has millis part?
            if (dateTime.indexOf('.') > 0) {
                return DATE_TIME_FORMAT_WITH_MILLIS.parse(dateTime);
            }
            // has time part without seconds?
            else if (dateTime.lastIndexOf(":") == 13) {
                return DATE_TIME_FORMAT_WITHOUT_SECONDS.parse(dateTime);
            }
            // has time part without millis?
            else if (dateTime.indexOf(":") > 0) {
                return DATE_TIME_FORMAT_WITHOUT_MILLIS.parse(dateTime);
            }
            // otherwise, assume date without the time part
            else {
                return DATE_FORMAT.parse(dateTime);
            }
        } catch (ParseException e) {
            throw new DomainDriventTestException(format("Could not parse date [%s].", dateTime));
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
                throw new DomainDriventTestException(format("Number of key values is %s but should be %s", keys.length, fieldList.size()));
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
            throw new DomainDriventTestException("Session is missing, most likely, due to missing @SessionRequired annotation.");
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
            throw new DomainDriventTestException("Transaction GUID is missing.");
        }
        return transactionGuid;
    }

    @Override
    public void setTransactionGuid(final String guid) {
        this.transactionGuid = guid;
    }

    @Override
    public User getUser() {
        final IUserProvider up = getInstance(IUserProvider.class);
        return up.getUser();
    }

}
