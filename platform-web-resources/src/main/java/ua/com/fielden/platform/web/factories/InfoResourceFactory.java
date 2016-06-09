package ua.com.fielden.platform.web.factories;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

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
        if (Method.GET.equals(request.getMethod()) || Method.HEAD.equals(request.getMethod())) {
            new InfoResource(appInfo, getContext(), request, response).handle();
        }
    }

}
