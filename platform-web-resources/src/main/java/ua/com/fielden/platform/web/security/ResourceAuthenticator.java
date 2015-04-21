package ua.com.fielden.platform.web.security;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.security.ChallengeAuthenticator;

import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is a guard that implements TG authentication mechanism based on the notion of authenticator.
 * It should be used at the application server side to guard access to web resources.
 *
 * @author TG Team
 *
 */
public class ResourceAuthenticator extends ChallengeAuthenticator {

    private static final String AUTHENTICATOR_COOKIE_NAME = "authenticator";
    private final RestServerUtil util;
    private final Injector injector;

    /**
     * Principal constructor.
     *
     * @param context
     * @param realm
     * @param authenticationUri
     *            -- required to match (and pass) user authentication requests.
     * @throws IllegalArgumentException
     */
    public ResourceAuthenticator(final Context context, final String realm, final RestServerUtil util, final Injector injector) throws IllegalArgumentException {
        super(context, ChallengeScheme.CUSTOM, realm);
        this.util = util;
        this.injector = injector;
        setRechallenging(false);
    }

    @Override
    public boolean authenticate(final Request request, final Response response) {
        try {
            final Cookie cookie = request.getCookies().getFirst(AUTHENTICATOR_COOKIE_NAME);
            if (cookie == null) {
                forbid(response);
                return false;
            }

            final String authenticator = cookie.getValue();
            if (StringUtils.isEmpty(authenticator)) {
                forbid(response);
                return false;
            }

            // separate username from the encoded part of the token
            final String[] parts = authenticator.split("::");
            if (parts.length != 4) {
                forbid(response);
                return false;
            }
            // use the username to lookup a corresponding public key to decode security token
            final String username = parts[0];
            final String seriesId = parts[1];
            final String expiryTimeStr = parts[2];
            // should be used to check authenticator expiration
            final DateTime expiryTime = new DateTime(Long.parseLong(expiryTimeStr));
            final String hashCode = parts[3];

            final String token = username + seriesId + expiryTimeStr;
            final String computedHash = SessionIdentifierGenerator.calculateRFC2104HMAC(token, "this is my cool key for testing purposes");

            if (!computedHash.equals(hashCode)) {
                forbid(response);
                return false;
            }

            // if this point is reached then the current request is considered authentic
            // need to regenerate the provided authenticator and provided it back as a cookie
            // TODO Implement authenticator construction
            final CookieSetting newCookie = new CookieSetting(1, AUTHENTICATOR_COOKIE_NAME, authenticator, "/", null);
            newCookie.setAccessRestricted(true); // this sets HttpOnly property, which informs the browser that only the server should access its value (hidden from JS)
            response.getCookieSettings().clear();
            response.getCookieSettings().add(newCookie);

        } catch (final Exception e) {
            forbid(response);
            return false;
        }

        return true;
    }

    protected IUserController getController() {
        return injector.getInstance(IUserController.class);
    }
}
