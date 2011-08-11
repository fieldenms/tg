package ua.com.fielden.platform.web;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import ua.com.fielden.platform.web.resources.InfoResource;

/**
 * Info restlet.
 *
 * @author TG Team
 *
 */
public class InfoResourceFactory extends Restlet {
    private final String appInfo;

    public InfoResourceFactory(final String appInfo) {
	this.appInfo = appInfo;
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);
	if (Method.GET.equals(request.getMethod())) {
	    new InfoResource(appInfo, getContext(), request, response).handleGet();
	} else if (Method.HEAD.equals(request.getMethod())) {
	    new InfoResource(appInfo, getContext(), request, response).handleHead();
	}
    }

}
