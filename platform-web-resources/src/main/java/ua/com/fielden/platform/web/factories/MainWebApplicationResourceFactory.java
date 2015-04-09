package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.app.IWebApp;
import ua.com.fielden.platform.web.resources.MainWebApplicationResource;

public class MainWebApplicationResourceFactory extends Restlet {

    private final IWebApp app;

    public MainWebApplicationResourceFactory(final IWebApp webApp) {
        this.app = webApp;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MainWebApplicationResource(app, getContext(), request, response).handle();
        }
    }
}
