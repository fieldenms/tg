package ua.com.fielden.platform.web.factories.webui;

import org.apache.commons.lang.StringUtils;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;
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

    public LoginResourceFactory(final RestServerUtil util, final Injector injector) {
        this.util = util;
        this.injector = injector;
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
