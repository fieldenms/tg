package ua.com.fielden.platform.web.resources;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.assignAuthenticatingCookie;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.extractAuthenticator;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.utils.ResourceLoader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A web resource handling explicit user logins.
 *
 * @author TG Team
 *
 */
public class LoginResource extends ServerResource {

    private final Logger logger = Logger.getLogger(LoginResource.class);

    private final String domainName;
    private final String path;
    private final IAuthenticationModel authenticationModel;
    private final IUserProvider userProvider;
    private final IUserEx coUserEx;
    private final IUserSession coUserSession;
    private final RestServerUtil restUtil;
    private final IUniversalConstants constants;

    /**
     * Creates {@link LoginResource} and initialises it with centre instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public LoginResource(//
    final String domainName,
            final String path,
            final IUniversalConstants constants,
            final IAuthenticationModel authenticationModel,
            final IUserProvider userProvider,
            final IUserEx coUserEx,
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
        this.coUserEx = coUserEx;
        this.coUserSession = coUserSession;
        this.restUtil = restUtil;

    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            // check if there is a valid authenticator
            // if there is then should respond with redirection to root /.

            final Optional<Authenticator> oAuth = extractAuthenticator(getRequest());
            if (oAuth.isPresent()) {
                final Authenticator auth = oAuth.get();
                userProvider.setUsername(auth.username, coUserEx);
                final Optional<UserSession> session = coUserSession.currentSession(userProvider.getUser(), auth.toString());
                if (session.isPresent()) {
                    // response needs to be provided with an authenticating cookie
                    assignAuthenticatingCookie(constants.now(), session.get().getAuthenticator().get(), domainName, path, getRequest(), getResponse());
                    // response needs to provide redirection instructions
                    getResponse().redirectSeeOther("/");
                    return new EmptyRepresentation();
                }
            }

            // otherwise just load the login page for user to login in explicitly
            return loginPage();
        } catch (final Exception ex) {
            // in case of an exception try try return a login page.
            logger.fatal(ex);
            return loginPage();
        }
    }

    public Representation loginPage() {
        try {
            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/login.html").replaceAll("@title", "Login").getBytes("UTF-8");
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
            final Credentials credo = new ObjectMapper().readValue(form.get(0).getName(), Credentials.class);

            final Result authResult = authenticationModel.authenticate(credo.getUsername(), credo.getPasswd());
            if (!authResult.isSuccessful()) {
                logger.debug(format("Unsuccessful login request (%s)", credo));
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            } else {
                // create a new session for an authenticated user...
                final User user = (User) authResult.getInstance();
                final UserSession session = coUserSession.newSession(user, credo.isTrustedDevice());

                // ...and provide the response with an authenticating cookie
                assignAuthenticatingCookie(constants.now(), session.getAuthenticator().get(), domainName, path, getRequest(), getResponse());
            }
        } catch (final Exception ex) {
            logger.fatal(ex);
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
