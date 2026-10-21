package ua.com.fielden.platform.test.runners;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import ua.com.fielden.platform.continuation.NeedMoreDataStorage;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.test.*;
import ua.com.fielden.platform.test.exceptions.DomainDrivenTestException;

import java.lang.reflect.Constructor;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.Reflector.assignStatic;
import static ua.com.fielden.platform.test.DbCreator.ddlScriptFileName;

/// The domain test case runner responsible for instantiating and initializing domain test cases.
///
/// * Supports [WithDbVersion].
///
/// * Supports [RequireTimeZone].
///
/// * Runs test cases with a test configuration created from the DB connectivity properties of the runner (see [#mkDbProps(String)]).
///   Configurations are created once per JVM for each runner type, so a runner that adjusts the properties (e.g., to set the SSO authentication mode)
///   gives its test cases a configuration of their own, while the test cases of each other runner share theirs.
///   All configurations in a JVM share the test database and its schema, which is created once.
///
public abstract class AbstractDomainDrivenTestCaseRunner extends BlockJUnit4ClassRunner  {

    private static final String
            INFO_TEST_IGNORED_DUE_TO_DB_VERSION = "Test [%s] is ignored because it requires one of databases %s while the current one is [%s].",
            ERR_INVALID_TYPE = "Test case [%s] should extend [%s].",
            ERR_MISSING_DB_CREATOR = "DbCreator type was not provided, but is required.",
            ERR_FAILED_TO_CREATE_TEST_CONFIGURATION = "Could not create test configuration.",
            INFO_TEST_IGNORED_DUE_TO_TIME_ZONE = "Test [%s] is ignored because it requires time zone [%s] while the actual is [%s].",
            ERR_INVALID_TIME_ZONE = "Test [%s] specifies an unrecognised time zone [%s] in @%s.";

    public final Logger logger = getLogger(getClass());

    /// Test configurations, created once per JVM for each runner type, as their creation is computationally intensive.
    /// The runner type determines the DB connectivity properties of a configuration, as [#mkDbProps(String)] is invoked during construction of the runner, with the database URI of the JVM.
    /// This approach is safe for single-threaded test execution; parallelisation should be achieved by forking JVM processes.
    ///
    private static final Map<Class<? extends AbstractDomainDrivenTestCaseRunner>, TestConfig> testConfigs = new HashMap<>();

    /// A test configuration together with the DB connectivity properties it was created with.
    ///
    private record TestConfig(Properties dbProps, IDomainDrivenTestCaseConfiguration config, IDbVersionProvider dbVersionProvider) {}

    /// A single DDL script is needed for all instances of all test cases.
    /// The intent is to create and cache it as a static variable upon instantiation of the first runner instance.
    /// This approach is safe for single-threaded test execution; parallelisation should be achieved by forking JVM processes.
    ///
    private static final List<String> ddlScript = new ArrayList<>();
    
    /// The URI of the database to be used for testing.
    public final String databaseUri;
    
    private final DbCreator dbCreator;
    private final TestConfig testConfig;

    public AbstractDomainDrivenTestCaseRunner(
            final Class<?> klass, 
            final Class<? extends DbCreator> dbCreatorType, 
            final Optional<IDomainDrivenTestCaseConfiguration> testConfig) throws Exception {
        super(klass);
        // assert if the provided test case is supported
        if (!AbstractDomainDrivenTestCase.class.isAssignableFrom(klass)) {
            throw new DomainDrivenTestException(ERR_INVALID_TYPE.formatted(klass.getName(), AbstractDomainDrivenTestCase.class.getName()));
        }
       
        if (dbCreatorType == null) {
            throw new DomainDrivenTestException(ERR_MISSING_DB_CREATOR);
        }
        
        // databaseUri value should be specified in POM or come from the command line
        // however, need to provide a sensible default not to force developers to specify this parameter for each test case in IDE, assuming H2 is the default
        if (isEmpty(System.getProperty("databaseUri"))) {
            databaseUri = "./src/test/resources/db/DEFAULT_TEST_DB";
        } else {
            databaseUri = System.getProperty("databaseUri");
        }
        
        // check if loadDdlScriptFromFile is specified 
        final boolean loadDdlScriptFromFile;
        if (isEmpty(System.getProperty("loadDdlScriptFromFile"))) {
            loadDdlScriptFromFile = false;
        } else {
            loadDdlScriptFromFile = Boolean.parseBoolean(System.getProperty("loadDdlScriptFromFile"));
        }
        
        // check if saveDdlScriptToFile is specified
        final boolean saveScriptsToFile;
        if (isEmpty(System.getProperty("saveScriptsToFile"))) {
            saveScriptsToFile = false;
        } else {
            saveScriptsToFile = Boolean.parseBoolean(System.getProperty("saveScriptsToFile"));
        }

        final boolean loadDataScriptFromFile;
        if (isEmpty(System.getProperty("loadDataScriptFromFile"))) {
            loadDataScriptFromFile = false;
        } else {
            loadDataScriptFromFile = Boolean.parseBoolean(System.getProperty("loadDataScriptFromFile"));
        }

        logger.info(() -> "Running [%s] with loadDdlScriptFromFile = [%s], saveScriptsToFile = [%s], loadDataScriptFromFile = [%s] and  databaseUri = [%s]".formatted(klass, loadDdlScriptFromFile, saveScriptsToFile, loadDataScriptFromFile, databaseUri));

        // Obtain the test configuration for the type of this runner, creating it if this is the first runner of that type.
        this.testConfig = testConfigs.computeIfAbsent(getClass(), _ -> {
            final Properties dbProps = mkDbProps(databaseUri);
            final var config = testConfig.orElseGet(() -> createConfig(dbProps));
            return new TestConfig(dbProps, config, config.getInstance(IDbVersionProvider.class));
        });
        // Test cases can be instantiated directly after the runner is created (e.g., for data population), hence the static state is assigned right away.
        useTestConfig(this.testConfig);

        // try loading the DDL script if applicable
        final boolean execDdlScripts = ddlScript.isEmpty();
        if (ddlScript.isEmpty() && loadDdlScriptFromFile) {
            logger.info(() -> "Loading DDL scripts from [%s]... ".formatted(ddlScriptFileName));
            ddlScript.addAll(DbCreator.loadScriptFromFile(ddlScriptFileName));
            logger.info(() -> "Loaded [%s] DDL scripts... if 0 then it will be generated...".formatted(ddlScript.size()));
        }

        // get constructor for instantiation of DB creator and instantiate it 
        final Constructor<? extends DbCreator> constructor = dbCreatorType.getConstructor(klass.getClass(), Properties.class, IDomainDrivenTestCaseConfiguration.class, List.class, boolean.class);
        this.dbCreator = constructor.newInstance(klass, this.testConfig.dbProps(), this.testConfig.config(), ddlScript, execDdlScripts);

        if (saveScriptsToFile) {
            saveDdlScript();
        }
    }

    private AbstractDomainDrivenTestCaseRunner saveDdlScript() {
        logger.info(() -> "Saving [%s] DDL scripts to [%s].".formatted(ddlScript.size(), ddlScriptFileName));
        DbCreator.saveScriptToFile(ddlScript, ddlScriptFileName);
        return this;
    }

    /// Creates DB connectivity properties in terms of Hibernate properties.
    /// The value for the property `hibernate.connection.url` should include `%s` as a placeholder for the database location and name.
    ///
    /// Examples:
    /// - `jdbc:sqlserver:%s;queryTimeout=30`, where `%s` is replaced with a value like `//192.168.1.142:1433;database=TEST_DB`
    /// - `jdbc:h2:%s;INIT=SET REFERENTIAL_INTEGRITY FALSE`, where `%s` is replaced with a value like `./src/test/resources/db/TEST_DB`
    ///
    /// @param dbUri the database location and name
    ///
    protected abstract Properties mkDbProps(final String dbUri);

    @Override
    public Object createTest() {
        final Class<?> testCaseType = getTestClass().getJavaClass();
        final AbstractDomainDrivenTestCase testCase = (AbstractDomainDrivenTestCase) testConfig.config().getInstance(testCaseType);
        return testCase.setDbCreator(dbCreator);
    }

    /// A routine to clean up the database when it is no longer needed.
    /// For example, in the case of H2, the database file can be deleted.
    ///
    public void dbCleanUp() {
    }

    /// Override to add custom logic at the very end of test case execution.
    /// This is primarily used to perform clean-up tasks.
    ///
    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        final Statement defaultSt = super.classBlock(notifier);

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // The static state of test cases needs to correspond to the configuration of this runner, as runners with other configurations may have run before.
                useTestConfig(testConfig);
                // Execute the default work, which includes running all the tests.
                defaultSt.evaluate();
                
                // now let's do some clean up work...
                dbCleanUp();
            };
        };
    }

    /// Runs an individual test within the scope of an empty [NeedMoreDataStorage],
    /// unless the test is annotated with [SkipNeedMoreDataStorageBinding].
    ///
    /// This ensures that tests using [CommonEntityDao#setMoreData(String, IContinuationData)]
    /// do not fail due to the absence of a bound scoped storage.
    ///
    /// If [#isIgnored(FrameworkMethod)] raises a [DomainDrivenTestException] (e.g., due to an invalid [RequireTimeZone] value),
    /// the error is reported as a failure of this method alone.
    /// This is deliberate: the exception would otherwise propagate up to [org.junit.runners.ParentRunner], which reports it as a
    /// class-level failure and aborts all remaining methods in the class, including those unrelated to the misconfiguration.
    ///
    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        try {
            if (!method.getMethod().isAnnotationPresent(SkipNeedMoreDataStorageBinding.class)) {
                NeedMoreDataStorage.runWithMoreData(Map.of(), () -> super.runChild(method, notifier));
            }
            else {
                super.runChild(method, notifier);
            }
        } catch (final DomainDrivenTestException ex) {
            // Only `isIgnored` can propagate this exception out of `super.runChild`:
            // a test body's exceptions are caught and reported by JUnit's `runLeaf`, never rethrown.
            final Description description = describeChild(method);
            notifier.fireTestStarted(description);
            notifier.fireTestFailure(new Failure(description, ex));
            notifier.fireTestFinished(description);
        }
    }

    @Override
    protected boolean isIgnored(final FrameworkMethod child) {
        if (super.isIgnored(child)) {
            return true;
        }

        final var atWithDbVersion = child.getAnnotation(WithDbVersion.class);
        final var dbVersionMatches = atWithDbVersion == null || ArrayUtils.contains(atWithDbVersion.value(), testConfig.dbVersionProvider().dbVersion());
        if (!dbVersionMatches) {
            logger.info(() -> INFO_TEST_IGNORED_DUE_TO_DB_VERSION.formatted(
                              methodId(child),
                              Arrays.toString(atWithDbVersion.value()),
                              testConfig.dbVersionProvider().dbVersion()));
            return true;
        }

        return !hasRequiredTimeZone(child);
    }

    private boolean hasRequiredTimeZone(final FrameworkMethod child) {
        final var atRequireTimeZone = child.getAnnotation(RequireTimeZone.class);
        if (atRequireTimeZone == null) {
            return true;
        }

        final ZoneId zoneId;
        try {
            zoneId = ZoneId.of(atRequireTimeZone.value());
        } catch (final DateTimeException ex) {
            // An unparseable time zone is a programming error (e.g., a typo), not an environmental condition.
            // Fail loudly rather than silently ignoring the test, which would otherwise hide the mistake in every environment.
            throw new DomainDrivenTestException(
                    ERR_INVALID_TIME_ZONE.formatted(
                            methodId(child),
                            atRequireTimeZone.value(),
                            RequireTimeZone.class.getSimpleName()),
                    ex);
        }

        // Compare by zone rules rather than by ID (i.e. not `TimeZone.equals`, which compares IDs).
        // This way, equivalent zones are treated as matching -- e.g. `UTC` and `Etc/UTC`, or `America/New_York` and `US/Eastern`.
        // It matters in practice because CI/containerised JVMs commonly resolve the default zone to an alias such as `Etc/UTC`,
        // which would otherwise cause a test annotated with `@RequireTimeZone("UTC")` to be silently ignored.
        if (!zoneId.getRules().equals(ZoneId.systemDefault().getRules())) {
            logger.info(() -> INFO_TEST_IGNORED_DUE_TO_TIME_ZONE.formatted(
                    methodId(child),
                    atRequireTimeZone.value(),
                    ZoneId.systemDefault()));
            return false;
        }

        return true;
    }

    /// Returns a human-readable identifier for `method`, in the form `SimpleDeclaringClassName.methodName`, for use in log and error messages.
    ///
    private static String methodId(final FrameworkMethod method) {
        return "%s.%s".formatted(method.getDeclaringClass().getSimpleName(), method.getName());
    }

    /// A helper function to instantiate a test case configuration as specified in `props` under the property `"config.domain"`.
    ///
    private static IDomainDrivenTestCaseConfiguration createConfig(final Properties props) {
        try {
            final String configClassName = props.getProperty("config.domain");
            final Class<IDomainDrivenTestCaseConfiguration> type = (Class<IDomainDrivenTestCaseConfiguration>) Class.forName(configClassName);
            final Constructor<IDomainDrivenTestCaseConfiguration> constructor = type.getConstructor(Properties.class);
            return constructor.newInstance(props);
        } catch (final Exception ex) {
            throw new DomainDrivenTestException(ERR_FAILED_TO_CREATE_TEST_CONFIGURATION, ex);
        }
    }

    /// Assigns the static state of [AbstractDomainDrivenTestCase], through which test cases access `testConfig`.
    ///
    private static void useTestConfig(final TestConfig testConfig) throws ReflectiveOperationException {
        final IDomainDrivenTestCaseConfiguration config = testConfig.config();
        final Function<Class<?>, Object> instFun = config::getInstance;
        assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("instantiator"), instFun);
        assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("coFinder"), config.getInstance(ICompanionObjectFinder.class));
        assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("factory"), config.getInstance(EntityFactory.class));
    }

}
