package ua.com.fielden.platform.web;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;

import ua.com.fielden.platform.cypher.Cypher;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.security.provider.IUserController2;
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
public class ResourceGuard extends Guard {

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
	setRechallengeEnabled(false);
    }

    @Override
    public int authenticate(final Request request) {
	try {
	    final String token = util.getHeaderValue(request, HttpHeaders.AUTHENTICATION);
	    if (StringUtils.isEmpty(token)) {
		return AUTHENTICATION_INVALID;
	    }
	    // separate username from the encoded part of the token
	    final String[] parts = token.split("::");
	    if (parts.length != 2) {
		return AUTHENTICATION_INVALID;
	    }
	    // use the username to lookup a corresponding public key to decode security token
	    final String username = parts[0];
	    final IUserController2 controller = getController();
	    final User user = controller.findByKey(username);
	    if (user == null) {
		return AUTHENTICATION_INVALID;
	    }
	    final String publicKey = user.getPublicKey();
	    if (StringUtils.isEmpty(publicKey)) {
		return AUTHENTICATION_INVALID;
	    }

	    // validate the decoded URI by matching it with the request URI
	    final String tokenUri = new Cypher().decrypt(parts[1], publicKey);
	    if (!request.getResourceRef().toString().equals(tokenUri)) {
		return AUTHENTICATION_INVALID;
	    }
	} catch (final Exception e) {
	    return AUTHENTICATION_INVALID;
	}

	return AUTHENTICATION_VALID;
    }

    protected IUserController2 getController() {
	return injector.getInstance(IUserController2.class);
    }
}
