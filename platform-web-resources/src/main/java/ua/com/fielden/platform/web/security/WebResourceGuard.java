package ua.com.fielden.platform.web.security;

import static java.lang.String.format;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.security.ChallengeAuthenticator;

import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;

/**
 * This is a guard that is based on the new TG authentication scheme, developed as part of the Web UI initiative.
 * It it used to restrict access to sensitive web resources.
 *
 * @author TG Team
 *
 */
public class WebResourceGuard extends ChallengeAuthenticator {
    private final Logger logger = Logger.getLogger(WebResourceGuard.class);
    private static final String AUTHENTICATOR_COOKIE_NAME = "authenticator";
    private final IUserSession coUserSession;

    /**
     *
     *
     * @param context
     * @param injector
     * @throws IllegalArgumentException
     */
    public WebResourceGuard(final Context context, final IUserSession coUserSession) throws IllegalArgumentException {
        super(context, ChallengeScheme.CUSTOM, "TG");
        if (coUserSession == null) {
            throw new IllegalArgumentException("User session companion object is required.");
        }
        this.coUserSession = coUserSession;
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

            final Optional<UserSession> session = coUserSession.currentSession(coUserSession.getUser(), authenticator);
            if (!session.isPresent()) {
                logger.warn(format("Authenticator validation failed for a request to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));
                forbid(response);
                return false;
            }

            // create a cookie that will carry an updated authenticator back to the client for further use
            // it is important to note that the time that will be used by further processing of this request is not known
            // and thus is not factored in for session authentication time frame
            // this means that if the processing time exceeds the session expiration time then the next request after this would render invalid, requiring explicit authentication
            // on the one hand this is potentially limiting for untrusted devices, but for trusted devices this should not a problem
            // on the other hand, it might server as an additional security level, limiting computationally intensive requests being send from untrusted devices
            final CookieSetting newCookie = new CookieSetting(1, AUTHENTICATOR_COOKIE_NAME, session.get().getAuthenticator().toString(), "/", null);
            // have to set HttpOnly header, which informs the browser that only the originating server should be able to access the cookie value (hidden for JS access)
            // ensures client side security of authenticators
            newCookie.setAccessRestricted(true);
            // just in case remove any cookies, which is safe as the guard would be the first in the line of request processing logic
            response.getCookieSettings().clear();
            // finally associate the refreshed authenticator with the response
            response.getCookieSettings().add(newCookie);
        } catch (final Exception e) {
            // in case of any
            forbid(response);
            return false;
        }

        return true;
    }

}
