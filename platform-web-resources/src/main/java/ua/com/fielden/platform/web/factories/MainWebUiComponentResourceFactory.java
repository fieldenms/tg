package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.MainWebUiComponentResource;

public class MainWebUiComponentResourceFactory extends Restlet {

    private final IWebUiConfig app;

    public MainWebUiComponentResourceFactory(final IWebUiConfig webApp) {
        this.app = webApp;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MainWebUiComponentResource(app, getContext(), request, response).handle();
        }
    }
}
