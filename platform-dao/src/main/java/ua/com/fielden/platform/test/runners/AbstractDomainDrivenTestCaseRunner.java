package ua.com.fielden.platform.test.runners;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Logger;
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
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.DbCreator;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.WithDbVersion;
import ua.com.fielden.platform.test.exceptions.DomainDrivenTestException;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.Reflector.assignStatic;
import static ua.com.fielden.platform.test.DbCreator.ddlScriptFileName;

/// The domain test case runner responsible for instantiating and initializing domain test cases.
///
public abstract class AbstractDomainDrivenTestCaseRunner extends BlockJUnit4ClassRunner  {

    private static final String
            INFO_TEST_IGNORED_DUE_TO_DB_VERSION = "Test [%s] is ignored because it requires one of databases %s while the current one is [%s].",
            ERR_INVALID_TYPE = "Test case [%s] should extend [%s].",
            ERR_MISSING_DB_CREATOR = "DbCreator type was not provided, but is required.",
            ERR_FAILED_TO_CREATE_TEST_CONFIGURATION = "Could not create test configuration.";

    public final Logger logger = getLogger(getClass());

    // The following two fields are initialised only once when the test configuration is created.
    // This avoids the memory- and CPU-intensive cost of repeating the initialisation.

    /// Properties for the establishment of a database connection via Hibernate.
    /// Required to instantiate [DbCreator].
    ///
    private static Properties dbProps;
    private static IDomainDrivenTestCaseConfiguration config;

    /// A single DDL script is needed for all instances of all test cases.
    /// The intent is to create and cache it as a static variable upon instantiation of the first runner instance.
    /// This approach is safe for single-threaded test execution; parallelisation should be achieved by forking JVM processes.
    ///
    private static final List<String> ddlScript = new ArrayList<>();
    
    /// The URI of the database to be used for testing.
    public final String databaseUri;
    
    private final DbCreator dbCreator;
    private static IDbVersionProvider dbVersionProvider;

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

        // let's construct and assign test configuration
        // this should occur only once per JVM instance as this is a computationally intensive operation
        // hence, caching of the produced value in the static variable
        // in the essence this is like a static initialization block that occurs during instantiation of the first test runner 
        if (config == null) {
            dbProps = mkDbProps(databaseUri);
            config = testConfig.orElseGet(() -> createConfig(dbProps));
            dbVersionProvider = config.getInstance(IDbVersionProvider.class);
            final Function<Class<?>, Object> instFun = type -> config.getInstance(type);
            assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("instantiator"), instFun);
            assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("coFinder"),
                         config.getInstance(ICompanionObjectFinder.class));
            assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("factory"),
                         config.getInstance(EntityFactory.class));
        }

        // try loading the DDL script if applicable
        final boolean execDdlScripts = ddlScript.isEmpty();
        if (ddlScript.isEmpty() && loadDdlScriptFromFile) {
            logger.info(() -> "Loading DDL scripts from [%s]... ".formatted(ddlScriptFileName));
            ddlScript.addAll(DbCreator.loadScriptFromFile(ddlScriptFileName));
            logger.info(() -> "Loaded [%s] DDL scripts... if 0 then it will be generated...".formatted(ddlScript.size()));
        }

        // get constructor for instantiation of DB creator and instantiate it 
        final Constructor<? extends DbCreator> constructor = dbCreatorType.getConstructor(klass.getClass(), Properties.class, IDomainDrivenTestCaseConfiguration.class, List.class, boolean.class);
        this.dbCreator = constructor.newInstance(klass, dbProps, config, ddlScript, execDdlScripts);

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
        final AbstractDomainDrivenTestCase testCase = (AbstractDomainDrivenTestCase) config.getInstance(testCaseType);
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
    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if (!method.getMethod().isAnnotationPresent(SkipNeedMoreDataStorageBinding.class)) {
            NeedMoreDataStorage.runWithMoreData(Map.of(), () -> super.runChild(method, notifier));
        }
        else {
            super.runChild(method, notifier);
        }
    }

    @Override
    protected boolean isIgnored(final FrameworkMethod child) {
        if (super.isIgnored(child)) {
            return true;
        }

        final var atWithDbVersion = child.getAnnotation(WithDbVersion.class);
        final var dbVersionMatches = atWithDbVersion == null || ArrayUtils.contains(atWithDbVersion.value(), dbVersionProvider.dbVersion());
        if (!dbVersionMatches) {
            logger.info(() -> INFO_TEST_IGNORED_DUE_TO_DB_VERSION.formatted(
                              "%s.%s".formatted(child.getDeclaringClass().getSimpleName(), child.getName()),
                              Arrays.toString(atWithDbVersion.value()),
                              dbVersionProvider.dbVersion()));
            return true;
        }

        return false;
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

}
