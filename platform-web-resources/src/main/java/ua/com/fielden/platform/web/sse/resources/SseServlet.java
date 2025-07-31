package ua.com.fielden.platform.web.sse.resources;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.sse.EventSourceEmitter;
import ua.com.fielden.platform.web.sse.IEventSourceEmitterRegister;
import ua.com.fielden.platform.web.sse.RequestInfo;
import ua.com.fielden.platform.web.sse.exceptions.SseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME;

/// A Servlet that implements support for non-blocking async Server-Sent Eventing.
///
/// TG-based applications should use factory method [#addSseServlet] to create and add an SSE servlet to [ServletContextHandler].
///
public final class SseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String NO_SSE_UID = "no SSE UID";
    private static final Logger LOGGER = getLogger(SseServlet.class);

    private static final ScheduledExecutorService HEARTBEAT_SCHEDULER = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("SSE-heartbeat-%d").build());
    private static final int HEARTBEAT_FREQUENCY_IN_SECONDS = 10;

    private final IEventSourceEmitterRegister eseRegister;
    private final ICompanionObjectFinder coFinder;
    private final String hashingKey;
    private final SessionIdentifierGenerator crypto;

     /// A convenient factory method to create an SSE service.
     ///
     /// The method inspects application property `sse.enabled`.
     /// If true, creates, but does not start, a separate Jetty server instances with `SseServlet` added.
     ///
     /// The following properties can be provided:
     ///
     /// * `sse.enabled`, default value `true`.
     /// * `sse.jetty.port`, default value `8092`.
     /// * `sse.jetty.threadPool.maxThreads`, default value `10`.
     /// * `sse.jetty.threadPool.minThreads`, default value `1`.
     /// * `sse.jetty.threadPool.idleTimeout`, default value `60000`.
     /// * `sse.jetty.connector.acceptors`, default value `1`.
     ///
     public static Optional<Server> createSseService(
            final Properties props,
            final IEventSourceEmitterRegister eseRegister,
            final ICompanionObjectFinder coFinder,
            final @SessionHashingKey String hashingKey,
            final SessionIdentifierGenerator crypto,
            final Logger logger)
     {
        if (!Boolean.parseBoolean(props.getProperty("sse.enabled", "true"))) {
            logger.warn("SSE service is disabled. Attachments and auto-refresh capabilities won't work.");
            return empty();
        }
        final int port = parseInt(props.getProperty("sse.jetty.port", "8092"));
        final int maxThreads = parseInt(props.getProperty("sse.jetty.threadPool.maxThreads", "10"));
        final int minThreads = parseInt(props.getProperty("sse.jetty.threadPool.minThreads", "1"));
        final int idleTimeout = parseInt(props.getProperty("sse.jetty.threadPool.idleTimeout", "60000"));
        final int acceptors = parseInt(props.getProperty("sse.jetty.connector.acceptors", "1"));
        
        logger.info(() ->
                """
                Creating SSE service:
                    sse.jetty.port..............................%s
                    sse.jetty.threadPool.maxThreads.............%s
                    sse.jetty.threadPool.minThreads.............%s
                    sse.jetty.threadPool.idleTimeout............%s
                    sse.jetty.connector.acceptors...............%s
                """.formatted(port, maxThreads, minThreads, idleTimeout, acceptors));

        final QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        final Server server = new Server(threadPool);

        final ServerConnector http = new ServerConnector(server, acceptors /* acceptors */, -1 /* selectors */);
        http.setPort(port);
        server.addConnector(http);

        // We need to use a context handler since Jetty 12+.
        final ServletContextHandler contextHandler = new ServletContextHandler("/");
        server.setHandler(contextHandler);

        // SSE servlet instantiation, which still needs to be associated with a Jetty instance
        SseServlet.addSseServlet(contextHandler, eseRegister, coFinder, hashingKey, crypto);
        return of(server);
    }

    /// A factory method for instantiating and adding SSE servlet to `context`.
    ///
    private static void addSseServlet(
            final ServletContextHandler contextHandler,
            final IEventSourceEmitterRegister eseRegister,
            final ICompanionObjectFinder coFinder,
            final @SessionHashingKey String hashingKey,
            final SessionIdentifierGenerator crypto)
    {
        // instantiate this servlet
        final SseServlet sseServlet = new SseServlet(eseRegister, coFinder, hashingKey, crypto);
        // need to configure a servlet holder with async support
        final ServletHolder servletHolder = new ServletHolder(sseServlet);
        servletHolder.setAsyncSupported(true);
        // let's now bind the servlet to the default SSE path
        contextHandler.addServlet(servletHolder, "/sse/*");
    }

    private SseServlet(
            final IEventSourceEmitterRegister eseRegister,
            final ICompanionObjectFinder coFinder,
            final @SessionHashingKey String hashingKey,
            final SessionIdentifierGenerator crypto)
    {
        this.eseRegister = eseRegister;
        this.coFinder = coFinder;
        this.hashingKey = hashingKey;
        this.crypto = crypto;
    }

    @Override
    public void destroy() {
        LOGGER.info("Shutting down SSE service...");
        HEARTBEAT_SCHEDULER.shutdown();
        super.destroy();
    }

    /// Responsible for processing requests for establishing SSE communication channels.
    ///
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String sseIdString = Optional.ofNullable(request.getPathInfo()).filter(pi -> pi.length() > 1).map(pi -> pi.substring(1)).orElse(NO_SSE_UID);
        if (NO_SSE_UID.equals(sseIdString)) {
            LOGGER.debug("SSE request: missing UID.");
            super.doGet(request, response);
            return;
        }

        final Optional<User> maybeUser = verifyAuthenticatorAndGetUser(request);
        if (maybeUser.isEmpty()) {
            LOGGER.debug("SSE request: no user identified, rejecting request.");
            super.doGet(request, response);
            return;
        }

        final User user = maybeUser.get();
        // Any errors, which may occur during subscription or a normal lifecycle after, should result in deregistering and closing of the emitter, created during this request.
        try {
            eseRegister.registerEmitter(user, sseIdString, () -> {
                try {
                    makeHandshake(response);
                    // create an asynchronous context for pushing messages out to the subscribed client
                    final AsyncContext async = request.startAsync();
                    // Infinite timeout because the continuation is never resumed,
                    // but only completed on close
                    async.setTimeout(0);

                    return new EventSourceEmitter(async, new RequestInfo(request), HEARTBEAT_SCHEDULER, HEARTBEAT_FREQUENCY_IN_SECONDS, () -> eseRegister.deregisterEmitter(user, sseIdString) /* event source close callback */);
                } catch (final IOException ex) {
                    throw new SseException("Could not create a new SSE emitter.", ex);
                }
            }).ifFailure(Result::throwRuntime);
            LOGGER.info(() -> "SSE subscription for client [%s, %s] completed.".formatted(user, sseIdString));
        } catch (final Exception ex) {
            LOGGER.error(ex);
            eseRegister.deregisterEmitter(user, sseIdString);
            LOGGER.warn(() -> "SSE subscription for client [%s, %s] did not complete.".formatted(user, sseIdString), ex);
            throw new ServletException(ex);
        }
    }

    /// Obtains and verifies authenticator from the request, and returns a corresponding user if successful.
    ///
    /// @param request
    ///
    private Optional<User> verifyAuthenticatorAndGetUser(final HttpServletRequest request) {
        final Optional<Authenticator> oAuth = extractAuthenticator(request);
        if (oAuth.isEmpty()) {
            LOGGER.debug("SSE request: unauthenticated.");
            return empty();
        }
        final Authenticator auth = oAuth.get();
        try {
            if (!auth.hash.equals(crypto.calculateRFC2104HMAC(auth.token, hashingKey))) {
                LOGGER.debug(() -> "SSE request: authenticator %s cannot be verified. A tempered authenticator is suspected.".formatted(auth));
                return empty();
            }
        } catch (final SignatureException ex) {
            LOGGER.debug("SSE request: provided authenticator cannot be verified. SignatureException was thrown.");
            return empty();
        }
        
        final IUser coUser = coFinder.find(User.class, true);
        final User user = coUser.findUser(auth.username);
        return user != null && user.isActive() ? of(user) : empty();
    }

    /// Adjusts `response` to be suitable for SSE communication.
    /// This effectively completes the server-end part of the handshake to establish an SSE connection.
    ///
    private void makeHandshake(final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/event-stream");
        // By adding this header, and not closing the connection,
        // we disable HTTP chunking, and we can use write()+flush()
        // to send data in the text/event-stream protocol
        response.addHeader("Connection", "close");
        response.flushBuffer();
    }

    private static Optional<Authenticator> extractAuthenticator(final HttpServletRequest request) {
        // If request has no cookies, getCookies() returns null instead of an empty array.
        if (request.getCookies() == null) {
            return empty();
        }
        // convert non-empty authenticating cookies to authenticators and get the most recent one by expiry date...
        return Stream.of(request.getCookies())
                .filter(c -> AUTHENTICATOR_COOKIE_NAME.equals(c.getName()) && !StringUtils.isEmpty(c.getValue()))
                .map(c -> fromString(c.getValue()))
                .max((auth1, auth2) -> Long.compare(auth1.version, auth2.version));
    }

}