package ua.com.fielden.platform.web.sse.resources;

import static ua.com.fielden.platform.security.session.Authenticator.fromString;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.application.RequestInfo;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.sse.EventSourceEmitter;
import ua.com.fielden.platform.web.sse.IEventSourceEmitterRegister;
import ua.com.fielden.platform.web.sse.exceptions.SseException;

public final class SseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String NO_SSE_UID = "no SSE UID";
    private static final Logger logger = Logger.getLogger(SseServlet.class);

    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;
    private final IEventSourceEmitterRegister eseRegister;
    private final ICompanionObjectFinder coFinder;

    public SseServlet(final IEventSourceEmitterRegister eseRegister, final IDeviceProvider deviceProvider, final IDates dates, final IUserProvider userProvider, final ICompanionObjectFinder coFinder) {
        this.eseRegister = eseRegister;
        this.deviceProvider = deviceProvider;
        this.dates = dates;
        this.userProvider = userProvider;
        this.coFinder = coFinder;
    }

    public static Optional<Authenticator> extractAuthenticator(final HttpServletRequest request) {
        // convert non-empty authenticating cookies to authenticators and get the most recent one by expiry date...
        return Stream.of(request.getCookies())
                .filter(c -> AUTHENTICATOR_COOKIE_NAME.equals(c.getName()) && !StringUtils.isEmpty(c.getValue()))
                .map(c -> fromString(c.getValue()))
                .max((auth1, auth2) -> Long.compare(auth1.version, auth2.version));
    }

    
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        //        final ServletOutputStream out = response.getOutputStream();

        final String sseIdString = Optional.ofNullable(request.getPathInfo()).filter(pi -> pi.length() > 1).map(pi -> pi.substring(1, pi.length())).orElse(NO_SSE_UID);
        if (NO_SSE_UID.equals(sseIdString)) {
            throw new ServletException("No SSE UID was provided.");
        }
        
        final Optional<Authenticator> oAuth = extractAuthenticator(request);
        if (!oAuth.isPresent()) {
            throw new ServletException("Unauthenticated SSE request.");
        }
        // TODO check validity of the authenticator, no need to refresh a current session
        
        final IUser coUser = coFinder.find(User.class, true);
        final User user = userProvider.setUsername(oAuth.get().username, coUser).getUser();
        // any errors that may occur during subscription or a normal lifecycle after, should result in deregistering and closing of the emitter, created during this request
        try {
            final AtomicBoolean shouldKeepGoing = new AtomicBoolean(true); // TODO needs to be removed as not really needed with the non-blocking approach
            eseRegister.registerEmitter(user, sseIdString, () -> {
                try {
                    makeHandshake(response);
                    // create an asynchronous context for pushing messages out to the subscribed client
                    final AsyncContext async = request.startAsync();
                    // Infinite timeout because the continuation is never resumed,
                    // but only completed on close
                    async.setTimeout(0);

                    return new EventSourceEmitter(shouldKeepGoing, async, new RequestInfo(request), () -> eseRegister.deregisterEmitter(user, sseIdString) /* event source close callback */);
                } catch (final IOException ex) {
                    throw new SseException("Could not create a new SSE emitter.", ex);
                }
            }).ifFailure(Result::throwRuntime);
            logger.info(String.format("SSE subscription for client [%s, %s] completed.", user, sseIdString));
        } catch (final Exception ex) {
            logger.error(ex);
            eseRegister.deregisterEmitter(user, sseIdString);
            logger.warn(String.format("SSE subscription for client [%s, %s] did not complete.", user, sseIdString), ex);
            throw new ServletException(ex);
        }
    }

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

}