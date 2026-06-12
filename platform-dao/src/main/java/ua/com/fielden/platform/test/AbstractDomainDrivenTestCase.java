package ua.com.fielden.platform.test;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.ISessionEnabled;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.data.IDomainDrivenData;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.exceptions.DomainDrivenTestException;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.utils.DbUtils;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.query.DbVersion.ID_SEQUENCE_NAME;

/// This is a base class for all test cases in TG-based applications.
///
public abstract class AbstractDomainDrivenTestCase implements IDomainDrivenData, ISessionEnabled {

    private static final String ERR_SAVE_WITH_FETCH_NOT_IMPLEMENTED =
            "Save-with-fetch cannot be used because the companion for entity [%s] does not implement [" + ISaveWithFetch.class.getSimpleName() + "].";

    public static final String
            ERR_CANNOT_SAVE_NULL = "Null instances cannot be saved.",
            ERR_MISSING_COMPANION = "Could not find companion implementation for [%s].",
            ERR_PARSING_DATE = "Could not parse date [%s].",
            ERR_INVALID_NUMBER_OF_KEY_VALUES = "Number of key values is %s but should be %s.",
            ERR_MISSING_SESSION = "Session is missing, most likely, due to missing @SessionRequired annotation.";

    /// An offset for the ID sequence when it is being reset to prevent overlaps between test data population and intermediate
    /// data in test cases.
    /// It serves as extra headroom for unusual circumstances (e.g., manually adding statements to a data population script).
    ///
    public static final int ID_HEADROOM = 1_000_000;

    /// A fallback value for the ID seed used to restart the ID sequence before each test method.
    ///
    public static final long DEFAULT_ID_SEED = 10_000_000L;

    /// A system property (type: boolean) that enables test data pre-population scripts to be loaded from disk.
    /// When `true`, initial pre-population for Cached Mode tests ([#prePopulateDomain]) is skipped in favour of
    /// scripts created by a prior Cached Mode test run.
    ///
    public static final String LOAD_DATA_SCRIPT_FROM_FILE = "loadDataScriptFromFile";

    /// A system property (type: boolean) that enables test data pre-population scripts to be persisted to disk.
    /// When `true`, the scripts created during initial pre-population for Cached Mode tests ([#prePopulateDomain])
    /// and a DDL script are all persisted to disk.
    /// Those scripts can later be used by enabling [#LOAD_DATA_SCRIPT_FROM_FILE] and [#LOAD_DDL_SCRIPT_FROM_FILE].
    ///
    public static final String SAVE_SCRIPTS_TO_FILE = "saveScriptsToFile";

    /// A system property (type: boolean) that enables a DDL script to be loaded from disk.
    /// If `true`, but a DDL script does not exist, the DDL will be generated ad-hoc.
    ///
    public static final String LOAD_DDL_SCRIPT_FROM_FILE = "loadDdlScriptFromFile";

    /// A system property that specifies a URI to the database that will be used for testing.
    ///
    public static final String DATABASE_URI = "databaseUri";

    private static final DateTimeFormatter JODA_FORMAT_WITH_MINUTES = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter JODA_FORMAT_WITH_SECONDS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter JODA_FORMAT_WITH_MILLIS = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter JODA_FORMAT_DATE_ONLY = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final DateFormat DATE_FORMAT_WITH_MINUTES = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final DateFormat DATE_FORMAT_WITH_SECONDS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat DATE_FORMAT_WITH_MILLIS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateFormat DATE_FORMAT_DATE_ONLY = new SimpleDateFormat("yyyy-MM-dd");

    // The following three static fields are reflectively assigned only once, by the platform test runner.
    // We could make these fields non-static and @Inject, but a lot of application-level tests use getInstance() in field
    // initialisers, which requires `instantiator` to already have been injected in the parent constructor.
    // Field-level injection through @Inject occurs only after the constructor.
    // Therefore, such a change would break existing application tests.
    private static ICompanionObjectFinder coFinder;
    private static EntityFactory factory;
    private static Function<Class<?>, Object> instantiator;

    /// This map stores ID seeds for all test classes.
    /// Each test class is assigned an ID seed after its own dataset is populated, which occurs before the first test method is executed.
    /// Before each test method, the ID sequence is restarted with the ID seed of the corresponding test class to prevent
    /// ID conflicts between entities in the dataset and any intermediate entities persisted within a test method.
    ///
    /// No synchronisation is required as TG supports only synchronous execution of tests within one JVM.
    ///
    private static final Map<Class<?>, Long> idSeedMap = new HashMap<>();

    private DbCreator dbCreator;
    private Session session;
    private String transactionGuid;

    /// Should be implemented in order to provide domain driven data population.
    ///
    protected abstract void populateDomain();

    /// Controls caching of data produced by methods annotated with `@EnsureData`.
    ///
    public boolean skipCaching() {
        return useSavedDataPopulationScript() || saveDataPopulationScriptToFile();
    }

    /// Builds the JVM-wide pre-population dataset for tests in Cached Mode.
    ///
    /// Invoked once per JVM, before any test in Cached Mode runs, by the test framework when:
    /// - the current test class is in Cached Mode ([#skipCaching] returns `false`), AND
    /// - pre-population has not yet occurred in this JVM, AND
    /// - [#LOAD_DATA_SCRIPT_FROM_FILE] is disabled (i.e., scripts are being generated, not loaded from disk).
    ///
    /// If [#LOAD_DATA_SCRIPT_FROM_FILE] is enabled, this method is never called, and previously created scripts are used instead.
    ///
    /// Implementations should call all methods annotated with `@EnsureData`.
    /// Each such call is intercepted by the `@EnsureData` interceptor and recorded as an SQL script.
    /// Calling all such methods in this single procedure ensures that the IDs assigned to entities
    /// across different methods do not conflict, since they all draw from the ID sequence in one
    /// continuous run.
    ///
    /// After this method returns:
    /// - The framework captures the ID seed from the populated state.
    /// - If [#SAVE_SCRIPTS_TO_FILE] is enabled, the seed is persisted to disk as a sequence-restart script for a future JVM run with [#LOAD_DATA_SCRIPT_FROM_FILE] enabled.
    /// - The database is truncated; only the in-memory `@EnsureData` scripts remain, ready to be replayed by subsequent test classes.
    ///
    /// This method will be called with non-strict model verification active ([AbstractEntity#useNonStrictModelVerification]).
    ///
    public abstract void prePopulateDomain();

    /// Invoked by the test framework after [#prePopulateDomain] completes, but **before** the database is truncated.
    ///
    /// Implementations should release any state accumulated during pre-population that must be reset for the upcoming test methods.
    /// The typical use case is invoking the cleanup routine registered by the `@EnsureData` interceptor.
    ///
    public abstract void afterPrePopulation();

    @Before
    public final void beforeTest() {
        dbCreator.populateOrRestoreData(this);
        resetIdGenerator();
    }

    @SessionRequired
    protected void resetIdGenerator() {
        final var seed = idSeedMap.getOrDefault(this.getClass(), DEFAULT_ID_SEED);
        DbUtils.resetSequenceGenerator(ID_SEQUENCE_NAME, seed.intValue(), this.getSession());
    }

    protected void setIdSeed(final long value) {
        idSeedMap.put(this.getClass(), value);
    }

    protected @Nullable Long getIdSeed() {
        return idSeedMap.get(this.getClass());
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

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractEntity<?>> Either<Long, T> save(final T instance, final Optional<fetch<T>> maybeFetch) {
        if (instance == null) {
            throw new DomainDrivenTestException(ERR_CANNOT_SAVE_NULL);
        }
        final IEntityDao<T> pp = coFinder.find((Class<T>) instance.getType());
        if (pp == null) {
            throw new DomainDrivenTestException(ERR_MISSING_COMPANION.formatted(instance.getType().getSimpleName()));
        }
        if (pp instanceof ISaveWithFetch<?> it) {
            return ((ISaveWithFetch<T>) it).save(instance, maybeFetch);
        }
        else {
            throw new EntityCompanionException(ERR_SAVE_WITH_FETCH_NOT_IMPLEMENTED.formatted(instance.getType().getSimpleName()));
        }
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
            // Has millis part?
            if (dateTime.indexOf('.') > 0) {
                return DATE_FORMAT_WITH_MILLIS.parse(dateTime);
            }
            // Has time part without seconds?
            else if (dateTime.lastIndexOf(":") == 13) {
                return DATE_FORMAT_WITH_MINUTES.parse(dateTime);
            }
            // Has time part without millis?
            else if (dateTime.indexOf(":") > 0) {
                return DATE_FORMAT_WITH_SECONDS.parse(dateTime);
            }
            // Otherwise, assume the date without the time part.
            else {
                return DATE_FORMAT_DATE_ONLY.parse(dateTime);
            }
        } catch (final ParseException ex) {
            throw new DomainDrivenTestException(ERR_PARSING_DATE.formatted(dateTime), ex);
        }
    }

    @Override
    public final DateTime dateTime(final String dateTime) {
        try {
            // Has millis part?
            if (dateTime.indexOf('.') > 0) {
                return JODA_FORMAT_WITH_MILLIS.parseDateTime(dateTime);
            }
            // Has time part without seconds?
            else if (dateTime.lastIndexOf(":") == 13) {
                return JODA_FORMAT_WITH_MINUTES.parseDateTime(dateTime);
            }
            // Has time part without millis?
            else if (dateTime.indexOf(":") > 0) {
                return JODA_FORMAT_WITH_SECONDS.parseDateTime(dateTime);
            }
            // Otherwise, assume the date without the time part.
            else {
                return JODA_FORMAT_DATE_ONLY.parseDateTime(dateTime);
            }
        } catch (final Exception ex) {
            throw new DomainDrivenTestException(ERR_PARSING_DATE.formatted(dateTime), ex);
        }
    }

    @Override
    public <T extends AbstractEntity<K>, K extends Comparable<?>> T new_(final Class<T> entityClass, final K key, final String desc) {
        final T entity = new_(entityClass);
        entity.setKey(key);
        entity.set(DESC, desc);
        return entity;
    }

    @Override
    public <T extends AbstractEntity<K>, K extends Comparable<?>> T new_(final Class<T> entityClass, final K key) {
        final T entity = new_(entityClass);
        entity.setKey(key);
        return entity;
    }

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
