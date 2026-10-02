package ua.com.fielden.platform.web.test;

import org.apache.logging.log4j.Logger;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;

import static org.apache.logging.log4j.LogManager.getLogger;

/// Restlet application that can be used in tests.
///
/// The intended usage is illustrated with the following example.
///
/// ```java
/// static final TestWebApplication webApplication = new TestWebApplication();
///
/// @BeforeClass
/// public static void beforeClass() {
///     webApplication.start(PORT);
/// }
///
/// @AfterClass
/// public static void afterClass() {
///     webApplication.stop();
/// }
///
/// @Before
/// public void startUp() {
///     webApplication.attachWebApplication("/path", myWebApp);
/// }
///
/// @After
/// public void tearDown() {
///     webApplication.detachWebApplication(myWebApp);
/// }
/// ```
///
/// It is recommended to use a different `PORT` in each test class that uses [TestWebApplication] to avoid starting two servers
/// on the same port, which may occur when tests are distributed between multiple JVM forks.
///
public final class TestWebApplication {

    private static final Logger LOGGER = getLogger(TestWebApplication.class);

    private Component component;

    public void start(final int port) {
        if (component != null) {
            throw new InvalidStateException("Server already started");
        }
        component = new Component();
        component.getServers().add(Protocol.HTTP, port);
        // Jetty needs additional settings to react to a shutdown signal, sent to JVM.
        final var server = component.getServers().getFirst();
        final Series<Parameter> parameters = server.getContext().getParameters();

        // Parameters to ensure quick shutdown of the test server during unit testing.
        parameters.add("shutdown.timeout", "1");
        parameters.add("shutdown.gracefully", "true");

        try {
            component.start();
        } catch (final Exception e) {
            LOGGER.error("Failed to start the test web component.", e);
            component = null;
        }
    }

    public void stop() {
        if (component == null) {
            throw new InvalidStateException("Server not started");
        }
        try {
            component.stop();
        } catch (final Exception e) {
            LOGGER.error("Failed to stop the test web component.", e);
        }
        component = null;
    }

    public void attachWebApplication(final String prefix, final Restlet restlet) {
        if (component == null) {
            throw new InvalidStateException("Server not started");
        }
        try {
            component.getDefaultHost().attach(prefix, restlet);
        } catch (final Exception e) {
            LOGGER.error("Failed to attach web application.", e);
        }
    }

    public void detachWebApplication(final Restlet restlet) {
        if (component == null) {
            throw new InvalidStateException("Server not started");
        }
        try {
            component.getDefaultHost().detach(restlet);
        } catch (final Exception e) {
            LOGGER.error("Failed to detach web application.", e);
        }
    }

}
