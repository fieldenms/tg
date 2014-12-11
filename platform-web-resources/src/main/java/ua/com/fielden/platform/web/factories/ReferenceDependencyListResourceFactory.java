package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.resources.ReferenceDependencyListResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * A factory for {@link ReferenceDependencyListResource}.
 *
 * @author TG Team
 *
 */
public class ReferenceDependencyListResourceFactory extends Restlet {
    private final String dependencyLocation;
    private final RestServerUtil restUtil;

    public ReferenceDependencyListResourceFactory(final String dependencyLocation, final Injector injector) {
        this.dependencyLocation = dependencyLocation;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        if (Method.GET == request.getMethod()) {
            final ReferenceDependencyListResource resource = new ReferenceDependencyListResource(dependencyLocation, restUtil, getContext(), request, response);
            resource.handle();
        }
    }
}
