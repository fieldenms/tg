package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.security.user.UserSecret.RESER_UUID_EXPIRATION_IN_MUNUTES;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.google.common.util.concurrent.AtomicDouble;

import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.mail.SmtpEmailSender;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserSecret;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.annotations.AppUri;

/**
 * A web resource to initiate user login recovery procedure.
 *
 * @author TG Team
 *
 */
public class LoginInitiateResetResource extends ServerResource {
    
    public static final String BINDING_PATH = "/forgotten";

    private final Logger logger = Logger.getLogger(LoginInitiateResetResource.class);

    private final String appUri;
    private final ICompanionObjectFinder coFinder;
    private final IUserProvider up;
    private final IUniversalConstants constants;

    /**
     * Creates {@link LoginInitiateResetResource}.
     */
    public LoginInitiateResetResource(//
            @AppUri final String appUri,
            final IUniversalConstants constants,
            final ICompanionObjectFinder coFinder,
            final IUserProvider userProvider,
            final Context context, //
            final Request request, //
            final Response response) {
        init(context, request, response);
        this.appUri = appUri;
        this.coFinder = coFinder;
        this.constants = constants;
        this.up = userProvider;
    }

    @Override
    protected Representation get() {
        return pageToProvideUsernameForPasswordReset("Login Reset Request", logger);
    }

    private static Representation pageToProvideUsernameForPasswordReset(final String title, final Logger logger) {
        try {
            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/login-initiate-reset.html")
                    .replace("@title", title)
                    .getBytes("UTF-8");
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body), MediaType.TEXT_HTML));
        } catch (final Exception ex) {
            logger.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }
    
    @Post
    public void initiateLoginReset(final Representation entity) {
        try {
            
            final JsonRepresentation response = new JsonRepresentation("{\"msg\": \"Reset password email is probably sent.\"}");
            final Form form = new Form(entity);
            final String usernameOrEmail = form.getValues("username_or_email");
            
            // the user initiating a password reset is not logged in, therefore SU is used as the current user for auditing purposes
            up.setUsername(User.system_users.SU.name(), coFinder.find(User.class, true));

            final IUser co$User = coFinder.find(User.class, false);
            final Optional<UserSecret> maybeUserSecret = co$User.assignPasswordResetUuid(usernameOrEmail);

            adjustResponseTime(maybeUserSecret.filter(secret -> !isEmpty(secret.getKey().getEmail())).map(this::reset));

            getResponse().setEntity(response);

        } catch (final Exception ex) {
            logger.fatal(ex);
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
     * @param computationStart
     * @param computationEnd
     */
    private void adjustResponseTime(final Optional<Integer> computeTime) {
        computeStats(computeTime.orElse(0));

        if (!computeTime.isPresent()) {
            try {
                final int avg = (int) avgComputeTime.get();
                final int var = (int) Math.sqrt(var2ComputeTime.get() / numberOfComputations.get()) + 1;
                final Random rnd = new Random();
                final int sleepTime = avg + (rnd.nextBoolean() ? 1 : -1) * rnd.nextInt(var);
                Thread.sleep(sleepTime);
            } catch (final Exception e) {
                logger.debug("Interrupted sleep during login.", e);
            }
        }
    }

    private String makePasswordRestEmail(final String appName, final String appUri, final String resetUuid) {
        final StringBuilder builder = new StringBuilder();
        builder.append(format("We heard that you lost your %s password. Sorry about that!", appName));
        builder.append("\n\n");
        builder.append("But don't worry! You can use the following link within the next day to reset your password:");
        builder.append("\n\n");
        builder.append(format("%sreset_password/%s", appUri, resetUuid));
        builder.append("\n\n");
        builder.append(format("If you don't use this link within %s minutes, it will expire. To get a new password reset link, visit %sforgotten", RESER_UUID_EXPIRATION_IN_MUNUTES, appUri));
        builder.append("\n\n");
        builder.append("Thanks,\n");
        builder.append("Your support team");

        return builder.toString();
    }

}
