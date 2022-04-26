package ua.com.fielden.platform.web.factories.webui;

import static org.apache.commons.lang3.StringUtils.replace;

import org.apache.commons.lang.StringUtils;
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

    public LoginResourceFactory(final boolean trustedDeviceByDefault, final RestServerUtil util, final Injector injector) {
        this.util = util;
        this.injector = injector;
        // the same login page can be served for multiple requests
        // this is why it can be loaded and prepared just once
        final AuthMode authMode = injector.getInstance(IApplicationSettings.class).authMode();
        final String authSsoProvider = injector.getInstance(Key.get(String.class, Names.named("auth.sso.provider")));
        this.loginPage = loadLoginPage(trustedDeviceByDefault, authMode, authSsoProvider);
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
     * @return
     */
    private static byte[] loadLoginPage(final boolean trustedDeviceByDefault, final AuthMode authMode, String authSsoProvider) {
        try {
            final String loginPage = ResourceLoader.getText("ua/com/fielden/platform/web/login.html");
            final String withTitle = replace(loginPage, "@title", "Login");
            final String withTrusted = replace(withTitle, "@trusted", trustedDeviceByDefault ? "checked" : "");
            final String withAuthMode = replace(withTrusted, "@auth.mode", AuthMode.SSO == authMode ? "block" : "none");
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