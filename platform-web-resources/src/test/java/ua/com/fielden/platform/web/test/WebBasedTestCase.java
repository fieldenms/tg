package ua.com.fielden.platform.web.test;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

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
 * <p>
 * <hr>
 * <br>
 * As of recently this class can also be used statically for attaching/detaching restlets from the running server.
 * This provides a way to reuse the current unit test server not only by extending the class, but also by means of directly
 * interacting with the unit test server, which is established statically and is the same for all test case instances,
 * by means of methods {@link WebBasedTestCase#attachWebApplication(Restlet)} and {@link WebBasedTestCase#detachWebApplication(Restlet)}.
 * <p>
 * The term <code>WebAppliction</code> in the name of these method refers to a test web application, which should incorporate the routing for all its web resources.
 * <p>
 * Since the move to domain driven testing where all data is populated through the domain, there is no really any
 * practical reason to extend {@link WebBasedTestCase}.
 *
 * @author TG Team
 *
 */
public abstract class WebBasedTestCase {
    protected static final Component component = new Component();
    public static final int PORT = 9042;
    static {
        component.getServers().add(Protocol.HTTP, PORT);
        try {
            component.start();
        } catch (final Exception e) {
            System.out.println("Failed to start the test web component:");
            e.printStackTrace();
        }
    }

    /**
     * Attaches the provided restlet to the running server at the specified path prefix.
     *
     * @param restlet
     */
    public static void attachWebApplication(final String pathPrefix, final Restlet restlet) {
        try {
            component.getDefaultHost().attach(pathPrefix, restlet);
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(100);
        }

    }

    /**
     * Removes web application with all its routes from the test web server.
     *
     * @param restlet
     */
    public static void detachWebApplication(final Restlet restlet) {
        try {
            component.getDefaultHost().detach(restlet);
        } catch (final Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }

}
