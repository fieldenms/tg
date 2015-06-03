package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CentreComponentResource;

import com.google.inject.Injector;

/**
 * The server resource factory for entity centres.
 *
 * The centre identification information is a part of the URI: "/centre_ui/{mitype}".
 *
 * @author TG Team
 *
 */
public class CentreComponentResourceFactory extends Restlet {
    private final IWebUiConfig webUiConfig;
    private final RestServerUtil restUtil;

    /**
     * Creates the {@link CentreComponentResourceFactory} instance.
     *
     * @param centres
     */
    public CentreComponentResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
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
            new CentreComponentResource(
                    restUtil,
                    ResourceFactoryUtils.getEntityCentre(request, webUiConfig),
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
