package ua.com.fielden.platform.web.resources.webui;

import org.apache.commons.lang3.StringUtils;
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
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A web resource handling user login reset requests.
 *
 * @author TG Team
 *
 */
public class LoginCompleteResetResource extends AbstractWebResource {

    public static final String ERR_RESETTING_PASSWORD = "Could not reset the password.";
    public static final String ERR_NO_USER_WITH_UUID = "Could not find a user matching requested UUID [%s].";

    private static final String FILE_LOGIN_EXPIRED_RESET_HTML = "ua/com/fielden/platform/web/login-expired-reset.html";
    private static final String FILE_LOGIN_COMPLETE_RESET_HTML = "ua/com/fielden/platform/web/login-complete-reset.html";
    public static final String BINDING_PATH = "/reset_password/{uuid}";

    private static final String UUID_EXPIRED_ERROR = "Password reset session has expired.";
    private static final String WEAK_SECRET_ERROR = "Password is not strong enough.";
    private static final String SECRET_MISMATCH_ERROR = "The new and confirmed passwords do not match.";
    private static final String DEMO_SECRET_ERROR = "Demo password should not be used.";
    private final String demoSecret;

    private static final Logger LOGGER = LogManager.getLogger(LoginCompleteResetResource.class);

    private final ICompanionObjectFinder coFinder;
    private final IUserProvider up;
    
    /**
     * Creates {@link LoginCompleteResetResource}.
     */
    public LoginCompleteResetResource(//
            final String demoSecret,
            final ICompanionObjectFinder coFinder,
            final IUserProvider up,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.demoSecret = demoSecret;
        this.coFinder = coFinder;
        this.up = up;
    }

    @Override
    protected Representation get() {
        try {
            final String uuid = (String) getRequest().getAttributes().get("uuid");

            final IUser coUser = coFinder.find(User.class, true);
            up.setUsername(User.system_users.SU.name(), coFinder.find(User.class, true));

            // if the UUID is invalid, then redirect the user to the password reset resource
            if (StringUtils.isEmpty(uuid) || !coUser.isPasswordResetUuidValid(uuid, serverNow())) {
                return pageToReportResetSessionExpiration();
            } else {
                final Optional<User> user = coUser.findUserByResetUuid(uuid);
                if (user.isPresent()) {
                    return pageToProvideNewPassword(uuid);
                } else {
                    throw new SecurityException(ERR_NO_USER_WITH_UUID.formatted(uuid));
                }
            }
        } catch (final RuntimeException ex) {
            LOGGER.fatal(ex);
            throw ex;
        } catch (final Exception ex) {
            LOGGER.fatal(ex);
            throw new SecurityException(ERR_RESETTING_PASSWORD, ex);
        }
    }

    private static Representation pageToReportResetSessionExpiration() {
        try {
            final byte[] body = Objects.requireNonNull(ResourceLoader.getText(FILE_LOGIN_EXPIRED_RESET_HTML)).getBytes(UTF_8);
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body), MediaType.TEXT_HTML));
        } catch (final Exception ex) {
            LoginCompleteResetResource.LOGGER.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }

    private Representation pageToProvideNewPassword(final String uuid) {
        try {
            final byte[] body = Objects.requireNonNull(ResourceLoader.getText(FILE_LOGIN_COMPLETE_RESET_HTML))
                    .replace("@title", "Login Complete Reset")
                    .replace("@demoPassword", demoSecret)
                    .replace("@demoPasswdError", DEMO_SECRET_ERROR)
                    .replace("@uuidExpired", UUID_EXPIRED_ERROR)
                    .replace("@weakPassword", WEAK_SECRET_ERROR)
                    .replace("@passwordMismatch", SECRET_MISMATCH_ERROR)
                    .replace("@uuid", uuid)
                    .getBytes(UTF_8);
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body), MediaType.TEXT_HTML));
        } catch (final Exception ex) {
            LoginCompleteResetResource.LOGGER.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Post
    public void resetLogin(final Representation entity) {
        final String msgTemplate = "{\"msg\": \"%s\"}";
        try {
            final Form form = new Form(entity);
            final String uuid = form.getValues("uuid");
            final IUser coUser = coFinder.find(User.class, true);
            // If the UUID is invalid, then redirect the user to the password reset resource.
            if (StringUtils.isEmpty(uuid) || !coUser.isPasswordResetUuidValid(uuid, serverNow())) {
                getResponse().setEntity(new JsonRepresentation(msgTemplate.formatted(UUID_EXPIRED_ERROR)));
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            } else {
                final Optional<User> user = coUser.findUserByResetUuid(uuid);
                if (user.isEmpty()) {
                    throw new SecurityException(ERR_NO_USER_WITH_UUID.formatted(uuid));
                }
                 
                // A user has been identified, and it should be recognised as the current one before proceeding with any further changes.
                up.setUsername(user.get().getKey(), coUser);
                
                final String passwd = form.getValues("passwd");
                final String passwdConfirmed = form.getValues("passwd-confirmed");
                // validate the password
                if (demoSecret.equalsIgnoreCase(passwd)) {
                    getResponse().setEntity(new JsonRepresentation(format(msgTemplate, DEMO_SECRET_ERROR)));
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                } else if (!coUser.isPasswordStrong(passwd)) {
                    getResponse().setEntity(new JsonRepresentation(format(msgTemplate, WEAK_SECRET_ERROR)));
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                } else if (!passwd.equals(passwdConfirmed)) {
                    getResponse().setEntity(new JsonRepresentation(format(msgTemplate, SECRET_MISMATCH_ERROR)));
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                } else {
                    // the password has passed the validation, so it can be associated with the user
                    final IUser co$User = coFinder.find(User.class, false);
                    co$User.resetPasswd(co$User.findUser(user.get().getKey()), passwd);
                }
            }
        } catch (final Exception ex) {
            LOGGER.fatal(ex);
            getResponse().setEntity(new JsonRepresentation(format(msgTemplate, ex.getMessage())));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    private static Date serverNow() {
        return new Date();
    }

}
