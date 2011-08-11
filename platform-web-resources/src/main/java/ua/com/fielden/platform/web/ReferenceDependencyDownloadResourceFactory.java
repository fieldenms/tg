package ua.com.fielden.platform.web;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.web.resources.ReferenceDependencyDownloadResource;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.inject.Injector;

/**
 * A factory for {@link ReferenceDependencyDownloadResource}.
 * 
 * @author TG Team
 * 
 */
public class ReferenceDependencyDownloadResourceFactory extends Restlet {
    private final String dependencyLocation;
    private final RestServerUtil restUtil;

    public ReferenceDependencyDownloadResourceFactory(final String dependencyLocation, final Injector injector) {
	this.dependencyLocation = dependencyLocation;
	this.restUtil = new RestServerUtil(injector.getInstance(ISerialiser.class));
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);
	if (Method.GET == request.getMethod()) {
	    final ReferenceDependencyDownloadResource resource = new ReferenceDependencyDownloadResource(dependencyLocation, restUtil, getContext(), request, response);
	    resource.handleGet();
	}
    }
}
