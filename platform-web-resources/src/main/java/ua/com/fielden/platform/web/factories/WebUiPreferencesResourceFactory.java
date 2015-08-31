package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.resources.WebUiPreferencesResource;

public class WebUiPreferencesResourceFactory extends Restlet {
    private final ISourceController sourceController;

    public WebUiPreferencesResourceFactory(final Injector injector) {
        this.sourceController = injector.getInstance(ISourceController.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new WebUiPreferencesResource(sourceController, getContext(), request, response).handle();
        }
    }
}
