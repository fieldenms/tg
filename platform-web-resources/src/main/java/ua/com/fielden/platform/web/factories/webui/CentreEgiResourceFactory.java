package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CentreEgiResource;

/**
 * The server resource factory for entity centre's EGI.
 *
 * The centre identification information is a part of the URI: "/centre_ui/egi/{mitype}".
 *
 * @author TG Team
 *
 */
public class CentreEgiResourceFactory extends Restlet {
    private final ISourceController sourceController;
    private final RestServerUtil restUtil;

    /**
     * Creates the {@link CentreEgiResourceFactory} instance.
     *
     */
    public CentreEgiResourceFactory(final ISourceController sourceController, final RestServerUtil restUtil) {
        this.sourceController = sourceController;
        this.restUtil = restUtil;
    }

    /**
     * Invokes on GET request from client.
     */
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new CentreEgiResource(
                    sourceController,
                    restUtil,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
