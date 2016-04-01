package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.assignAuthenticatingCookie;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.extractAuthenticator;

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
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
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
public class LoginResetResource extends ServerResource {

    private final Logger logger = Logger.getLogger(LoginResetResource.class);

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
     * Creates {@link LoginResetResource}.
     */
    public LoginResetResource(//
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
        System.out.println("UUID " + uuid + "  " + System.identityHashCode(this));
    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            // check if there is a valid authenticator
            // if there is then should respond with redirection to root /.

            if (StringUtils.isEmpty(this.uuid)) {
                return loginResetRequestPage();
            } else {
                // TODO validate uuid and if valid then proceed to to the submission page 
                return loginResetSubmitPage(this.uuid);
            }
        } catch (final Exception ex) {
            // in case of an exception try try return a login page.
            logger.fatal(ex);
            return loginResetRequestPage(); // TODO what else could we return?
        }
    }

    private Representation loginResetRequestPage() {
        try {
            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/login-reset-request.html")
                    .replace("@title", "Login Reset Request").getBytes("UTF-8");
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
        } catch (final Exception ex) {
            logger.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }
    
    private Representation loginResetSubmitPage(final String uuid) {
        try {
            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/login-reset-submit.html")
                    .replace("@title", "Login Reset Submit")
                    .replace("@uuid", uuid)
                    .getBytes("UTF-8");
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
        } catch (final Exception ex) {
            logger.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Post
    public void login(final Representation entity) throws ResourceException {
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

    /**
     * This is just a convenient wrapper for JSON login package.
     *
     */
    static class Credentials {
        private String username;
        private String passwd;
        private boolean trustedDevice;

        public String getUsername() {
            return username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }

        public String getPasswd() {
            return passwd;
        }

        public void setPasswd(final String passwd) {
            this.passwd = passwd;
        }

        public boolean isTrustedDevice() {
            return trustedDevice;
        }

        public void setTrustedDevice(final boolean trustedDevice) {
            this.trustedDevice = trustedDevice;
        }

        @Override
        public String toString() {
            try {
                return new ObjectMapper().writer().writeValueAsString(this);
            } catch (final JsonProcessingException e) {
                return "could not serialise to JSON";
            }
        }
    }

}
