package ua.com.fielden.platform.web;

import java.io.IOException;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.resources.QueryPageResource;

import com.google.inject.Injector;

public class QueryPageResourceFactory extends Restlet {

    private final Injector injector;
    private final String username;

    public QueryPageResourceFactory(final Injector injector, final String username) {
        this.injector = injector;
        this.username = username;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            try {
                new QueryPageResource(injector, getContext(), request, response, username).handle();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
