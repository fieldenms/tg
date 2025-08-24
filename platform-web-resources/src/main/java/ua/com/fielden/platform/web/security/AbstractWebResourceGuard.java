package ua.com.fielden.platform.web.security;

import static java.lang.String.format;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;
import static ua.com.fielden.platform.web.resources.webui.LoginResource.BINDING_PATH;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CookieSetting;
import org.restlet.data.Method;
import org.restlet.data.Status;

import com.google.inject.Injector;

import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.sse.SseUtils;

/// This is a guard that is based on the new TG authentication scheme, developed as part of the Web UI initiative.
/// It is used to restrict access to sensitive web resources.
///
/// This type is abstract.
/// The only part that is abstract in it is the method for identifying the current user.
/// Applications and unit tests may need to have different ways of determining current users.
///
/// Method [#getUser(String)] needs to be implemented to identify the currently logged-in user, making a request.
///
public abstract class AbstractWebResourceGuard extends org.restlet.security.Authenticator {
    private final Logger logger = LogManager.getLogger(getClass());
    public static final String AUTHENTICATOR_COOKIE_NAME = "authenticator";
    protected final Injector injector;
    private final IUniversalConstants constants;
    private final String domainName;
    private final String path;

    /**
     * Principle constructor.
     *
     * @param context
     * @param injector
     * @throws IllegalArgumentException
     */
    public AbstractWebResourceGuard(final Context context, final String domainName, final String path, final Injector injector) {
        super(context);
        if (injector == null) {
            throw new IllegalArgumentException("Injector is required.");
        }

        this.injector = injector;
        this.constants = injector.getInstance(IUniversalConstants.class);
        this.domainName = domainName;
        this.path = path;

        if (StringUtils.isEmpty(domainName) || StringUtils.isEmpty(path)) {
            throw new IllegalStateException("Both the domain name and the applicatin binding path should be provided.");
        }

    }

    @Override
    public boolean authenticate(final Request request, final Response response) {
        // uncomment the following lines to circumvent the use of session authenticators; this at time useful for development purposes, especially in HTTP mode (as opposed to HTTPS).
        //  if(true) {
        //      final IUserProvider userProvider = injector.getInstance(IUserProvider.class);
        //      userProvider.setUsername(User.system_users.SU.name(), injector.getInstance(IUser.class));
        //      return true;
        //  }
        try {
            logger.debug(format("Starting request authentication to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));

            final Optional<Authenticator> oAuth = extractAuthenticator(request);
            if (!oAuth.isPresent()) {
                logger.warn(format("Authenticator cookie is missing for a request to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));
                redirectGetToLoginOrForbid(request, response);
                return false;
            }

            // authenticator is present
            final Authenticator auth = oAuth.get();

            // let's validate the authenticator
            final IUserSession coUserSession = injector.getInstance(IUserSession.class);
            // for SSE requests session ID should not be regenerated
            // this is due to the fact that for SSE requests no HTTP responses are sent, and so there is nothing to carry an updated cookie with a new authenticator back to the client
            final boolean skipRegeneration = SseUtils.isEventSourceRequest(request);
            final Optional<UserSession> session = coUserSession.currentSession(getUser(auth.username), auth.toString(), enforceUserSessionEvictionWhenDbSessionIsMissing(), skipRegeneration);
            if (!session.isPresent()) {
                logger.warn(format("Authenticator validation failed for a request to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));
                redirectGetToLoginOrForbid(request, response);
                assignAuthenticatorCookieToExpire(response);
                return false;
            }

            // the provided authenticator was valid and a new cookie should be sent back to the client
            assignAuthenticatingCookie(session.get().getUser(), constants.now(), session.get().getAuthenticator().get(), domainName, path, request, response);

        } catch (final Exception ex) {
            // in case of any internal exception forbid the request
            forbid(response);

            // TODO Do we really want to expire a potentially valid authenticator in this case? For example, where there was just an intermittent DB connectivity issue?
            assignAuthenticatorCookieToExpire(response);
            logger.fatal(ex);
            return false;
        }

        return true;
    }

    /**
     * Redirects HTTP GET requests to the login resource. Forbids all other requests.
     *
     * @param request
     * @param response
     */
    protected void redirectGetToLoginOrForbid(final Request request, final Response response) {
        // GET requests can be redirected to the login resource, which takes care of both RSO and SSO workflows.
        // Need to forbid requests from SW containing "?checksum=true", which is specifically used to redirect to /login from the client side.
        if (Method.GET.equals(request.getMethod()) && !request.getResourceRef().toString().contains("?checksum=true")) {
            response.redirectTemporary(BINDING_PATH);
        } else {
            forbid(response);
        }
    }

    /**
     * Extracts the latest user authenticator from the provided request.
     * Returns an empty result in case no authenticating cookies was identified.
     *
     * @param request
     * @return
     */
    public static Optional<Authenticator> extractAuthenticator(final Request request) {
        // convert non-empty authenticating cookies to authenticators and get the most recent one by expiry date...
        return request.getCookies().stream()
                .filter(c -> AUTHENTICATOR_COOKIE_NAME.equals(c.getName()) && !StringUtils.isEmpty(c.getValue()))
                .map(c -> fromString(c.getValue()))
                .max((auth1, auth2) -> Long.compare(auth1.version, auth2.version));
    }

    /**
     * A convenient method that creates an authenticating cookie based on the provided authenticator and associates it with the specified HTTP response.
     *
     * @param authenticator
     * @param response
     */
    public static void assignAuthenticatingCookie(final User user, final DateTime now, final Authenticator authenticator, final String domainName, final String path, final Request request, final Response response) {
        // create a cookie that will carry an updated authenticator back to the client for further use
        // it is important to note that the time that will be used by further processing of this request is not known
        // and thus is not factored in for session authentication time frame
        // this means that if the processing time exceeds the session expiration time then the next request after this would render invalid, requiring explicit authentication
        // on the one hand this is potentially limiting for untrusted devices, but for trusted devices this should not a problem
        // on the other hand, it might server as an additional security level, limiting computationally intensive requests being send from untrusted devices

        // calculate maximum cookie age in seconds
        final long millis = authenticator.getExpiryTime().orElseThrow(() -> new SecurityException("Authenticator is missing the expiration date.")).getMillis();
        final int maxAge = (int) (millis - now.getMillis()) / 1000;

        if (maxAge <= 0) {
            throw new IllegalStateException("What the hack is goinig on! maxAge for cookier is not > zero.");
        }

        final CookieSetting newCookie = new CookieSetting(
                0 /*version*/,
                AUTHENTICATOR_COOKIE_NAME /*name*/,
                authenticator.toString() /*value*/,
                path /*path*/,
                domainName /*domain*/,
                null /*comment*/,
                maxAge /* number of seconds before cookie expires */,
                true /*secure*/, // if secure is set to true then this cookie would only be included into the request if it is done over HTTPS!
                true /*accessRestricted*/);
        // finally associate the refreshed authenticator with the response
        response.getCookieSettings().clear();
        response.getCookieSettings().add(newCookie);
        
        // let's record the current username as the Restlet security User that forms part of the HTTP request information.
        // This information can then be easily reused whenever the current username is required, but there is no access to the application user provider (e.g. in HTTP request filters).
        final org.restlet.security.User restletUser = new org.restlet.security.User();
        restletUser.setIdentifier(user.getId().toString());
        request.getClientInfo().setUser(restletUser);
    }

    /**
     * Should be implemented in accordance with requirements for obtaining the current user by name.
     *
     * @return
     */
    protected abstract User getUser(final String username);

    /**
     * Assigns "expired" authentication cookie to inform the browser that this cookie is no longer valid.
     * @param response
     */
    private void assignAuthenticatorCookieToExpire(final Response response) {
        final CookieSetting cookie = mkAuthenticationCookieToExpire(domainName, path);
        response.getCookieSettings().clear();
        response.getCookieSettings().add(cookie);
    }

    /**
     * A convenient factory method for creating expiring authentication cookies.
     * @param domainName
     * @param path
     * @return
     */
    public static CookieSetting mkAuthenticationCookieToExpire(final String domainName, final String path) {
        return new CookieSetting(
                0 /*version*/,
                AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME /*name*/,
                "" /* empty value*/,
                path,
                domainName,
                null /*comment*/,
                0 /* number of seconds before cookie expires, 0 -- expires immediately */,
                true /*secure*/, // if secure is set to true then this cookie would only be included into the request if it is done over HTTPS!
                true /*accessRestricted*/);
    }

    /**
     * Indicates to the authenticator validation logic whether all user sessions needs to be evicted in case if a valid authenticator was provided, but a corresponding DB record was missing.
     * @return
     */
    protected boolean enforceUserSessionEvictionWhenDbSessionIsMissing() {
        return false;
    }

    /**
     * Sets the status for {@code response} as forbidden.
     *
     * @param response
     */
    private void forbid(final Response response) {
        response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
    }

}