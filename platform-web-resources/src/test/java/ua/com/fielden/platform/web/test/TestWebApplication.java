package ua.com.fielden.platform.web.test;

import org.apache.logging.log4j.Logger;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Restlet application that can be used in tests.
 * <p>
 * This class can be used statically for attaching/detaching restlets from the running server via:
 * <ul>
 *   <li>{@link #attachWebApplication(String, Restlet)}
 *   <li>{@link #detachWebApplication(Restlet)}
 * </ul>
 * <p>
 * <code>WebAppliction</code> in the name of these method refers to a test web application, which should incorporate the routing for all its web resources.
 * <p>
 *
 * @author TG Team
 */
public final class TestWebApplication {

    private static final Logger LOGGER = getLogger(TestWebApplication.class);
    public static final int PORT = 9042;
    static final Component component = new Component();

    static {
        component.getServers().add(Protocol.HTTP, PORT);
        try {
            component.start();
        } catch (final Exception e) {
            LOGGER.error("Failed to start the test web component.", e);
        }
    }

    /**
     * Attaches the provided restlet to the running server at the specified path prefix.
     */
    public static void attachWebApplication(final String pathPrefix, final Restlet restlet) {
        try {
            component.getDefaultHost().attach(pathPrefix, restlet);
        } catch (final Exception e) {
            LOGGER.fatal("Failed to attach web application.", e);
            System.exit(100);
        }

    }

    /**
     * Removes web application with all its routes from the test web server.
     */
    public static void detachWebApplication(final Restlet restlet) {
        try {
            component.getDefaultHost().detach(restlet);
        } catch (final Exception e) {
            LOGGER.fatal("Failed to detach web application.", e);
            System.exit(100);
        }
    }

    private TestWebApplication() {}

}
