package ua.com.fielden.platform.web.sse.resources;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.annotations.SessionHashingKey;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.sse.EventSourceEmitter;
import ua.com.fielden.platform.web.sse.IEventSourceEmitterRegister;
import ua.com.fielden.platform.web.sse.RequestInfo;
import ua.com.fielden.platform.web.sse.exceptions.SseException;

/**
 * A Servlet that implements support for non-blocking async Server-Sent Eventing.
 * <p>
 * TG-based applications should use factory method {@link #addSseServlet(ServletHandler, IEventSourceEmitterRegister, IUserProvider, ICompanionObjectFinder, String, SessionIdentifierGenerator)}
 * to create and add an SSE servlet to {@link ServletHandler}.
 *
 * @author TG Team
 *
 */
public final class SseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String NO_SSE_UID = "no SSE UID";
    private static final Logger LOGGER = Logger.getLogger(SseServlet.class);

    private static final ScheduledExecutorService HEARTBEAT_SCHEDULER = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("SSE-heartbeat-%d").build());
    private static final int HEARTBEAT_FREQUENCY_IN_SECONDS = 5;

    private final IEventSourceEmitterRegister eseRegister;
    private final ICompanionObjectFinder coFinder;
    private final String hashingKey;
    private final SessionIdentifierGenerator crypto;

    /**
     * A factory method for instantiating and adding SSE servlet to {@code handler}.
     *
     * @param handler
     * @param eseRegister
     * @param coFinder
     * @param hashingKey
     * @param crypto
     */
    public static void addSseServlet(
            final ServletHandler handler,
            final IEventSourceEmitterRegister eseRegister,
            final ICompanionObjectFinder coFinder,
            final @SessionHashingKey String hashingKey,
            final SessionIdentifierGenerator crypto) {
        // instantiate this servlet
        final SseServlet sseServlet = new SseServlet(eseRegister, coFinder, hashingKey, crypto);
        // need to configure a servlet holder with async support
        final ServletHolder servletHolder = new ServletHolder(sseServlet);
        servletHolder.setAsyncSupported(true);
        // let's now bind the servlet to the default SSE path
        handler.addServletWithMapping(servletHolder, "/sse/*");
    }

    private SseServlet(
            final IEventSourceEmitterRegister eseRegister,
            final ICompanionObjectFinder coFinder,
            final @SessionHashingKey String hashingKey,
            final SessionIdentifierGenerator crypto) {
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

    /**
     * Responsible for processing requests for establishing SSE communication channels.
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String sseIdString = Optional.ofNullable(request.getPathInfo()).filter(pi -> pi.length() > 1).map(pi -> pi.substring(1, pi.length())).orElse(NO_SSE_UID);
        if (NO_SSE_UID.equals(sseIdString)) {
            LOGGER.debug("SSE request: missing UID.");
            super.doGet(request, response);
            return;
        }

        final Optional<User> maybeUser = verifyAuthenticatorAndGetUser(request);
        if (!maybeUser.isPresent()) {
            LOGGER.debug("SSE request: no user identified, rejecting request.");
            super.doGet(request, response);
            return;
        }

        final User user = maybeUser.get();
        // any errors that may occur during subscription or a normal lifecycle after, should result in deregistering and closing of the emitter, created during this request
        final AtomicBoolean wasRegisterd = new AtomicBoolean(false);
        try {
            LOGGER.info(format("Registering event emitter for web client [%s, %s].", user, sseIdString));
            eseRegister.registerEmitter(user, sseIdString, () -> {
                wasRegisterd.set(true);
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
            if (wasRegisterd.get()) {
                LOGGER.info(format("SSE subscription for client [%s, %s] completed.", user, sseIdString));
            } else {
                LOGGER.info(format("SSE subscription for client [%s, %s] already registered, skip.", user, sseIdString));
                super.doGet(request, response);
                return;
            }
        } catch (final Exception ex) {
            LOGGER.error(ex);
            if (wasRegisterd.get()) {
                eseRegister.deregisterEmitter(user, sseIdString);
                LOGGER.warn(format("SSE subscription for client [%s, %s] did not complete.", user, sseIdString), ex);
            }
            throw new ServletException(ex);
        }
    }

    /**
     * Obtains and verifies authenticator from the request, and returns a corresponding user if successful.
     *
     * @param request
     * @param response
     * @return
     * @throws ServletException
     * @throws IOException
     */
    private Optional<User> verifyAuthenticatorAndGetUser(final HttpServletRequest request) {
        final Optional<Authenticator> oAuth = extractAuthenticator(request);
        if (!oAuth.isPresent()) {
            LOGGER.debug("SSE request: unauthenticated.");
            return empty();
        }
        final Authenticator auth = oAuth.get();
        try {
            if (!auth.hash.equals(crypto.calculateRFC2104HMAC(auth.token, hashingKey))) {
                LOGGER.debug(format("SSE request: authenticator %s cannot be verified. A tempered authenticator is suspected.", auth));
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

    /**
     * Adjusts {@code response} to be suitable for SSE communication.
     * This effectively completes the server-end part of the handshake to establish an SSE connection.
     *
     * @param response
     * @throws IOException
     */
    protected void makeHandshake(final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/event-stream");
        // By adding this header, and not closing the connection,
        // we disable HTTP chunking, and we can use write()+flush()
        // to send data in the text/event-stream protocol
        response.addHeader("Connection", "close");
        response.flushBuffer();
    }

    protected static Optional<Authenticator> extractAuthenticator(final HttpServletRequest request) {
        // If request has not cookies, getCookies() returns null instead of an empty array.
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