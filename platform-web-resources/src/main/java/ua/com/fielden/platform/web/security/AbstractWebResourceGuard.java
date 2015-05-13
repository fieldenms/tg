package ua.com.fielden.platform.web.security;

import static java.lang.String.format;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.security.ChallengeAuthenticator;

import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.User;

import com.google.inject.Injector;

/**
 * This is a guard that is based on the new TG authentication scheme, developed as part of the Web UI initiative. It it used to restrict access to sensitive web resources.
 * <p>
 * This type is abstract. The only part that is abstract in it, is the way for obtaining current user. Applications and unit test may need to have different ways for determining
 * current users. Method {@link #getUser()} needs to be implemented to provide a currently logged in user.
 *
 * @author TG Team
 *
 */
public abstract class AbstractWebResourceGuard extends ChallengeAuthenticator {
    private final Logger logger = Logger.getLogger(AbstractWebResourceGuard.class);
    public static final String AUTHENTICATOR_COOKIE_NAME = "authenticator";
    protected final Injector injector;

    /**
     * Principle constructor.
     *
     * @param context
     * @param injector
     * @throws IllegalArgumentException
     */
    public AbstractWebResourceGuard(final Context context, final Injector injector) throws IllegalArgumentException {
        super(context, ChallengeScheme.CUSTOM, "TG");
        if (injector == null) {
            throw new IllegalArgumentException("Injector is required.");
        }
        this.injector = injector;
        setRechallenging(false);
    }

    @Override
    public boolean authenticate(final Request request, final Response response) {
        try {
            logger.debug(format("Starting request authentication to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));
            final Cookie cookie = request.getCookies().getFirst(AUTHENTICATOR_COOKIE_NAME);
            if (cookie == null) {
                logger.warn(format("Authenticator cookie is missing for a request to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));
                forbid(response);
                return false;
            }

            final String authenticator = cookie.getValue();
            if (StringUtils.isEmpty(authenticator)) {
                logger.warn(format("Authenticator value is missing for a request to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));
                forbid(response);
                return false;
            }

            final Authenticator auth = fromString(authenticator);
            final IUserSession coUserSession = injector.getInstance(IUserSession.class);
            final Optional<UserSession> session = coUserSession.currentSession(getUser(auth.username), authenticator);
            if (!session.isPresent()) {
                logger.warn(format("Authenticator validation failed for a request to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));
                forbid(response);
                return false;
            }

            assignAuthenticatingCookie(session.get().getAuthenticator().get().toString(), response);

        } catch (final Exception ex) {
            // in case of any internal exception forbid the request
            forbid(response);
            logger.fatal(ex);
            return false;
        }

        return true;
    }

    /**
     * A convenient method that creates an authenticating cookie based on the provided authenticator and associates it with the specified HTTP response.
     *
     * @param authenticator
     * @param response
     */
    public static void assignAuthenticatingCookie(final String authenticator, final Response response) {
        // create a cookie that will carry an updated authenticator back to the client for further use
        // it is important to note that the time that will be used by further processing of this request is not known
        // and thus is not factored in for session authentication time frame
        // this means that if the processing time exceeds the session expiration time then the next request after this would render invalid, requiring explicit authentication
        // on the one hand this is potentially limiting for untrusted devices, but for trusted devices this should not a problem
        // on the other hand, it might server as an additional security level, limiting computationally intensive requests being send from untrusted devices
        final CookieSetting newCookie = new CookieSetting(1, AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
        // have to set HttpOnly header, which informs the browser that only the originating server should be able to access the cookie value (hidden for JS access)
        // ensures client side security of authenticators
        newCookie.setAccessRestricted(true);
        // just in case remove any cookies, which is safe as the guard would be the first in the line of request processing logic
        response.getCookieSettings().clear();
        // finally associate the refreshed authenticator with the response
        response.getCookieSettings().add(newCookie);
    }

    /**
     * Should be implemented in accordance with requirements for obtaining the current user by name.
     *
     * @return
     */
    protected abstract User getUser(final String username);
}
