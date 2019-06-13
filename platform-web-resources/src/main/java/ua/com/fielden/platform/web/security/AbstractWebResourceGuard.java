package ua.com.fielden.platform.web.security;

import static java.lang.String.format;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.google.common.collect.Iterables;
import com.google.inject.Injector;

import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IUniversalConstants;

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
    private final Logger logger = Logger.getLogger(getClass());
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
        super(context, ChallengeScheme.CUSTOM, "TG");
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

        setRechallenging(false);
    }

    @Override
    public boolean authenticate(final Request request, final Response response) {
        // uncomment following lines to be able to use http (StartOverHttp) server instead of https (Start) server for development purposes:
//        if(true) {
//            final IUserProvider userProvider = injector.getInstance(IUserProvider.class);
//            userProvider.setUsername(User.system_users.SU.name(), injector.getInstance(IUser.class));
//            return true;
//        }
        try {
            logger.debug(format("Starting request authentication to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));

            final Optional<Authenticator> oAuth = extractAuthenticator(request);
            if (!oAuth.isPresent()) {
                logger.warn(format("Authenticator cookie is missing for a request to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));
                forbid(response);
                return false;
            }

            // authenticator is present
            final Authenticator auth = oAuth.get();

            // let's validate the authenticator
            final IUserSession coUserSession = injector.getInstance(IUserSession.class);
            // TODO For SSE requests authenticators should not be regenerated
            //      This is due to the fact that for SSE requests no HTTP responses are sent, and so there is nothing to carry an updated cookie with a new authenticator back to the client
            final boolean skipRegeneration = false;
            final Optional<UserSession> session = coUserSession.currentSession(getUser(auth.username), auth.toString(), skipRegeneration);
            if (!session.isPresent()) {
                logger.warn(format("Authenticator validation failed for a request to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));
                // TODO this is an interesting approach to prevent any further processing of the request, this event prevents receiving it completely
                // useful to prevent unauthorised file uploads
                // However, the client side would not be able to receive a response.
                //request.abort();
                forbid(response);
                assignAuthenticatorCookieToExpire(response);
                return false;
            }

            // the provided authenticator was valid and a new cookie should be send back to the client
            assignAuthenticatingCookie(session.get().getUser(), constants.now(), session.get().getAuthenticator().get(), domainName, path, request, response);

        } catch (final Exception ex) {
            // in case of any internal exception forbid the request
            forbid(response);
            assignAuthenticatorCookieToExpire(response);
            logger.fatal(ex);
            return false;
        }

        return true;
    }

    /**
     * Extracts the latest user authenticator from the provided request.
     * Returns an empty result in case no authenticating cookies was identified.
     *
     * @param request
     * @return
     */
    public static Optional<Authenticator> extractAuthenticator(final Request request) {
        // first collect non-empty authenticating cookies
        final List<Cookie> cookies = request.getCookies().stream()
                .filter(c -> AUTHENTICATOR_COOKIE_NAME.equals(c.getName()) && !StringUtils.isEmpty(c.getValue()))
                .collect(Collectors.toList());

        // convert authenticating cookies to authenticators and get the most recent one by expiry date...
        return cookies.stream()
                .map(c -> fromString(c.getValue()))
                .max((auth1, auth2) -> Long.compare(auth1.expiryTime, auth2.expiryTime));
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
        final int maxAge = (int) (authenticator.expiryTime - now.getMillis()) / 1000;

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

}
