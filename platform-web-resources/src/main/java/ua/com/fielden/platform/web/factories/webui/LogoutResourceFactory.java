package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.security.session.IUserSession;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.resources.webui.LogoutResource;

/**
 * A factory for a logout web resource.
 *
 * @author TG Team
 *
 */
public class LogoutResourceFactory extends Restlet {

    private final Injector injector;

    public LogoutResourceFactory(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new LogoutResource(
                    injector.getInstance(IUserProvider.class),
                    injector.getInstance(IUser.class),
                    injector.getInstance(IUserSession.class),
                    getContext(),
                    request,
                    response
            ).handle();
        }
    }
}
