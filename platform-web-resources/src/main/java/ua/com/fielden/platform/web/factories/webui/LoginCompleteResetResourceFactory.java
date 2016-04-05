package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.web.resources.webui.LoginCompleteResetResource;

/**
 * A factory for a web resource to complete the user password recovery procedure.
 *
 * @author TG Team
 *
 */
public class LoginCompleteResetResourceFactory extends Restlet {

    private final Injector injector;

    public LoginCompleteResetResourceFactory(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod()) || Method.POST.equals(request.getMethod())) {
            new LoginCompleteResetResource(
                    injector.getInstance(IUser.class),
                    getContext(),
                    request,
                    response
            ).handle();
        }
    }
}
