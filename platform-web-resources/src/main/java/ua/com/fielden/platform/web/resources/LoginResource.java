package ua.com.fielden.platform.web.resources;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.security.session.Authenticator.fromString;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.assignAuthenticatingCookie;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.Encoding;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
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
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * A web resource handling explicit user logins.
 *
 * @author TG Team
 *
 */
public class LoginResource extends ServerResource {
    private final Logger logger = Logger.getLogger(LoginResource.class);

    private final IAuthenticationModel authenticationModel;
    private final IUserProvider userProvider;
    private final IUserEx coUserEx;
    private final IUserSession coUserSession;
    private final RestServerUtil restUtil;

    /**
     * Creates {@link LoginResource} and initialises it with centre instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public LoginResource(//
            final IAuthenticationModel authenticationModel,
            final IUserProvider userProvider,
            final IUserEx coUserEx,
            final IUserSession coUserSession,//
            final RestServerUtil restUtil,//
            final Context context, //
            final Request request, //
            final Response response) {
        init(context, request, response);
        this.authenticationModel = authenticationModel;
        this.userProvider = userProvider;
        this.coUserEx = coUserEx;
        this.coUserSession = coUserSession;
        this.restUtil = restUtil;

        // TODO username can only come from an authentication cookie
        //final String username = (String) request.getAttributes().get("username");
        //injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserEx.class));

    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            // check if there is a valid authenticator
            // if there is then should respond with redirection to root /.
            final Cookie cookie = getRequest().getCookies().getFirst(AUTHENTICATOR_COOKIE_NAME);
            if (cookie != null) {
                final String authenticator = cookie.getValue();
                if (!StringUtils.isEmpty(authenticator)) {
                    final Authenticator auth = fromString(authenticator);
                    userProvider.setUsername(auth.username, coUserEx);
                    final Optional<UserSession> session = coUserSession.currentSession(userProvider.getUser(), authenticator);
                    if (session.isPresent()) {
                        // response needs to be provided with an authenticating cookie
                        assignAuthenticatingCookie(session.get().getAuthenticator().get().toString(), getResponse());
                        // response needs to provide redirection instructions
                        getResponse().redirectSeeOther("/");
                        return new EmptyRepresentation();
                    }
                }
            }

            // otherwise just load the login page for user to login in explicitly
            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/login.html").replaceAll("@title", "Login").getBytes("UTF-8");
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
        } catch (final UnsupportedEncodingException ex) {
            logger.fatal(ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return restUtil.errorJSONRepresentation(ex);
        }
    }

    @Put
    @Override
    public Representation put(final Representation entity) throws ResourceException {
        try {
            final String username = getRequest().getResourceRef().getQueryAsForm().getFirstValue("username");
            final String password = getRequest().getResourceRef().getQueryAsForm().getFirstValue("passwd");
            final Boolean isDeviceTrusted = Boolean.parseBoolean(getRequest().getResourceRef().getQueryAsForm().getFirstValue("trusted-device"));

            final Result authResult = authenticationModel.authenticate(username, password);
            if (!authResult.isSuccessful()) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                final byte[] body = authResult.getMessage().getBytes("UTF-8");
                return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
            } else {
                // create a new session for an authenticated user...
                final User user = (User) authResult.getInstance();
                final UserSession session = coUserSession.newSession(user, isDeviceTrusted);

                // ...and provide the response with an authenticating cookie
                assignAuthenticatingCookie(session.getAuthenticator().get().toString(), getResponse());

                // the response body should provide an URI where successful login should be redirected to
                final byte[] body = "/".getBytes("UTF-8");
                return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
            }
        } catch (final UnsupportedEncodingException ex) {
            logger.fatal(ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
            return restUtil.errorJSONRepresentation(ex);
        }
    }



}
