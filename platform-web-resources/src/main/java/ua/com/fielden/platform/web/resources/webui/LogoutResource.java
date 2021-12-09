package ua.com.fielden.platform.web.resources.webui;

import static org.restlet.data.MediaType.TEXT_HTML;
import static ua.com.fielden.platform.web.resources.webui.FileResource.createRepresentation;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.extractAuthenticator;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.mkAuthenticationCookieToExpire;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * A web resource handling explicit user logins.
 *
 * @author TG Team
 *
 */
public class LogoutResource extends AbstractWebResource {

    public static final String BINDING_PATH = "/logout";
    
    private final Logger logger = LogManager.getLogger(LogoutResource.class);

    private final IWebResourceLoader webResourceLoader;
    private final IUserProvider userProvider;
    private final IUser coUser;
    private final IUserSession coUserSession;
    private final String domainName;
    private final String path;

    /**
     * Creates {@link LogoutResource}.
     */
    public LogoutResource(
            final IWebResourceLoader webResourceLoader,
            final IUserProvider userProvider,
            final IUser coUser,
            final IUserSession coUserSession,
            final String domainName,
            final String path,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.webResourceLoader = webResourceLoader;
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
                }
                // let's use this opportunity to clear expired sessions for the user
                coUserSession.clearExpired(userProvider.getUser());
                return loggedOutPage();
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

    public Representation loggedOutPage() {
        try {
            return createRepresentation(webResourceLoader, TEXT_HTML, "/app/logout.html", getReference().getRemainingPart());
        } catch (final Exception ex) {
            logger.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }

}
