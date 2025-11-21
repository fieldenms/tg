package ua.com.fielden.platform.test;

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
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.exceptions.DomainDrivenTestException;
import ua.com.fielden.platform.utils.DbUtils;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator.ID_SEQUENCE_NAME;

/// This is a base class for all test cases in TG-based applications.
///
public abstract class AbstractDomainDrivenTestCase implements IDomainDrivenData, ISessionEnabled {

    public static final String
            ERR_CANNOT_SAVE_NULL = "Null instances cannot be saved.",
            ERR_MISSING_COMPANION = "Could not find companion implementation for [%s].",
            ERR_PARSING_DATE = "Could not parse date [%s].",
            ERR_INVALID_NUMBER_OF_KEY_VALUES = "Number of key values is %s but should be %s.",
            ERR_MISSING_SESSION = "Session is missing, most likely, due to missing @SessionRequired annotation.";

    private DbCreator dbCreator;
    private static ICompanionObjectFinder coFinder;
    private static EntityFactory factory;
    private static Function<Class<?>, Object> instantiator;

    private static final DateTimeFormatter jodaFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat DATE_TIME_FORMAT_WITHOUT_SECONDS = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final DateFormat DATE_TIME_FORMAT_WITHOUT_MILLIS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat DATE_TIME_FORMAT_WITH_MILLIS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");


    private Session session;
    private String transactionGuid;

    /// Should be implemented in order to provide domain driven data population.
    ///
    protected abstract void populateDomain();

    /// Should return a complete list of domain entity types.
    ///
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
            throw new DomainDrivenTestException(ERR_CANNOT_SAVE_NULL);
        }
        @SuppressWarnings("unchecked")
        final IEntityDao<T> pp = coFinder.find((Class<T>) instance.getType());
        if (pp == null) {
            throw new DomainDrivenTestException(ERR_MISSING_COMPANION.formatted(instance.getType().getSimpleName()));
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

    /// Converts a date string to a [Date] using system's default time zone.
    ///
    /// Supported formats:
    ///
    /// - `yyyy-MM-dd`
    /// - `yyyy-MM-dd HH:mm`
    /// - `yyyy-MM-dd HH:mm:ss`
    /// - `yyyy-MM-dd HH:mm:ss.SSS`
    ///
    @Override
    public final Date date(final String dateTime) {
        try {
            // Has millis part?
            if (dateTime.indexOf('.') > 0) {
                return DATE_TIME_FORMAT_WITH_MILLIS.parse(dateTime);
            }
            // Has time part without seconds?
            else if (dateTime.lastIndexOf(":") == 13) {
                return DATE_TIME_FORMAT_WITHOUT_SECONDS.parse(dateTime);
            }
            // Has time part without millis?
            else if (dateTime.indexOf(":") > 0) {
                return DATE_TIME_FORMAT_WITHOUT_MILLIS.parse(dateTime);
            }
            // Otherwise, assume the date without the time part.
            else {
                return DATE_FORMAT.parse(dateTime);
            }
        } catch (ParseException e) {
            throw new DomainDrivenTestException(ERR_PARSING_DATE.formatted(dateTime));
        }
    }

    @Override
    public final DateTime dateTime(final String dateTime) {
        return jodaFormatter.parseDateTime(dateTime);
    }

    /// Instantiates a new entity with a non-composite key, where the key value is provided as the second argument,
    /// and the description is provided as the third argument.
    ///
    @Override
    public <T extends AbstractEntity<K>, K extends Comparable<?>> T new_(final Class<T> entityClass, final K key, final String desc) {
        final T entity = new_(entityClass);
        entity.setKey(key);
        entity.set(DESC, desc);
        return entity;
    }

    /// Instantiates a new entity with a non-composite key, whose value is provided as the second argument.
    ///
    @Override
    public <T extends AbstractEntity<K>, K extends Comparable<?>> T new_(final Class<T> entityClass, final K key) {
        final T entity = new_(entityClass);
        entity.setKey(key);
        return entity;
    }

    /// Instantiates a new entity with a composite key, where the key members are assigned based on the provided values.
    /// The order of values must match the order defined in the key member definitions.
    /// An empty list of key values is permitted.
    ///
    @Override
    public <T extends AbstractEntity<DynamicEntityKey>> T new_composite(final Class<T> entityClass, final Object... keys) {
        final T entity = new_(entityClass);
        if (keys.length > 0) {
            // setting composite key fields
            final List<Field> fieldList = Finder.getKeyMembers(entityClass);
            if (fieldList.size() != keys.length) {
                throw new DomainDrivenTestException(format(ERR_INVALID_NUMBER_OF_KEY_VALUES, keys.length, fieldList.size()));
            }
            for (int index = 0; index < fieldList.size(); index++) {
                final Field keyField = fieldList.get(index);
                final Object keyValue = keys[index];
                entity.set(keyField.getName(), keyValue);
            }
        }
        return entity;
    }

    /// Instantiates a new entity based solely on the provided type, resulting in a completely empty instance with no properties assigned.
    ///
    @Override
    public <T extends AbstractEntity<K>, K extends Comparable<?>> T new_(final Class<T> entityClass) {
        final IEntityDao<T> co = co$(entityClass);
        return co != null ? co.new_() : factory.newEntity(entityClass);
    }

    //------------------------ @SessionRequired support ------------------------//

    @Override
    public Session getSession() {
        if (session == null) {
            throw new DomainDrivenTestException(ERR_MISSING_SESSION);
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
            throw new DomainDrivenTestException("Transaction GUID is missing.");
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
