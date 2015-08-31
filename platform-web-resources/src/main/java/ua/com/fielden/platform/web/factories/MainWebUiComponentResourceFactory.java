package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.app.IPreloadedResources;
import ua.com.fielden.platform.web.resources.MainWebUiComponentResource;

public class MainWebUiComponentResourceFactory extends Restlet {
    private final IPreloadedResources preloadedResources;

    public MainWebUiComponentResourceFactory(final Injector injector) {
        this.preloadedResources = injector.getInstance(IPreloadedResources.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MainWebUiComponentResource(preloadedResources, getContext(), request, response).handle();
        }
    }
}
