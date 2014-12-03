package ua.com.fielden.platform.web.factories;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.security.ChallengeAuthenticator;

import ua.com.fielden.platform.cypher.Cypher;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * This is a guard that implements TG authentication mechanism. It should be used at the application server side to guard access to web resources.
 * <p>
 * By default this implementation passes through all request to user authentication resource without any check. This is required to allow unauthenticated user to login.
 * 
 * @author TG Team
 * 
 */
public class ResourceGuard extends ChallengeAuthenticator {

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
    public ResourceGuard(final Context context, final String realm, final RestServerUtil util, final Injector injector) throws IllegalArgumentException {
        super(context, ChallengeScheme.CUSTOM, realm);
        this.util = util;
        this.injector = injector;
        setRechallenging(false);
    }

    @Override
    public boolean authenticate(final Request request, final Response response) {
        try {
            final String token = util.getHeaderValue(request, HttpHeaders.AUTHENTICATION);
            if (StringUtils.isEmpty(token)) {
                forbid(response);
                return false;
            }
            // separate username from the encoded part of the token
            final String[] parts = token.split("::");
            if (parts.length != 2) {
                forbid(response);
                return false;
            }
            // use the username to lookup a corresponding public key to decode security token
            final String username = parts[0];
            final IUserController controller = getController();
            final User user = controller.findByKey(username);
            if (user == null) {
                forbid(response);
                return false;
            }
            final String publicKey = user.getPublicKey();
            if (StringUtils.isEmpty(publicKey)) {
                forbid(response);
                return false;
            }

            // validate the decoded URI by matching it with the request URI
            final String tokenUri = new Cypher().decrypt(parts[1], publicKey);
            if (!request.getResourceRef().toString().equals(tokenUri)) {
                forbid(response);
                return false;
            }
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
