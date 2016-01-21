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
import org.restlet.data.Status;
import org.restlet.security.ChallengeAuthenticator;

import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IUniversalConstants;

import com.google.common.collect.Iterables;
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
    public AbstractWebResourceGuard(final Context context, final String domainName, final String path, final Injector injector) throws IllegalArgumentException {
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
            final Optional<UserSession> session = coUserSession.currentSession(getUser(auth.username), auth.toString());
            if (!session.isPresent()) {
                logger.warn(format("Authenticator validation failed for a request to a resource at URI %s (%s, %s, %s)", request.getResourceRef(), request.getClientInfo().getAddress(), request.getClientInfo().getAgentName(), request.getClientInfo().getAgentVersion()));
                // TODO this is an interesting approach to prevent any further processing of the request, this event prevents receiving it completely
                // useful to prevent unauthorised file uploads
                // However, the client side would not be able to receive a response.
                //request.abort();
                forbid(response);
                return false;
            }

            // at this stage it is safe to record the fact of successful resource access for later analysis of resources being accessed and
            // to count concurrent users ... it would be wise to use some key/value database for this instead of the underlying RDBMS
            // TODO #236 implement logging of accessed by users resources... potentially a custom log4j appender could be created for this purpose


            // the provided authenticator was valid and a new cookie should be send back to the client
            assignAuthenticatingCookie(constants.now(), session.get().getAuthenticator().get(), domainName, path, request, response);

        } catch (final Exception ex) {
            // in case of any internal exception forbid the request
            forbid(response);
            ex.printStackTrace();
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

        // if there are no authenticating cookies then return an empty result
        if (cookies.isEmpty()) {
            return Optional.empty();
        }

        // convert authenticating cookies to authenticators and sort them by expiry date oldest first...
        final List<Authenticator> authenticators = cookies.stream()
                .map(c -> fromString(c.getValue()))
                .sorted((auth1, auth2) -> auth1.getExpiryTime().compareTo(auth2.getExpiryTime()))
                .collect(Collectors.toList());

        // ...and get the most recent authenticator...
        final Authenticator auth = Iterables.getLast(authenticators);
        return Optional.of(auth);
    }

    /**
     * A convenient method that creates an authenticating cookie based on the provided authenticator and associates it with the specified HTTP response.
     *
     * @param authenticator
     * @param response
     */
    public static void assignAuthenticatingCookie(final DateTime now, final Authenticator authenticator, final String domainName, final String path, final Request request, final Response response) {
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
    }

    /**
     * Should be implemented in accordance with requirements for obtaining the current user by name.
     *
     * @return
     */
    protected abstract User getUser(final String username);
}
