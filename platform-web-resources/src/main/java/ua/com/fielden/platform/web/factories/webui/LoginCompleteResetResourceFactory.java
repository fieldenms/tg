package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web.resources.webui.LoginCompleteResetResource;

/**
 * A factory for a web resource to complete the user password recovery procedure.
 *
 * @author TG Team
 *
 */
public class LoginCompleteResetResourceFactory extends Restlet {

    private final Injector injector;
    private final String demoSecret;

    public LoginCompleteResetResourceFactory(final Injector injector, final String demoSecret) {
        this.injector = injector;
        this.demoSecret = demoSecret;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod()) || Method.POST.equals(request.getMethod())) {
            new LoginCompleteResetResource(
                    demoSecret,
                    injector.getInstance(ICompanionObjectFinder.class),
                    injector.getInstance(IUserProvider.class),
                    getContext(),
                    request,
                    response
            ).handle();
        }
    }
}
