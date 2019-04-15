package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.extractAuthenticator;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CookieSetting;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.security.AbstractWebResourceGuard;

/**
 * A web resource handling explicit user logins.
 *
 * @author TG Team
 *
 */
public class LogoutResource extends ServerResource {

    public static final String BINDING_PATH = "/logout";
    
    private final Logger logger = Logger.getLogger(LogoutResource.class);

    private final IUserProvider userProvider;
    private final IUser coUser;
    private final IUserSession coUserSession;
    private final String domainName;
    private final String path;

    /**
     * Creates {@link LogoutResource}.
     */
    public LogoutResource(
            final IUserProvider userProvider,
            final IUser coUser,
            final IUserSession coUserSession,
            final String domainName,
            final String path,
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);
        this.userProvider = userProvider;
        this.coUser = coUser;
        this.coUserSession = coUserSession;
        this.domainName = domainName;
        this.path = path;
    }

    @Get
    public Representation logout() {
        try {
            // check if there is a valid authenticator
            // if there is then the logout request is authentic and should be honored
            final Optional<Authenticator> oAuth = extractAuthenticator(getRequest());
            if (oAuth.isPresent()) {
                final Authenticator auth = oAuth.get();
                userProvider.setUsername(auth.username, coUser);
                final Optional<UserSession> session = coUserSession.currentSession(userProvider.getUser(), auth.toString(), false);
                if (session.isPresent()) {
                    coUserSession.clearSession(session.get());
                    return loggedOutPage();
                }
            }

            // otherwise return the response as if the logout actually happened
            return loggedOutPage();
        } catch (final Exception ex) {
            // in case of an exception try try return a login page.
            logger.fatal(ex);
            getResponse().redirectSeeOther("/login");
            return new EmptyRepresentation();
        } finally {
            final Response response = getResponse();
            response.getCookieSettings().clear();
            response.getCookieSettings().add(mkAuthenticationCookieToExpire(domainName, path));
        }
    }

    private static CookieSetting mkAuthenticationCookieToExpire(final String domainName, final String path) {
        final CookieSetting newCookie = new CookieSetting(
                0 /*version*/,
                AbstractWebResourceGuard.AUTHENTICATOR_COOKIE_NAME /*name*/,
                "" /*value*/,
                path,
                domainName,
                null /*comment*/,
                0 /* number of seconds before cookie expires */,
                true /*secure*/, // if secure is set to true then this cookie would only be included into the request if it is done over HTTPS!
                true /*accessRestricted*/);
        return newCookie;
    }

    public Representation loggedOutPage() {
        try {
            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/logout.html").replaceAll("@title", "Logout").getBytes("UTF-8");
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body), MediaType.TEXT_HTML));
        } catch (final Exception ex) {
            logger.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }

}
