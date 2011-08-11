package ua.com.fielden.platform.web.test;

import java.io.FileInputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;

import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.test.DbDrivenTestCase;

/**
 * This is a base class for web-driven testing of resources and RAOs. It is responsible for auto-configuration of a web-server listening on port 9000.
 * <p>
 * Descendants of this class should implement method {@link #getDataSetPaths()} similarly as for {@link DbDrivenTestCase}.
 * <p>
 * <code>WebBasedTestCase<code> is also a restlet application. The web resources that need to be tested or used for testing should be bound by overriding method {@link Application#getRoot()}.
 * <p>
 * An instance of pre-configured {@link RestClientUtil} is provided for the use by descendants requiring instantiation of RAO classes.
 * <p>
 * Effectively each web-drive test case is a little web-application.
 * 
 * @author TG Team
 * 
 */
public abstract class WebBasedTestCase extends Application {
    protected static final Component component = new Component();
    static {
	component.getServers().add(Protocol.HTTP, IWebDrivenTestCaseConfiguration.PORT);
	try {
	    component.start();
	} catch (final Exception e) {
	    System.out.println("Failed to start the test web component:");
	    e.printStackTrace();
	}
    }

    protected DbDrivenTestCase dbDrivenTestCase = new DbDrivenTestCase() {
	@Override
	protected String[] getDataSetPathsForInsert() {
	    return WebBasedTestCase.this.getDataSetPaths();
	}
    };

    protected final IWebDrivenTestCaseConfiguration config = createConfig();

    private static IWebDrivenTestCaseConfiguration createConfig() {
	try {
	    final Properties testProps = new Properties();
	    final FileInputStream in = new FileInputStream("src/test/resources/test.properties");
	    testProps.load(in);
	    in.close();
	    final String configClassName = testProps.getProperty("web-config");
	    final Class<IWebDrivenTestCaseConfiguration> type = (Class<IWebDrivenTestCaseConfiguration>) Class.forName(configClassName);
	    final IWebDrivenTestCaseConfiguration webConf = type.newInstance();
	    webConf.setDbDrivenTestConfiguration(DbDrivenTestCase.config);
	    return webConf;
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
    }

    @Before
    public void setUp() {
	try {
	    component.getDefaultHost().attach("/v1", this); // needed for application versioned resources
	    component.getDefaultHost().attach("/system", this); // needed for application unversioned resources such as authentication resource
	    if (getDataSetPaths() != null && getDataSetPaths().length > 0) {
		dbDrivenTestCase.setUp();
	    }
	} catch (final Exception e) {
	    e.printStackTrace();
	    System.exit(100);
	}
    }

    @After
    public void tearDown() {
	try {
	    component.getDefaultHost().detach(this);
	    if (getDataSetPaths() != null && getDataSetPaths().length > 0) {
		dbDrivenTestCase.tearDown();
	    }
	} catch (final Exception e) {
	    e.printStackTrace();
	    System.exit(100);
	}
    }

    /**
     * This method should be implemented in descendants in order to provide list of paths to datasets, which are to be used with the given test case (via invoking method
     * getDataSet()).
     * 
     * @return
     */
    protected abstract String[] getDataSetPaths();
}
