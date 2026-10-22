package ua.com.fielden.platform.web.resources.webui;

import com.google.common.util.concurrent.AtomicDouble;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.mail.SmtpEmailSender;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.security.user.UserSecretCo;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.annotations.AppUri;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.restlet.data.MediaType.TEXT_HTML;
import static ua.com.fielden.platform.security.user.UserSecretCo.RESET_UUID_EXPIRATION_IN_MINUTES;
import static ua.com.fielden.platform.security.user.UserSecretCo.resetUuidExpirationTime;
import static ua.com.fielden.platform.web.resources.webui.FileResource.createRepresentation;

/// A web resource to initiate user login recovery procedure.
///
/// A password reset is initiated at most once per user within the cooldown of [PasswordResetCooldown].
/// A reset link that remains valid for at least [#MIN_REUSED_LINK_VALIDITY_IN_MINUTES] is sent again instead of being replaced,
/// so that repeated requests do not invalidate a link that the user has received.
/// Requests for a user in cooldown receive the same response, and the same response-time adjustment, as requests for an unknown user,
/// so that the response does not disclose whether an account exists.
///
public class LoginInitiateResetResource extends AbstractWebResource {

    private static final Logger LOGGER = LogManager.getLogger(LoginInitiateResetResource.class);

    public static final String BINDING_PATH = "/forgotten";
    public static final String FILE_APP_LOGIN_INITIATE_RESET_HTML = "/app/login-initiate-reset.html";

    public static final String PARAM_USERNAME_OR_EMAIL = "username_or_email";

    /// The minimum remaining validity of a reset link for it to be sent again instead of being replaced.
    ///
    public static final int MIN_REUSED_LINK_VALIDITY_IN_MINUTES = 5;

    private final IWebResourceLoader webResourceLoader;
    private final String appUri;
    private final ICompanionObjectFinder coFinder;
    private final IUserProvider up;
    private final IUniversalConstants constants;
    private final PasswordResetCooldown cooldown;

    public LoginInitiateResetResource(
            final IWebResourceLoader webResourceLoader,
            @AppUri final String appUri,
            final IUniversalConstants constants,
            final ICompanionObjectFinder coFinder,
            final IUserProvider userProvider,
            final PasswordResetCooldown cooldown,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Context context,
            final Request request,
            final Response response)
    {
        super(context, request, response, deviceProvider, dates);
        this.webResourceLoader = webResourceLoader;
        this.appUri = appUri;
        this.coFinder = coFinder;
        this.constants = constants;
        this.up = userProvider;
        this.cooldown = cooldown;
    }

    @Override
    protected Representation get() {
        return pageToProvideUsernameForPasswordReset(webResourceLoader, getReference());
    }

    private static Representation pageToProvideUsernameForPasswordReset(final IWebResourceLoader webResourceLoader, final Reference reference) {
        try {
            return createRepresentation(webResourceLoader, TEXT_HTML, FILE_APP_LOGIN_INITIATE_RESET_HTML, reference.getRemainingPart());
        } catch (final Exception ex) {
            LoginInitiateResetResource.LOGGER.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }
    
    @Post
    public void initiateLoginReset(final Representation entity) {
        try {
            
            final JsonRepresentation response = new JsonRepresentation("{\"msg\": \"Reset password email is probably sent.\"}");
            final String usernameOrEmail = new Form(entity).getValues(PARAM_USERNAME_OR_EMAIL);
            
            // The user initiating a password reset is not logged in.
            // Therefore, SU is used as the current user for auditing purposes.
            up.setUsername(User.system_users.SU.name(), coFinder.find(User.class, true));

            final IUser co$User = coFinder.find(User.class, false);

            final Optional<PasswordResetLink> maybeLink = findOrAssignPasswordResetUuid(co$User, cooldown, usernameOrEmail, constants);

            adjustResponseTime(maybeLink.filter(link -> !isEmpty(link.secret().getKey().getEmail())).map(this::reset));

            getResponse().setEntity(response);

        } catch (final Exception ex) {
            LOGGER.fatal(ex);
            getResponse().setEntity(new JsonRepresentation(format("{\"msg\": \"%s.\"}", "There was an error when attempting to request a password reset.")));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        } finally {
            // This handler sets the current user to SU (above); clear it so a pooled worker thread does not retain it after the request.
            // This resource is attached at the component level and is not covered by the application-level cleanup filter.
            up.clearUser();
        }
    }

    /// A password reset link, identified by the reset UUID of `secret`, which remains valid for `validityInMinutes` (in whole minutes) from the time of the request by the server clock.
    ///
    record PasswordResetLink(UserSecret secret, long validityInMinutes) {}

    /// Returns a password reset link for the user identified by `usernameOrEmail`, unless no such user can reset their password, or the user is in cooldown.
    /// The UUID already assigned to the user is kept if it remains valid for at least [#MIN_REUSED_LINK_VALIDITY_IN_MINUTES];
    /// otherwise, a new UUID that is valid for [UserSecretCo#RESET_UUID_EXPIRATION_IN_MINUTES] is assigned.
    ///
    /// The time of the request is taken from the server clock, `constants.serverNow()`, against which [LoginCompleteResetResource] validates reset links.
    /// [IUniversalConstants#now()] is not used, as in the independent time-zone mode it shifts the instant by the offset of the request time-zone.
    ///
    static Optional<PasswordResetLink> findOrAssignPasswordResetUuid(
            final IUser co$User,
            final PasswordResetCooldown cooldown,
            final String usernameOrEmail,
            final IUniversalConstants constants)
    {
        final DateTime now = constants.serverNow();
        final Date validUntil = now.plusMinutes(MIN_REUSED_LINK_VALIDITY_IN_MINUTES).toDate();
        final Date expirationTime = now.plusMinutes(RESET_UUID_EXPIRATION_IN_MINUTES).toDate();
        return co$User.findUserForPasswordReset(usernameOrEmail)
                .filter(cooldown::tryStart)
                .map(user -> co$User.findOrAssignPasswordResetUuid(user, validUntil, expirationTime))
                .map(secret -> new PasswordResetLink(
                        secret,
                        MILLISECONDS.toMinutes(resetUuidExpirationTime(secret.getResetUuid()).orElseThrow().getTime() - now.getMillis())));
    }

    /// An action to email password reset `link` to a valid user, returning the number of milliseconds it took to perform the work.
    /// The result is used to randomise the response time for reducing the risk of timing-based attacks.
    ///
    private int reset(final PasswordResetLink link) {
        final long computationStart = System.currentTimeMillis();
        final UserSecret secret = link.secret();
        final String emailAddr = secret.getKey().getEmail();
        final String emailBody = makePasswordRestEmail(constants.appName(), appUri, secret.getResetUuid(), link.validityInMinutes());
        final SmtpEmailSender sender = new SmtpEmailSender(constants.smtpServer());
        sender.sendPlainMessage(constants.fromEmailAddress(),
                                emailAddr,
                                format("[%s] Please reset your password", constants.appName()),
                                emailBody);
        return (int) (System.currentTimeMillis() - computationStart);
    }


    private static final Object lockForStats = new Object();
    private static final AtomicInteger numberOfComputations = new AtomicInteger(1);
    private static final AtomicDouble avgComputeTime = new AtomicDouble(300.00);
    private static final AtomicDouble var2ComputeTime = new AtomicDouble(0);

    /// Computes the mean and the variance based on the online algorithm outlined in [Wikipeida](https://en.wikipedia.org/wiki/Algorithms\_for\_calculating\_variance).
    ///
    private static void computeStats(final int newReading) {
        synchronized (lockForStats) {
            if (newReading > 0) {
                final int n = numberOfComputations.incrementAndGet();
                final double delta = newReading - avgComputeTime.get();

                final double avg = avgComputeTime.get() + delta / n;
                final double delta2 = newReading - avg;
                final double var2 = var2ComputeTime.get() + delta * delta2;

                avgComputeTime.set(avg);
                var2ComputeTime.set(var2);
            }
        }
    }

    /// A function to randomly delay the response to reduce the risk of a timing-based attack.
    ///
    private void adjustResponseTime(final Optional<Integer> computeTime) {
        computeStats(computeTime.orElse(0));

        if (computeTime.isEmpty()) {
            try {
                final int avg = (int) avgComputeTime.get();
                final int var = (int) Math.sqrt(var2ComputeTime.get() / numberOfComputations.get()) + 1;
                final Random rnd = new Random();
                final int sleepTime = avg + (rnd.nextBoolean() ? 1 : -1) * rnd.nextInt(var);
                Thread.sleep(sleepTime);
            } catch (final Exception e) {
                LOGGER.debug("Interrupted sleep during login.", e);
            }
        }
    }

    private String makePasswordRestEmail(final String appName, final String appUri, final String resetUuid, final long validityInMinutes) {
        return """
                We heard that you lost your %s password. Sorry about that!

                But don’t worry! You can use the following link within %s minutes to reset your password:

                %sreset_password/%s

                If you don’t use this link within %s minutes, it will expire. To get a new password reset link, visit %sforgotten

                Thanks,
                Your support team"""
                .formatted(appName, validityInMinutes, appUri, resetUuid, validityInMinutes, appUri);
    }

}
