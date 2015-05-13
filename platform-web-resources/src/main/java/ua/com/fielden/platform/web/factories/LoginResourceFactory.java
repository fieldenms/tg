package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.user.IAuthenticationModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.LoginResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

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

        if (Method.GET.equals(request.getMethod()) || Method.PUT.equals(request.getMethod())) {
            new LoginResource(
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
