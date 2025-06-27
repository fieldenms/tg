package ua.com.fielden.platform.web.resources.webui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import ua.com.fielden.platform.security.session.Authenticator;
import ua.com.fielden.platform.security.session.ISsoSessionController;
import ua.com.fielden.platform.security.session.ISsoSessionController.Tokens;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.session.UserSession;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

import java.util.Optional;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.restlet.data.MediaType.TEXT_HTML;
import static ua.com.fielden.platform.web.resources.webui.FileResource.createRepresentation;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.extractAuthenticator;
import static ua.com.fielden.platform.web.security.AbstractWebResourceGuard.mkAuthenticationCookieToExpire;

/// A web resource handling explicit user logouts.
///
public class LogoutResource extends AbstractWebResource {

    public static final String BINDING_PATH = "/logout";

    private final Logger logger = LogManager.getLogger(LogoutResource.class);

    private final IWebResourceLoader webResourceLoader;
    private final IUserProvider userProvider;
    private final IUser coUser;
    private final IUserSession coUserSession;
    private final ISsoSessionController ssoSessionController;
    private final String domainName;
    private final String path;
    private final Optional<String> maybeSsoRedirectUriSignOut;

    /**
     * Creates {@link LogoutResource}.
     */
    public LogoutResource(
            final IWebResourceLoader webResourceLoader,
            final IUserProvider userProvider,
            final IUser coUser,
            final IUserSession coUserSession,
            final ISsoSessionController ssoSessionController,
            final String domainName,
            final String path,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Optional<String> maybeSsoRedirectUriSignOut,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.webResourceLoader = webResourceLoader;
        this.userProvider = userProvider;
        this.coUser = coUser;
        this.coUserSession = coUserSession;
        this.ssoSessionController = ssoSessionController;
        this.domainName = domainName;
        this.path = path;
        this.maybeSsoRedirectUriSignOut = maybeSsoRedirectUriSignOut;
    }

    @Get
    public Representation logout() {
        try {
            // Logout might be initiated from a TG-based app or from a front-channel logout process by OP (e.g. Microsoft Office 365 portal).
            // In the case of front-channel request, `sid` is expected as the request parameter.
            // However, some OPs do not send `sid`, such as Okta, where the front-channel logout is still evolving.
            // In the case of TG-based app request, a corresponding `sid` needs to be identified from the current session.
            final String sid = getQueryValue("sid");
            logger.debug(() -> format("LOGOUT sid (if any): [%s]", sid));
            if (!isEmpty(sid)) {
                coUserSession.clearAllWithSid(sid);
            }
            // check if there is a valid authenticator
            // if there is then the logout request is authentic and should be honored
            final Optional<Authenticator> oAuth = extractAuthenticator(getRequest());
            final Optional<Tokens> maybeTokens;
            if (oAuth.isPresent()) {
                final Authenticator auth = oAuth.get();
                userProvider.setUsername(auth.username, coUser);
                final Optional<UserSession> maybeSession = coUserSession.currentSession(userProvider.getUser(), auth.toString(), false);
                if (maybeSession.isPresent()) {
                    // Deletes sessions from cache and database.
                    final UserSession session = maybeSession.get();
                    // Get tokens by `sid` before the records are deleted.
                    maybeTokens = ssoSessionController.tokens(session.getSid());
                    coUserSession.clearAllWithSid(session.getSid());
                    coUserSession.clearSession(session);
                } else {
                    maybeTokens = Optional.empty();
                }
                // let's use this opportunity to clear expired sessions for the user
                coUserSession.clearExpired(userProvider.getUser());
            } else {
                maybeTokens = Optional.empty();
            }

            // In cases where Single Sign/Log-Out (SLO) is supported, and the logout request arrived with a session authenticator present,
            // it is necessary to initiate SLO redirection to OP.
            if (maybeSsoRedirectUriSignOut.isPresent() && oAuth.isPresent()) {
                final String baseSignOutUrl = maybeSsoRedirectUriSignOut.get();
                final var signOutUrl = maybeTokens.map(tokens -> baseSignOutUrl + "&id_token_hint=" + tokens.idToken()).orElse(baseSignOutUrl);
                getResponse().redirectSeeOther(signOutUrl);
                return new EmptyRepresentation();
            }

            // Otherwise, return the response as if the logout actually happened.
            return loggedOutPage();
        } catch (final Exception ex) {
            // In case of an exception, try returning the login page.
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
