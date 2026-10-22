package ua.com.fielden.platform.web.resources.webui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.LoginAttemptTracker.Outcome;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.resources.webui.LoginAttemptTracker.BLOCK_TIME_SECONDS;
import static ua.com.fielden.platform.web.resources.webui.LoginAttemptTracker.LOCKOUT_THRESHOLD;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.assignAuthenticatingCookie;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.extractAuthenticator;

/// A web resource handling explicit user logins.
///
/// Login attempts are tracked by [LoginAttemptTracker], which blocks repeated failed attempts and determines when an account is to be locked out (see [IUser#lockoutUser(String)]).
///
public class LoginResource extends AbstractWebResource {
    
    public static final String BINDING_PATH = "/login";
    public static final String SSO_BINDING_PATH = "/sso";
    private static final Logger LOGGER = LogManager.getLogger(LoginResource.class);

    private final String domainName;
    private final String path;
    private final IAuthenticationModel authenticationModel;
    private final IUserProvider up;
    private final IUser coUser;
    private final IUserSession coUserSession;
    private final RestServerUtil restUtil;
    private final IUniversalConstants constants;
    private final LoginAttemptTracker attemptTracker;
    
    private final byte[] loginPage;
    private final byte[] loginPageForMixedMode;

    public LoginResource(
            final byte[] loginPage,
            final byte[] loginPageForMixedMode,
            final String domainName,
            final String path,
            final IUniversalConstants constants,
            final IAuthenticationModel authenticationModel,
            final IUserProvider userProvider,
            final IUser coUser,
            final IUserSession coUserSession,
            final RestServerUtil restUtil,
            final LoginAttemptTracker attemptTracker,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Context context,
            final Request request,
            final Response response)
    {
        super(context, request, response, deviceProvider, dates);
        this.domainName = domainName;
        this.path = path;
        this.constants = constants;
        this.authenticationModel = authenticationModel;
        this.up = userProvider;
        this.coUser = coUser;
        this.coUserSession = coUserSession;
        this.restUtil = restUtil;
        this.attemptTracker = attemptTracker;
        this.loginPage = loginPage;
        this.loginPageForMixedMode = loginPageForMixedMode;
    }

    @Get
    public Representation login() {
        try {
            // check if there is a valid authenticator
            // if there is then should respond with redirection to root /.

            final Optional<Authenticator> oAuth = extractAuthenticator(getRequest());
            if (oAuth.isPresent()) {
                final Authenticator auth = oAuth.get();
                up.setUsername(auth.username, coUser);
                final Optional<UserSession> session = coUserSession.currentSession(up.getUser(), auth.toString(), false);
                // if authenticated session could be obtained, then we simply redirect request to "/"
                if (session.isPresent()) {
                    // response needs to be provided with an authenticating cookie
                    assignAuthenticatingCookie(session.get().getUser(), constants.serverNow(), session.get().getAuthenticator().get(), domainName, path, getRequest(), getResponse());
                    // response needs to provide redirection instructions
                    getResponse().redirectSeeOther("/");
                    return new EmptyRepresentation();
                }
            }

            // Otherwise just return the login page for user to login explicitly.
            // The returned page should take into account support for SSO and the mixed RSO/SSO mode
            // Query parameter "auth" can be used to enforce the RSO mode even when SSO is preferred
            // Accessing the login resource with /login?auth=RSO should return a login page with a prompt without automatic re-direction to SSO
            // Such request is valid in both RSO and SSO authentication modes, but in the SSO mode it is ignored
            final String requestedAuth = getQueryValue("auth");
            final ByteArrayInputStream loginPageStream = AuthMode.RSO.name().equalsIgnoreCase(requestedAuth) ? new ByteArrayInputStream(loginPageForMixedMode) : new ByteArrayInputStream(loginPage);
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(loginPageStream, MediaType.TEXT_HTML));
        } catch (final Exception ex) {
            // in case of an exception log the error and return the login page for the user to try again
            LOGGER.fatal(ex.getMessage(), ex);
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(loginPageForMixedMode), MediaType.TEXT_HTML));
        } finally {
            // This handler may set the current user (above) to validate an existing session.
            // It is attached at the component level and is not covered by the application-level cleanup filter, so it must clear the user itself —
            // otherwise a pooled worker thread would retain it after the request.
            up.clearUser();
        }
    }

    @Post
    public void tryLogin(final Representation entity) {
        final long nanoStart = System.nanoTime();
        try {
            final Form form = new Form(entity);
            final Credentials credo = new Credentials();
            credo.setUsername(form.getValues("username"));
            credo.setPasswd(form.getValues("passwd"));
            credo.setTrustedDevice(Boolean.parseBoolean(form.getValues("trustedDevice")));

            // Attempts with all usernames are tracked and blocked in the same way, but only accounts that can sign in with a password are locked out (see IUser.lockoutUser).
            final boolean passwordAccount = coUser.findUserForPasswordLogin(credo.username).isPresent();
            final var authResult = new AtomicReference<Result>();
            final Outcome outcome = attemptTracker.attempt(credo.username, passwordAccount, () -> {
                authResult.set(authenticationModel.authenticate(credo.username, credo.passwd));
                return authResult.get().isSuccessful();
            });
            switch (outcome) {
                case AUTHENTICATED -> startSession((User) authResult.get().getInstance(), credo.trustedDevice);
                case DENIED -> {
                    LOGGER.warn(() -> "Unsuccessful login request for user [%s].".formatted(credo.username));
                    respondAccessDenied();
                }
                case BLOCKED -> {
                    LOGGER.warn(() -> "Repeated login attempt for user [%s], blocked for [%s] seconds.".formatted(credo.username, BLOCK_TIME_SECONDS));
                    respondUserIsBlocked(BLOCK_TIME_SECONDS);
                }
                case LOCKOUT -> {
                    LOGGER.warn(() -> "Lockout after [%s] unsuccessful login attempts for user [%s].".formatted(LOCKOUT_THRESHOLD, credo.username));
                    coUser.lockoutUser(credo.username);
                    respondAccessDenied();
                }
            }
        } catch (final Exception ex) {
            LOGGER.fatal(ex.getMessage(), ex);
            getResponse().setEntity(restUtil.errorJsonRepresentation(ex.getMessage()));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        } finally {
            LOGGER.debug(() -> "LOGIN ATTEMPT RESPONSE TIME: %s%n".formatted(TimeUnit.MILLISECONDS.convert(System.nanoTime() - nanoStart, TimeUnit.NANOSECONDS)));
        }
    }

    /// Starts a new session for an authenticated `user`, responding with an authenticating cookie.
    ///
    private void startSession(final User user, final boolean trustedDevice) {
        // let's use this opportunity to clear expired sessions for the user
        coUserSession.clearExpired(user);
        final UserSession session = coUserSession.newSession(user, trustedDevice, null);
        assignAuthenticatingCookie(user, constants.serverNow(), session.getAuthenticator().get(), domainName, path, getRequest(), getResponse());
        getResponse().setEntity(new JsonRepresentation("{\"msg\": \"Credentials are valid.\"}"));
    }

    private void respondUserIsBlocked(final long delaySeconds) {
        getResponse().setEntity(new JsonRepresentation(format("{\"msg\": \"Blocked for [%s] seconds.\"}", delaySeconds)));
        getResponse().setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
    }

    private void respondAccessDenied() {
        getResponse().setEntity(new JsonRepresentation("{\"msg\": \"Invalid credentials.\"}"));
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    /// This is just a convenient wrapper for JSON login package.
    ///
    private static class Credentials {
        private String username;
        private String passwd;
        private boolean trustedDevice;

        public void setUsername(final String username) {
            this.username = username;
        }

        public void setPasswd(final String passwd) {
            this.passwd = passwd;
        }

        public void setTrustedDevice(final boolean trustedDevice) {
            this.trustedDevice = trustedDevice;
        }

        @Override
        public String toString() {
            try {
                return new ObjectMapper().writer().writeValueAsString(this);
            } catch (final JsonProcessingException ex) {
                LOGGER.error(ex.getMessage(), ex);
                return "could not serialise to JSON";
            }
        }
    }

}
