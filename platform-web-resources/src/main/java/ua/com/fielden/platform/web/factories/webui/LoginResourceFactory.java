package ua.com.fielden.platform.web.factories.webui;

import static org.apache.commons.lang3.StringUtils.replace;

import org.apache.commons.lang3.StringUtils;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.representation.Representation;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.LoginResource;

/**
 * A factory for a login web resource.
 *
 * @author TG Team
 *
 */
public class LoginResourceFactory extends Restlet {

    private final RestServerUtil util;
    private final Injector injector;
    private final byte[] loginPage;
    private final byte[] loginPageForMixedMode;

    public LoginResourceFactory(final boolean trustedDeviceByDefault, final RestServerUtil util, final Injector injector) {
        this.util = util;
        this.injector = injector;
        // the same login page can be served for multiple requests
        // this is why it can be loaded and prepared just once
        final AuthMode authMode = injector.getInstance(IApplicationSettings.class).authMode();
        final String authSsoProvider = injector.getInstance(Key.get(String.class, Names.named("auth.sso.provider")));
        // For both SSO and RSO modes, the standard login page should not display the SSO button -- hence "none"
        // For SSO auto redirect would happen and for RSO this button is invalid
        this.loginPage = loadLoginPage(trustedDeviceByDefault, authMode, authSsoProvider, "none");
        // We also need to provide a login page that would support the mixed mode (if SSO mode is on), giving the user control to choose how to login.
        // This is needed for base user logins and, potentially, for users outside the organisation, which may not be eligible for SSO.
        // If the authentication mode is RSO, this page is equivalent to loginPage where no SSO button is visible.
        this.loginPageForMixedMode = loadLoginPage(trustedDeviceByDefault, AuthMode.RSO, authSsoProvider, AuthMode.SSO == authMode ? "block" : "none");
    }

    public LoginResourceFactory(final RestServerUtil util, final Injector injector) {
        this(false, util, injector);
    }

    /**
     * A helper function to load and process the login page source file.
     * The returned byte array can be reused for building {@link Representation} instances in response to GET requests.
     *
     * @param trustedDeviceByDefault
     * @param authMode – either RSO or SSO.
     * @param authSsoProvider – a user-readable string representing an Identity Provider used for SSO.
     * @param authSsoDisplay – control visibility for the SSO button; either "none" or "block" is expected as the value.
     * @return
     */
    private static byte[] loadLoginPage(final boolean trustedDeviceByDefault, final AuthMode authMode, final String authSsoProvider, final String authSsoDisplay) {
        try {
            // If authMode == SSO then we expect an auto-redirection to "/sso" with appropriate encoding of the local URI part as a query parameter.
            // If the login page contains the standard login form, this form may become visible for a split second before redirection takes place, potentially, confusing the user. 
            // In order to prevent this, the form should be invisible and the message at the top could say something like "Authenticating..."
            final String loginPage = ResourceLoader.getText("ua/com/fielden/platform/web/login.html");
            final String withLoginMsg = replace(loginPage, "@auth.login.msg", AuthMode.RSO == authMode ? "Please enter valid credentials." : "Authenticating...");
            final String withRsoPrompt = replace(withLoginMsg, "@auth.rso.display", AuthMode.RSO == authMode ? "block" : "none");
            final String withTitle = replace(withRsoPrompt, "@title", "Login");
            final String withTrusted = replace(withTitle, "@trusted", trustedDeviceByDefault ? "checked" : "");
            final String withAuthSsoDisplay = replace(withTrusted, "@auth.sso.display", authSsoDisplay);
            final String withAuthMode = replace(withAuthSsoDisplay, "@auth.mode", authMode.name());
            final String withSsoIdentity = replace(withAuthMode, "@auth.sso.provider", authSsoProvider);
            final String withSsoBindingPath = replace(withSsoIdentity, "@auth.sso.binding.path", LoginResource.SSO_BINDING_PATH);
            return withSsoBindingPath.getBytes("UTF-8");
        } catch (final Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod()) || Method.POST.equals(request.getMethod())) {
            final IWebUiConfig webUiConfig = injector.getInstance(IWebUiConfig.class);

            if (StringUtils.isEmpty(webUiConfig.getDomainName()) || StringUtils.isEmpty(webUiConfig.getPath())) {
                throw new IllegalStateException("Both the domain name and the applicatin binding path should be provided.");
            }

            final ICompanionObjectFinder coFinder = injector.getInstance(ICompanionObjectFinder.class);
            final IUser coUser = coFinder.find(User.class, true);

            new LoginResource(
                    loginPage,
                    loginPageForMixedMode,
                    webUiConfig.getDomainName(),
                    webUiConfig.getPath(),
                    injector.getInstance(IUniversalConstants.class),
                    injector.getInstance(IAuthenticationModel.class),
                    injector.getInstance(IUserProvider.class),
                    coUser,
                    injector.getInstance(IUserSession.class),
                    util,
                    injector.getInstance(IDeviceProvider.class),
                    injector.getInstance(IDates.class),
                    getContext(),
                    request,
                    response
            ).handle();
        }
    }

}