package ua.com.fielden.platform.web.factories.webui;

import org.apache.commons.lang.StringUtils;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.LoginResource;

import com.google.inject.Injector;

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

            new LoginResource(
                    webUiConfig.getDomainName(),
                    webUiConfig.getPath(),
                    injector.getInstance(IUniversalConstants.class),
                    injector.getInstance(IAuthenticationModel.class),
                    injector.getInstance(IUserProvider.class),
                    injector.getInstance(IUserEx.class),
                    injector.getInstance(IUserSession.class),
                    util,
                    getContext(),
                    request,
                    response
            ).handle();
        }
    }
}
