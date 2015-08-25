package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.egi.WebEntityGridInspector;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CentreEgiResource;

import com.google.inject.Injector;

/**
 * The server resource factory for entity centre's EGI.
 *
 * The centre identification information is a part of the URI: "/centre_ui/egi/{mitype}".
 *
 * @author TG Team
 *
 */
public class CentreEgiResourceFactory extends Restlet {
    private final IWebUiConfig webUiConfig;
    private final RestServerUtil restUtil;

    /**
     * Creates the {@link CentreEgiResourceFactory} instance.
     *
     * @param centres
     */
    public CentreEgiResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webUiConfig = webUiConfig;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    /**
     * Invokes on GET request from client.
     */
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new CentreEgiResource(
                    restUtil,
                    new WebEntityGridInspector(ResourceFactoryUtils.getEntityCentre(request, webUiConfig)),
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
