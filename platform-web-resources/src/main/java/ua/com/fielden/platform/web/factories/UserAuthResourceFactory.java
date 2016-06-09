package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.UserAuthResource;

/**
 * A restlet responsible for handling user authentication requests.
 * 
 * @author TG Team
 * 
 */
public class UserAuthResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;

    public UserAuthResourceFactory(final Injector injector, final RestServerUtil restUtil) {
        this.injector = injector;
        this.restUtil = restUtil;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            handleGet(request, response);
        }
    }

    /**
     * Handles get request. Could be overridden to perform specific user-related tasks.
     * 
     * @param request
     * @param response
     */
    protected void handleGet(final Request request, final Response response) {
        final IUser controller = getController();
        new UserAuthResource(controller, restUtil, getContext(), request, response).handle();
    }

    protected IUser getController() {
        return injector.getInstance(IUser.class);
    }

    protected RestServerUtil getRestUtil() {
        return restUtil;
    }

    protected Injector getInjector() {
        return injector;
    }
}
