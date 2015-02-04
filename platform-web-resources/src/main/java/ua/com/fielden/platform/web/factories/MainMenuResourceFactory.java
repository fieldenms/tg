package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.app.WebApp;
import ua.com.fielden.platform.web.resources.MainMenuResource;

public class MainMenuResourceFactory extends Restlet {

    private final WebApp app;

    public MainMenuResourceFactory(final WebApp webApp) {
        this.app = webApp;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MainMenuResource(app, getContext(), request, response).handle();
        }
    }
}
