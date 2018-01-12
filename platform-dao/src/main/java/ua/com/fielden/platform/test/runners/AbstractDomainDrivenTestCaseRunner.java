package ua.com.fielden.platform.test.runners;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.Statement;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.test.AbstractDomainDrivenTestCase;
import ua.com.fielden.platform.test.DbCreator;

/**
 * The domain test case runner that is responsible of instantiation and initialisation of domain test cases.
 * 
 * @author TG Team
 *
 */
public class AbstractDomainDrivenTestCaseRunner extends BlockJUnit4ClassRunner  {

    /** 
     * Need one DDL script for all instances of all test cases.
     * The intent is to create and cache it as a static variable upon instantiation of the first runner instance.
     * This should be safe for a single threaded execution of tests -- parallelisation should occur by means of forking JVM processes. 
     */
    private static final List<String> ddlScript = new ArrayList<>();
    
    /**
     * The data script can be cached at the runner instance level due to the fact that the same data is used by all tests in the same test case,
     * and a single runner instance is used for all those tests.
     */
    private final List<String> dataScript = new ArrayList<>();
    
    /**
     * The name of the database to be used for testing.
     */
    private final String databaseUri;
    
    private final DbCreator dbCreator;
    private static ICompanionObjectFinder coFinder;
    private static EntityFactory factory;

    
    public AbstractDomainDrivenTestCaseRunner(final Class<?> klass) throws Exception {
        super(klass);
        // assert if the provided test case is supported
        if (!AbstractDomainDrivenTestCase.class.isAssignableFrom(klass)) {
            throw new IllegalArgumentException(format("Test case [%s] should extend [%s].", klass.getName(), AbstractDomainDrivenTestCase.class.getName()));
        }
        // the following values should be specified in POM
        // TODO remove default name, which is just 
        databaseUri = System.getProperty("databaseUri");
        if (StringUtils.isEmpty(databaseUri)) {
            throw new IllegalArgumentException(format("Test case [%s] is missing system property \"databaseUri\".", klass.getName()));
        }

        System.out.println("RUNNER for type: " + klass + " and db = " + databaseUri);
        
        this.dbCreator = new DbCreator(databaseUri, ddlScript);
        if (coFinder == null) {
            coFinder = DbCreator.config.getInstance(ICompanionObjectFinder.class);
            factory = DbCreator.config.getEntityFactory();
            assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("dbCreator"), dbCreator);
            assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("coFinder"), coFinder);
            assignStatic(AbstractDomainDrivenTestCase.class.getDeclaredField("factory"), factory);
        }
        
    }
    
    @Override
    protected Object createTest() throws Exception {
        final Class<?> testCaseType = getTestClass().getJavaClass();
        final AbstractDomainDrivenTestCase testCase = (AbstractDomainDrivenTestCase) dbCreator.config.getInstance(testCaseType);
        return testCase;
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
                final Path rootPath = Paths.get(DbCreator.baseDir);
                try (final Stream<Path> paths = Files.walk(rootPath)) {
                    paths
                    .filter(path -> path.getFileName().toString().contains(dbCreator.dbName))
                        .map(Path::toFile)
                        .peek(file -> System.out.println(format("Removing %s", file.getName())))
                        .forEach(File::delete);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            };
        };
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
