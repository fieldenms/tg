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
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.security.exceptions.SecurityException;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * A web resource handling user login reset requests.
 *
 * @author TG Team
 *
 */
public class LoginCompleteResetResource extends ServerResource {
    
    public static final String BINDING_PATH = "/reset_password/{uuid}";

    private static final String uuidExpiredError = "Password reset session has expired.";
    private static final String weakPasswordError = "Password is not strong enough.";
    private static final String passwordMismatchError = "The new and confirmed passwords do not match.";
    private static final String demoPasswordError = "Demo password should not be used.";
    private static final String demoPassword = "Ambulance services save many lives.";

    private final Logger logger = Logger.getLogger(LoginCompleteResetResource.class);

    private final IUser coUser;
    
    /**
     * Creates {@link LoginCompleteResetResource}.
     */
    public LoginCompleteResetResource(//
            final IUser coUser,
            final Context context, //
            final Request request, //
            final Response response) {
        init(context, request, response);
        this.coUser = coUser;
    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            final String uuid = (String) getRequest().getAttributes().get("uuid");

            // if the UUID is invalid then redirect the user to the password reset resource
            if (StringUtils.isEmpty(uuid) || !coUser.isPasswordResetUuidValid(uuid)) {
                return pageToReportResetSessionExpiration(logger);
            } else {
                final Optional<User> user = coUser.findUserByResetUuid(uuid);
                if (user.isPresent()) {
                    return pageToProvideNewPassword(uuid, logger);
                } else {
                    final SecurityException securityException = new SecurityException(format("Could not find a user matching requested UUID [%s].", uuid));
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

    private static Representation pageToProvideNewPassword(final String uuid, final Logger logger) {
        try {
            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/login-complete-reset.html")
                    .replace("@title", "Login Complete Reset")
                    .replace("@demoPassword", demoPassword)
                    .replace("@demoPasswdError", demoPasswordError)
                    .replace("@uuidExpired", uuidExpiredError)
                    .replace("@weakPassword", weakPasswordError)
                    .replace("@passwordMismatch", passwordMismatchError)
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
            final String uuid = form.getValues("uuid");
            // if the UUID is invalid then redirect the user to the password reset resource
            if (StringUtils.isEmpty(uuid) || !coUser.isPasswordResetUuidValid(uuid)) {
                getResponse().setEntity(new JsonRepresentation(format("{\"msg\": \"%s\"}", uuidExpiredError)));
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            } else {
                final Optional<User> user = coUser.findUserByResetUuid(uuid);
                if (user.isPresent()) {
                    final String passwd = form.getValues("passwd"); 
                    final String passwdConfirmed = form.getValues("passwd-confirmed");
                    // validate the password
                    if (demoPassword.equalsIgnoreCase(passwd)) {
                        getResponse().setEntity(new JsonRepresentation(format("{\"msg\": \"%s\"}", demoPasswordError)));
                        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    } else if (!coUser.isPasswordStrong(passwd)) {
                        getResponse().setEntity(new JsonRepresentation(format("{\"msg\": \"%s\"}", weakPasswordError)));
                        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    } else if (!passwd.equals(passwdConfirmed)) {
                        getResponse().setEntity(new JsonRepresentation(format("{\"msg\": \"%s\"}", passwordMismatchError)));
                        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    } else {
                        // the password has passed the validation, so it can be associated with the user
                        coUser.resetPasswd(user.get(), passwd);
                    }
                } else {
                    final SecurityException securityException = new SecurityException(format("Could not find a user matching requested UUID [%s].", uuid));
                    throw securityException;
                }
            }
        } catch (final Exception ex) {
            logger.fatal(ex);
            getResponse().setEntity(new JsonRepresentation(format("{\"msg\": \"%s.\"}", ex.getMessage())));
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

}
