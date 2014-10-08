package ua.com.fielden.platform.web;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.resources.MainWebApplicationResource;

public class MainWebApplicationResourceFactory extends Restlet {

    private final WebApplication app;

    public MainWebApplicationResourceFactory(final WebApplication app) {
	this.app = app;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MainWebApplicationResource(app, getContext(), request, response).handle();
        }
    }
}
