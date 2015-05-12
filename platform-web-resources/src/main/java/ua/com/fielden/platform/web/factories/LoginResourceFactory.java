package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.security.provider.IUserEx;
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

        // TODO username can only come from an authentication cookie
        //final String username = (String) request.getAttributes().get("username");
        //injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserEx.class));


        if (Method.GET.equals(request.getMethod()) || Method.PUT.equals(request.getMethod())) {
            new LoginResource(util, getContext(), request, response).handle();
        }
    }
}
