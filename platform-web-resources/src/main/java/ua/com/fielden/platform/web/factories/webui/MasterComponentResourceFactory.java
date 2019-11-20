package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.MasterComponentResource;

/**
 * The server resource factory for entity masters.
 *
 * The master identification information is a part of the URI: "/master_ui/{entityType}".
 *
 * @author TG Team
 *
 */
public class MasterComponentResourceFactory extends Restlet {
    private final IWebResourceLoader webResourceLoader;
    private final RestServerUtil restUtil;
    private final IDeviceProvider deviceProvider;
    private final IUniversalConstants universalConstants;

    /**
     * Creates the {@link MasterComponentResourceFactory} instance.
     *
     * @param centres
     */
    public MasterComponentResourceFactory(final IWebResourceLoader webResourceLoader, final RestServerUtil restUtil, final IDeviceProvider deviceProvider, final IUniversalConstants universalConstants) {
        this.webResourceLoader = webResourceLoader;
        this.restUtil = restUtil;
        this.deviceProvider = deviceProvider;
        this.universalConstants = universalConstants;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MasterComponentResource(
                    webResourceLoader,
                    restUtil,
                    deviceProvider,
                    universalConstants,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
