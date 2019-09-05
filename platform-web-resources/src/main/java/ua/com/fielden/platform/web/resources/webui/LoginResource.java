package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.assignAuthenticatingCookie;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.extractAuthenticator;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
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
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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
 * A web resource handling explicit user logins.
 *
 * @author TG Team
 *
 */
public class LoginResource extends ServerResource {
    
    public static final String BINDING_PATH = "/login";
    public static final int BLOCK_TIME_SECONDS = 15;
    public static final int LOCKOUT_THRESHOLD = 6;
    private static final Cache<String, LoginAttempts> LOGIN_ATTEMPTS = CacheBuilder.newBuilder().initialCapacity(100).maximumSize(1000).concurrencyLevel(50).build();
    private static final Logger LOGGER = Logger.getLogger(LoginResource.class);

    private final String domainName;
    private final String path;
    private final IAuthenticationModel authenticationModel;
    private final IUserProvider up;
    private final IUser coUser;
    private final IUserSession coUserSession;
    private final RestServerUtil restUtil;
    private final IUniversalConstants constants;

    /**
     * Creates {@link LoginResource}.
     */
    public LoginResource(
            final String domainName,
            final String path,
            final IUniversalConstants constants,
            final IAuthenticationModel authenticationModel,
            final IUserProvider userProvider,
            final IUser coUser,
            final IUserSession coUserSession,
            final RestServerUtil restUtil,
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);
        this.domainName = domainName;
        this.path = path;
        this.constants = constants;
        this.authenticationModel = authenticationModel;
        this.up = userProvider;
        this.coUser = coUser;
        this.coUserSession = coUserSession;
        this.restUtil = restUtil;
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
                if (session.isPresent()) {
                    // response needs to be provided with an authenticating cookie
                    assignAuthenticatingCookie(session.get().getUser(), constants.now(), session.get().getAuthenticator().get(), domainName, path, getRequest(), getResponse());
                    // response needs to provide redirection instructions
                    getResponse().redirectSeeOther("/");
                    return new EmptyRepresentation();
                }
            }

            // otherwise just load the login page for user to login in explicitly
            return loginPage();
        } catch (final Exception ex) {
            // in case of an exception try try return a login page.
            LOGGER.fatal(ex);
            return loginPage();
        }
    }

    public Representation loginPage() {
        try {
            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/login.html").replaceAll("@title", "Login").getBytes("UTF-8");
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body), MediaType.TEXT_HTML));
        } catch (final Exception ex) {
            LOGGER.fatal(ex);
            throw new IllegalStateException(ex);
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

            final DateTime now = constants.now();
            final LoginAttempts la = LOGIN_ATTEMPTS.get(credo.username, () -> new LoginAttempts(0, empty(), now));
            synchronized (la) {
                if (la.attemptCount >= LOCKOUT_THRESHOLD) {
                    lockUserAccount(credo, now, la);
                } else { 
                    processLoginAttempt(credo, now, la);
                }
            }
        } catch (final Exception ex) {
            LOGGER.fatal(ex);
            getResponse().setEntity(restUtil.errorJsonRepresentation(ex.getMessage()));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        } finally {
            LOGGER.debug(format("LOGIN ATTEMPT RESPONSE TIME: %s%n", TimeUnit.MILLISECONDS.convert(System.nanoTime() - nanoStart, TimeUnit.NANOSECONDS)));
        }
    }

    private void lockUserAccount(final Credentials credo, final DateTime now, final LoginAttempts la) {
        LOGGER.warn(format("Account for user [%s] is locked after [%s] login attempts.", credo.username, la.attemptCount));
        coUser.lockoutUser(credo.username);
        LOGIN_ATTEMPTS.invalidate(credo.username); // locked out, invalidate user login attempts from cache
        respondAccessDenied();        
    }

    private void processLoginAttempt(final Credentials credo, final DateTime now, final LoginAttempts la) {
        final long timeSinceLastLoginAttempt = SECONDS.convert(now.getMillis() - la.lastAttemptTime.getMillis(), MILLISECONDS);
        final boolean isRapidFireLogin = timeSinceLastLoginAttempt < BLOCK_TIME_SECONDS;
        la.lastAttemptTime = now;
        // check if use is still blocked or need to be blocked...
        if (isRapidFireLogin && (la.blockedUntil.filter(now::isBefore).isPresent() || la.attemptCount >= 3)) {
            // if blocking time is not present then this is the first time the user is blocked and we need to assign the block time
            la.blockedUntil = of(now.plusSeconds(BLOCK_TIME_SECONDS));
            LOGGER.warn(format("Repeated login attempt [%s] for user [%s], blocked for [%s] seconds.", la.attemptCount, credo.username, BLOCK_TIME_SECONDS));
            respondUserIsBlocked(BLOCK_TIME_SECONDS);
        } else {
            la.incAttemptCount(); // only non-blocked login attempts count towards login attempts
            final Result authResult = authenticationModel.authenticate(credo.username, credo.passwd);
            if (!authResult.isSuccessful()) {
                LOGGER.warn(format("Unsuccessful login request for user [%s].", credo.username));
                if (la.attemptCount < 3) {
                    respondAccessDenied();
                } else if (la.attemptCount < LOCKOUT_THRESHOLD){
                    la.blockedUntil = of(now.plusSeconds(BLOCK_TIME_SECONDS));
                    LOGGER.warn(format("Repeated unsuccessful login attempt [%s] for user [%s], blocked for [%s] seconds.", la.attemptCount, credo.username, BLOCK_TIME_SECONDS));
                    respondUserIsBlocked(BLOCK_TIME_SECONDS);
                } else {
                    lockUserAccount(credo, now, la);
                }
            } else {
                LOGIN_ATTEMPTS.invalidate(credo.username); // logged in successful, invalidate user login attempts from cache
                // create a new session for an authenticated user...
                final User user = (User) authResult.getInstance();
                final UserSession session = coUserSession.newSession(user, credo.trustedDevice);
         
                // ...and provide the response with an authenticating cookie
                assignAuthenticatingCookie(user, constants.now(), session.getAuthenticator().get(), domainName, path, getRequest(), getResponse());
                getResponse().setEntity(new JsonRepresentation("{\"msg\": \"Credentials are valid.\"}"));
            }
        }
    }

    private void respondUserIsBlocked(final long delaySeconds) {
        getResponse().setEntity(new JsonRepresentation(format("{\"msg\": \"Blocked for [%s] seconds.\"}", delaySeconds)));
        getResponse().setStatus(Status.CLIENT_ERROR_PRECONDITION_FAILED);
    }

    private void respondAccessDenied() {
        getResponse().setEntity(new JsonRepresentation("{\"msg\": \"Invalid credentials.\"}"));
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    /**
     * This is just a convenient wrapper for JSON login package.
     *
     */
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
            } catch (final JsonProcessingException e) {
                LOGGER.error(e);
                return "could not serialise to JSON";
            }
        }
    }

    /**
     * A structure to track login attempts.
     * Its instances are mutable and need to be synchronised on before any changes.
     */
    private static class LoginAttempts {
        private int attemptCount;
        private Optional<DateTime> blockedUntil;
        private DateTime lastAttemptTime;

        public int incAttemptCount() {
            attemptCount = attemptCount + 1;
            return attemptCount;
        }

        public LoginAttempts(final int attemptCount, final Optional<DateTime> blockedUntil, final DateTime lastAttemptTime) {
            this.attemptCount = attemptCount;
            this.blockedUntil = blockedUntil;
            this.lastAttemptTime = lastAttemptTime;
        }
    }
}
