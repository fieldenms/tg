package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
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
            
            final long computationStart = System.currentTimeMillis();
            if (maybeUserSecret.isPresent()) {
                final UserSecret secret = maybeUserSecret.get();
                final User user = secret.getKey(); 
                if (!StringUtils.isEmpty(user.getEmail())) {
                    final String emailBody = makePasswordRestEmail(constants.appName(), appUri, secret.getResetUuid());
                    final SmtpEmailSender sender = new SmtpEmailSender(constants.smtpServer());
                    sender.sendPlainMessage(constants.fromEmailAddress(), 
                                            user.getEmail(), 
                                            format("[%s] Please reset your password", constants.appName()), 
                                            emailBody);
                }
            }
            
            delayResponse(computationStart, System.currentTimeMillis());

            getResponse().setEntity(response);

        } catch (final Exception ex) {
            logger.fatal(ex);
            getResponse().setEntity(new JsonRepresentation(format("{\"msg\": \"%s.\"}", "There was an error when attempting to request a password reset.")));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    /**
     * A function to randomly delay the response to reduce the risk of a timing-based attack.
     *
     * @param computationStart
     * @param computationEnd
     */
    private void delayResponse(final long computationStart, final long computationEnd) {
        try {
            final int computationTime = (int) (computationEnd - computationStart);
            if (computationTime < 1000) {
                final Random rnd = new Random();
                final int sleepTime = 300 + rnd.nextInt(1000 - computationTime);
                Thread.sleep(sleepTime);
            }
        } catch (final Exception e) {
            logger.debug("Interrupted sleep during login.", e);
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
        builder.append(format("If you don't use this link within 24 hours, it will expire. To get a new password reset link, visit %sforgotten", appUri));
        builder.append("\n\n");
        builder.append("Thanks,\n");
        builder.append("Your support team");

        return builder.toString();
    }

}
