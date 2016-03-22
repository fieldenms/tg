package ua.com.fielden.platform.web.resources;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Resource;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.cypher.Cypher;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.User;

/**
 * A resource responsible for user authentication. The result of its successful execution is a full user information returned to the client application.
 * 
 * @author TG Team
 */
public class UserAuthResource extends ServerResource {
    private final IUserEx coUserEx;
    private final RestServerUtil restUtil;
    private final String token;
    private final String publicKey;
    private final String username;
    private final String secrete;

    public static final String CREDENTIALS_ARE_VALID = "Credentials are valid.";
    public static final String PASSWORD_RESET = "The password has been reset. Log in is required.";
    public static final String INVALID_CREDENTIALS = "Provided credentials are invalid. Log in required.";
    public static final String ANOTHER_USER_LOGGED_IN = "<html>Another user has logged in with the same credentials.<br/>Please change the password once logged in if fraud is suspected.</html>";

    /**
     * The main resource constructor accepting a DAO instance and an entity factory in addition to the standard {@link Resource} parameters.
     * <p>
     * DAO is required for DB interoperability, whereas entity factory is required for enhancement of entities provided in request envelopes.
     * 
     * @param controller
     * @param factory
     * @param context
     * @param request
     * @param response
     * @throws Exception
     */
    public UserAuthResource(final IUserEx controller, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        init(context, request, response);
        setNegotiated(false);
        getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
        this.coUserEx = controller;
        this.restUtil = restUtil;
        token = restUtil.getHeaderValue(request, HttpHeaders.AUTHENTICATION);
        publicKey = request.getResourceRef().getQueryAsForm().getFirstValue("public-key");
        username = request.getResourceRef().getQueryAsForm().getFirstValue("username");
        secrete = request.getResourceRef().getQueryAsForm().getFirstValue("secrete");
    }

    ///////////////////////////////////////////////////////////////////
    ////////////////////// request handlers ///////////////////////////
    ///////////////////////////////////////////////////////////////////
    /**
     * Handles GET requests for user authentication. The GET here means client application is trying to access user specific information, where the details of the request are
     * provided as a security token in an encrypted form.
     */
    @Get
    @Override
    public Representation get() {
        // process GET request
        if (!StringUtils.isEmpty(publicKey)) { // login request
            return login();
        } else if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(secrete)) { // validate credentials request
            return validateCredentials();
        } else { // unrecognised request
            return restUtil.errorRepresentation("The request has not been recognised as valid.");
        }
    }

    /** Performs normal log in. */
    private Representation login() {
        try {
            // Decipher security token, which should have the structure of <username>::<password>.
            final String[] values = new Cypher().decrypt(token, restUtil.getAppWidePublicKey()).split("::");
            final String username = values[0];
            // Encrypt the password with application wide private key in order for it to be matched against an encrypted password retrieved using controller
            final String password = new Cypher().encrypt(values[1], restUtil.getAppWidePrivateKey());
            // Find user data by the provided username.
            final User user;
            try {
                validateAndUpdate(username);
                user = findUser(username);
            } catch (final Exception e) {
                getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return restUtil.errorRepresentation(e.getMessage());
            }
            // If user was found and password matches then it can be updated and returned to the client
            if (user != null && password.equals(user.getPassword())) {
                user.setPublicKey(publicKey);
                coUserEx.save(user);
                return restUtil.singleRepresentation(coUserEx.findUserByIdWithRoles(user.getId()));
            } else { // otherwise the provided authentication information is invalid
                getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return restUtil.errorRepresentation("User name or password is incorrect. Please try again.");
            }
        } catch (final Exception ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return restUtil.errorRepresentation("Could not process user authentication request:\n" + ex.getMessage());
        }
    }

    /** Validates provided in the request credentials. */
    private Representation validateCredentials() {
        try {
            final User user;
            try {
                validateAndUpdate(username);
                user = findUser(username);
            } catch (final Exception e) {
                getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return restUtil.errorRepresentation(e.getMessage());
            }
            if (user != null && !StringUtils.isEmpty(user.getPublicKey())) {
                // user and public key are present... try to decrypt the provided secrete with the public key...
                try {
                    final String descrypteSecrete = new Cypher().decrypt(secrete, user.getPublicKey());
                    // if the decrypted value matches username then credentials are considered to be correct
                    if (username.equals(descrypteSecrete)) {
                        return restUtil.resultRepresentation(new Result(CREDENTIALS_ARE_VALID));
                    }
                } catch (final Exception e) {
                    // in case of exception, which could happen during decryption, try to match the current user public key with the application-wide public key
                    // if it matches then user password was reset
                    if (user.getPublicKey().equals(restUtil.getAppWidePublicKey())) {
                        return restUtil.errorRepresentation(PASSWORD_RESET);
                    }
                }
            } else { // either user was not found or its private key is empty -- in both cases credentials are considered to be invalid
                getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                return restUtil.errorRepresentation(INVALID_CREDENTIALS);
            }

            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return restUtil.errorRepresentation(ANOTHER_USER_LOGGED_IN);
        } catch (final Exception ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return restUtil.errorRepresentation("Could not process validation of user credentials:\n" + ex.getMessage());
        }
    }

    /**
     * Validates and updates/creates user for appropriate <code>username</code>. This could be done from outside resources like LDAP servers etc.
     * 
     * @param username
     * @return
     */
    public void validateAndUpdate(final String username) throws Exception {
    }

    protected User findUser(final String username) {
        return coUserEx.findByKey(username);
    }

    protected RestServerUtil getRestUtil() {
        return restUtil;
    }
}
