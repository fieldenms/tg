package ua.com.fielden.platform.web.resources.webui;

import com.google.common.util.concurrent.AtomicDouble;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.annotations.AppUri;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.restlet.data.MediaType.TEXT_HTML;
import static ua.com.fielden.platform.security.user.UserSecretCo.RESET_UUID_EXPIRATION_IN_MUNUTES;
import static ua.com.fielden.platform.security.user.UserSecretCo.passwordResetExpirationTime;
import static ua.com.fielden.platform.web.resources.webui.FileResource.createRepresentation;

/**
 * A web resource to initiate user login recovery procedure.
 *
 * @author TG Team
 *
 */
public class LoginInitiateResetResource extends AbstractWebResource {

    private static final Logger LOGGER = LogManager.getLogger(LoginInitiateResetResource.class);

    public static final String BINDING_PATH = "/forgotten";
    public static final String FILE_APP_LOGIN_INITIATE_RESET_HTML = "/app/login-initiate-reset.html";

    public static final String PARAM_USERNAME_OR_EMAIL = "username_or_email";

    private final IWebResourceLoader webResourceLoader;
    private final String appUri;
    private final ICompanionObjectFinder coFinder;
    private final IUserProvider up;
    private final IUniversalConstants constants;

    /**
     * Creates {@link LoginInitiateResetResource}.
     */
    public LoginInitiateResetResource(
            final IWebResourceLoader webResourceLoader,
            @AppUri final String appUri,
            final IUniversalConstants constants,
            final ICompanionObjectFinder coFinder,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Context context, //
            final Request request, //
            final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.webResourceLoader = webResourceLoader;
        this.appUri = appUri;
        this.coFinder = coFinder;
        this.constants = constants;
        this.up = userProvider;
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

            final Optional<UserSecret> maybeUserSecret = co$User.assignPasswordResetUuid(usernameOrEmail, passwordResetExpirationTime());

            adjustResponseTime(maybeUserSecret.filter(secret -> !isEmpty(secret.getKey().getEmail())).map(this::reset));

            getResponse().setEntity(response);

        } catch (final Exception ex) {
            LOGGER.fatal(ex);
            getResponse().setEntity(new JsonRepresentation(format("{\"msg\": \"%s.\"}", "There was an error when attempting to request a password reset.")));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    /**
     * An action to initiate password reset to a valid user, returning the number of milliseconds it took to perform the work.
     * The result is used to randomise the response time for reducing the risk of timing-based attacks.
     *
     * @param secret
     * @return
     */
    private int reset(final UserSecret secret) {
        final long computationStart = System.currentTimeMillis();
        final String emailAddr = secret.getKey().getEmail();
        final String emailBody = makePasswordRestEmail(constants.appName(), appUri, secret.getResetUuid());
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

    /**
     * Computes the mean and the variance based on the online algorithm outlined in <pre>https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance</pre>.
     *
     * @param newReading
     * @return
     */
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

    /**
     * A function to randomly delay the response to reduce the risk of a timing-based attack.
     *
     * @param computeTime
     */
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

    private String makePasswordRestEmail(final String appName, final String appUri, final String resetUuid) {
        final StringBuilder builder = new StringBuilder();
        builder.append(format("We heard that you lost your %s password. Sorry about that!", appName));
        builder.append("\n\n");
        builder.append(format("But don't worry! You can use the following link within %s minutes to reset your password:", RESET_UUID_EXPIRATION_IN_MUNUTES));
        builder.append("\n\n");
        builder.append(format("%sreset_password/%s", appUri, resetUuid));
        builder.append("\n\n");
        builder.append(format("If you don't use this link within %s minutes, it will expire. To get a new password reset link, visit %sforgotten", RESET_UUID_EXPIRATION_IN_MUNUTES, appUri));
        builder.append("\n\n");
        builder.append("Thanks,\n");
        builder.append("Your support team");

        return builder.toString();
    }

}
