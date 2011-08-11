package ua.com.fielden.platform.web.resources;

import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.roa.HttpHeaders;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * A resource that handles all requests to previous versions of the application.
 * 
 * @author TG Team
 * 
 */
public class OldVersionResource extends Restlet {

    private final RestServerUtil util;
    private final String msg = "New application version is avilable.\nPlease restart the application to download the update.";

    public OldVersionResource(final RestServerUtil serverRestUtil) {
	this.util = serverRestUtil;
    }

    @Override
    public void handle(final Request request, final Response response) {
	super.handle(request, response);

	if (Method.HEAD.equals(request.getMethod()) || Method.DELETE.equals(request.getMethod())) {
	    util.setHeaderEntry(response, HttpHeaders.ERROR, msg);
	} else {
	    response.setEntity(util.resultRepresentation(new Result(null, new Exception(msg))));
	}
    }

}
