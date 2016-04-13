package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;
import com.google.inject.Key;

import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.annotations.AppUri;
import ua.com.fielden.platform.web.resources.webui.LoginInitiateResetResource;

/**
 * A factory for a web resource that initiates the password recovery procedure.
 *
 * @author TG Team
 *
 */
public class LoginInitiateResetResourceFactory extends Restlet {

    private final Injector injector;

    public LoginInitiateResetResourceFactory(final Injector injector) {
        this.injector = injector;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod()) || Method.POST.equals(request.getMethod())) {

            new LoginInitiateResetResource(
                    injector.getInstance(Key.get(String.class, AppUri.class)),
                    injector.getInstance(IUniversalConstants.class),
                    injector.getInstance(IUser.class),
                    getContext(),
                    request,
                    response
            ).handle();
        }
    }
}
