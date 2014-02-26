package ua.com.fielden.platform.security;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;

import ua.com.fielden.platform.cypher.AsymmetricKeyGenerator;
import ua.com.fielden.platform.cypher.Cypher;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.security.user.IAuthenticationModel;

/**
 * Provide user authentication implementation, which should be used by web-enabled clients for authenticating users trying to login to the system.
 *
 * @author TG Team
 *
 */
public class ClientAuthenticationModel implements IAuthenticationModel {

    /** Identifies a URI to a resource at the server end providing an authentication service. */
    private final String authenticationUri;
    private final int keyLenght;
    private final RestClientUtil util;
    private final String applicationWidePrivateKey;

    public ClientAuthenticationModel(final String authenticationUri, final int keyLenght, final RestClientUtil util, final String applicationWidePrivateKey) {
	this.authenticationUri = authenticationUri;
	this.keyLenght = keyLenght;
	this.util = util;
	this.applicationWidePrivateKey = applicationWidePrivateKey;
    }

    @Override
    public Result authenticate(final String username, final String password) {
	try {
	    final AsymmetricKeyGenerator gen = new AsymmetricKeyGenerator(keyLenght);
	    final String privateKey = gen.getStrPrivateKey();
	    final String publicKey = gen.getStrPublicKey();
	    // compose security token
	    final String token = new Cypher().encrypt(username + "::" + password, applicationWidePrivateKey);
	    final Request request = util.newRequest(Method.GET, util.getSystemUri() + authenticationUri + "?public-key=" + publicKey);
	    final Response response = util.send(request, token);
	    if (response.getStatus().isSuccess()) {
		final Result result = util.process(response);
		util.setPrivateKey(privateKey);
		util.setUsername(username);
		return new Result(result.getInstance(), "User successfully authenticated");
	    } else {
		util.resetUser();
		return new Result(null, util.process(response).getMessage(), new IllegalArgumentException("The provided credentials are invalid."));
	    }
	} catch (final Exception e) {
	    util.resetUser();
	    return new Result(null, e);
	}
    }

    /**
     * Validates username for its correspondence to the provided private key.
     *
     * @param username
     * @param privateKey
     * @return
     */
    public Result validateCredentials(final String username, final String privateKey) {
	try {
	    final String secrete = new Cypher().encrypt(username, privateKey);
	    final Request request = util.newRequest(Method.GET, util.getSystemUri() + authenticationUri + "?username=" + username + "&secrete=" + secrete);
	    final Result result = util.process(request).getValue();
	    if (result.isSuccessful()) {
		util.updateLoginInformation(username, privateKey);
	    }
	    return result;
	} catch (final Exception e) {
	    return new Result(null, e);
	}
    }

}
