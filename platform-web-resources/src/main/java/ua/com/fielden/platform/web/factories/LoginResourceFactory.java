package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.LoginResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

public class LoginResourceFactory extends Restlet {

    private final IWebUiConfig app;
    private final RestServerUtil util;

    public LoginResourceFactory(final IWebUiConfig webApp, final RestServerUtil util) {
        this.app = webApp;
        this.util = util;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod()) || Method.PUT.equals(request.getMethod())) {
            new LoginResource(util, getContext(), request, response).handle();
        }
    }
}
