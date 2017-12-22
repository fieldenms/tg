package ua.com.fielden.platform.test;

import static ua.com.fielden.platform.test.AbstractDomainDrivenTestCase.*;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * The domain test case runner that is responsible of instantiation and initialisation of domain test cases.
 * 
 * @author TG Team
 *
 */
public class DomainDrivenTestCaseRunner extends BlockJUnit4ClassRunner  {

    public DomainDrivenTestCaseRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }
    
    @Override
    protected Object createTest() throws Exception {
        final Class<?> testCaseType = getTestClass().getJavaClass();
        //System.out.printf("CREATING TEST [%s]\n", testCaseType.getSimpleName());
        return dbCreator(uuid()).config.getInstance(testCaseType);
    }

}
