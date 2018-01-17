package ua.com.fielden.platform.test.runners;

import static java.lang.String.format;

import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.Statement;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.DbCreator;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.exceptions.DomainDriventTestException;

/**
 * The domain test case runner that is responsible of instantiation and initialisation of domain test cases.
 * 
 * @author TG Team
 *
 */
public abstract class AbstractDomainDrivenTestCaseRunner extends BlockJUnit4ClassRunner  {

    protected final Logger logger = Logger.getLogger(getClass());
    
    // the following two properties must be static to perform their allocation only once due to its memory and CPU intencity
    private static Properties dbProps; // mainly used for db creation and population at the time of loading the test case classes
    private static IDomainDrivenTestCaseConfiguration config;
    
    /** 
     * Need one DDL script for all instances of all test cases.
     * The intent is to create and cache it as a static variable upon instantiation of the first runner instance.
     * This should be safe for a single threaded execution of tests -- parallelisation should occur by means of forking JVM processes. 
     */
    private static final List<String> ddlScript = new ArrayList<>();
    
    /**
     * The data script can be cached at the runner instance level due to the fact that the same data is used by all tests in the same test case,
     * and a single runner instance is used for all those tests.
     * 
     * TODO: it is yet to be utilised.
     */
    private final List<String> dataScript = new ArrayList<>();
    
    /**
     * The name of the database to be used for testing.
     */
    protected final String databaseUri;
    
    private final DbCreator dbCreator;
    private static ICompanionObjectFinder coFinder;
    private static EntityFactory factory;

    
    public AbstractDomainDrivenTestCaseRunner(final Class<?> klass, final Class<? extends DbCreator> dbCreatorType) throws Exception {
        super(klass);
        // assert if the provided test case is supported
        if (!AbstractDomainDrivenTestCase.class.isAssignableFrom(klass)) {
            throw new IllegalArgumentException(format("Test case [%s] should extend [%s].", klass.getName(), AbstractDomainDrivenTestCase.class.getName()));
        }
        
        // databaseUri value should be specified in POM or come from the command line
        // however, need to provide a sensible default not to force developers to specify this parameter for each test case in IDE, assuming H2 is the default
        if (StringUtils.isEmpty(System.getProperty("databaseUri"))) {
            databaseUri = "./src/test/resources/db/DEFAULT_TEST_DB";
        } else {
            databaseUri = System.getProperty("databaseUri");
        }

        logger.info("RUNNER for type: " + klass + " and db URI = " + databaseUri);

        // lets construct and assign test configuration
        // this should occur only once per JVM instance as this is a computationally intensive operation
        // hence, caching of the produced value in the static variable
        // in the essence this is like a static initialization block that occurs during instantiation of the first test runner 
        if (config == null) {
            dbProps = mkDbProps(databaseUri);
            config = createConfig(dbProps);
            coFinder = config.getInstance(ICompanionObjectFinder.class);
            factory = config.getInstance(EntityFactory.class);
            assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("instantiator"), 
                    new Function<Class<?>, Object>() {
                        @Override
                        public Object apply(Class<?> type) {
                            return config.getInstance(type);
                        }});
            assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("coFinder"), coFinder);
            assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("factory"), factory);
        }

        // get constructor for instantiation of DB creator and instantiate it 
        final Constructor<? extends DbCreator> constructor = dbCreatorType.getConstructor(klass.getClass(), Properties.class, IDomainDrivenTestCaseConfiguration.class, List.class);
        this.dbCreator = constructor.newInstance(klass, dbProps, config, ddlScript);
    }

    /**
     * Creates db connectivity properties in terms of Hibernate properties.
     * The value for property <code>hibernate.connection.url</code> should contain <code>%s</code> as a place holder for the database location and name.
     * For example:
     * <ul>
     * <li><code>jdbc:sqlserver:%s;queryTimeout=30</code>, where <code>%s</code> would be replaced with something like <code>//192.168.1.142:1433;database=TEST_DB</code>.
     * <li><code>jdbc:h2:%s;INIT=SET REFERENTIAL_INTEGRITY FALSE</code>, where <code>%s</code> would be replaced with something like <code>./src/test/resources/db/TEST_DB</code>.
     * </ul> 
     * @param dbUri -- the database location and name
     * 
     * @return
     */
    protected abstract Properties mkDbProps(final String dbUri);

    @Override
    protected Object createTest() throws Exception {
        final Class<?> testCaseType = getTestClass().getJavaClass();
        final AbstractDomainDrivenTestCase testCase = (AbstractDomainDrivenTestCase) config.getInstance(testCaseType);
        return testCase.setDbCreator(dbCreator);
    }
    
    /**
     * A routine to clean up the database once it is no longer needed. For example, in case of H2 the database file can be deleted.
     */
    protected void dbCleanUp() {
        dbCreator.closeConnetion();
    }
    
    /**
     * Override to add custom logic at the very end of test case execution.
     * Mainly this is needed to do the clean up work.
     */
    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        final Statement defaultSt = super.classBlock(notifier);

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // execute the dafault work, which includes running all the tests
                defaultSt.evaluate();
                
                // now let's do some clean up work...
                dbCleanUp();
            };
        };
    }
    
    /**
     * A helper function to instantiate test case configuration as specified in <code>src/test/resources/test.properties</code>, property <code>config-domain</code>.
     * 
     * @param props
     * @return
     */
    private static IDomainDrivenTestCaseConfiguration createConfig(final Properties props) {
        try {
            final Properties testProps = new Properties();
            try (final FileInputStream in = new FileInputStream("src/test/resources/test.properties")) {
                testProps.load(in);
            }

            final String configClassName = testProps.getProperty("config-domain");
            final Class<IDomainDrivenTestCaseConfiguration> type = (Class<IDomainDrivenTestCaseConfiguration>) Class.forName(configClassName);
            final Constructor<IDomainDrivenTestCaseConfiguration> constructor = type.getConstructor(Properties.class);
            return constructor.newInstance(props);
        } catch (final Exception ex) {
            throw new DomainDriventTestException("Could not create test configuration.", ex);
        }
    }

    /**
     * A helper function to assign value to a field.
     *  
     * @param field
     * @param newValue
     * @throws Exception
     */
    private static void assignStatic(final Field field, final Object newValue) throws Exception {
        field.setAccessible(true);
        field.set(null, newValue);
     }

}
