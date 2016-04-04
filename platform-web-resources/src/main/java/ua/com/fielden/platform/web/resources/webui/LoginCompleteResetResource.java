package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * A web resource handling user login reset requests.
 *
 * @author TG Team
 *
 */
public class LoginCompleteResetResource extends ServerResource {

    private final Logger logger = Logger.getLogger(LoginCompleteResetResource.class);

    private final String domainName;
    private final String path;
    private final IAuthenticationModel authenticationModel;
    private final IUserProvider userProvider;
    private final IUser coUser;
    private final IUserSession coUserSession;
    private final RestServerUtil restUtil;
    private final IUniversalConstants constants;

    private final String uuid;

    /**
     * Creates {@link LoginCompleteResetResource}.
     */
    public LoginCompleteResetResource(//
    final String domainName,
            final String path,
            final IUniversalConstants constants,
            final IAuthenticationModel authenticationModel,
            final IUserProvider userProvider,
            final IUser coUser,
            final IUserSession coUserSession,//
            final RestServerUtil restUtil,//
            final Context context, //
            final Request request, //
            final Response response) {
        init(context, request, response);
        this.domainName = domainName;
        this.path = path;
        this.constants = constants;
        this.authenticationModel = authenticationModel;
        this.userProvider = userProvider;
        this.coUser = coUser;
        this.coUserSession = coUserSession;
        this.restUtil = restUtil;
        this.uuid = (String) request.getAttributes().get("uuid");
    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            // if the UUID is invalid then redirect the user to the password reset resource
            if (StringUtils.isEmpty(this.uuid) || !coUser.isPasswordResetUuidValid(this.uuid)) {
                return pageToReportResetSessionExpiration(logger);
            } else {
                final Optional<User> user = coUser.findUserByResetUuid(uuid);
                if (user.isPresent()) {
                    return pageToProvideNewPassword(this.uuid);
                } else {
                    final SecurityException securityException = new SecurityException(format("Could not find a user matching requested UUID [%s].", this.uuid));
                    throw securityException;
                }
            }
        } catch (final Exception ex) {
            logger.fatal(ex);
            
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new SecurityException("Could not reset the password.", ex);
            }
        }
    }

    private static Representation pageToReportResetSessionExpiration(final Logger logger) {
        try {
            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/login-expired-reset.html").getBytes("UTF-8");
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
        } catch (final Exception ex) {
            logger.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }

    private Representation pageToProvideNewPassword(final String uuid) {
        try {
            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/login-complete-reset.html")
                    .replace("@title", "Login Complete Reset")
                    .replace("@uuid", uuid)
                    .getBytes("UTF-8");
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
        } catch (final Exception ex) {
            logger.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Post
    public void resetLogin(final Representation entity) throws ResourceException {
        try {
            final Form form = new Form(entity);
            final String usernameOrEmail = form.getValues("username"); 
            System.out.println(usernameOrEmail);
            getResponse().setEntity(new JsonRepresentation("{\"msg\": \"Not yet supported.\"}"));
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            
//            final Result authResult = authenticationModel.authenticate(credo.getUsername(), credo.getPasswd());
//            if (!authResult.isSuccessful()) {
//                logger.warn(format("Unsuccessful login request (%s)", credo));
//                getResponse().setEntity(new JsonRepresentation("{\"msg\": \"Invalid credentials.\"}"));
//                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
//            } else {
//                // create a new session for an authenticated user...
//                final User user = (User) authResult.getInstance();
//                final UserSession session = coUserSession.newSession(user, credo.isTrustedDevice());
//
//                // ...and provide the response with an authenticating cookie
//                assignAuthenticatingCookie(constants.now(), session.getAuthenticator().get(), domainName, path, getRequest(), getResponse());
//                getResponse().setEntity(new JsonRepresentation("{\"msg\": \"Credentials are valid.\"}"));
//            }
        } catch (final Exception ex) {
            logger.fatal(ex);
            getResponse().setEntity(restUtil.errorJSONRepresentation(ex.getMessage()));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

}
