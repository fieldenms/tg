package ua.com.fielden.platform.web.factories.webui;

import org.apache.commons.lang.StringUtils;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.LoginInitiateResetResource;

/**
 * A factory for a login web resource.
 *
 * @author TG Team
 *
 */
public class LoginInitiateResetResourceFactory extends Restlet {

    private final RestServerUtil util;
    private final Injector injector;

    public LoginInitiateResetResourceFactory(final RestServerUtil util, final Injector injector) {
        this.util = util;
        this.injector = injector;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod()) || Method.POST.equals(request.getMethod())) {

            new LoginInitiateResetResource(
                    injector.getInstance(IWebUiConfig.class),
                    injector.getInstance(IUniversalConstants.class),
                    injector.getInstance(IAuthenticationModel.class),
                    injector.getInstance(IUserProvider.class),
                    injector.getInstance(IUser.class),
                    injector.getInstance(IUserSession.class),
                    util,
                    getContext(),
                    request,
                    response
            ).handle();
        }
    }
}
