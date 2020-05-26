package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.MasterInfoProviderResource;
import ua.com.fielden.platform.web.view.master.MasterInfoProvider;

public class MasterInfoProviderResourceFactory extends Restlet {

    private final MasterInfoProvider masterInfoProvider;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;
    private final RestServerUtil restUtil;

    public MasterInfoProviderResourceFactory(final IWebUiConfig webApp, final IDeviceProvider deviceProvider, final IDates dates, final RestServerUtil restUtil) {
        this.masterInfoProvider = new MasterInfoProvider(webApp);
        this.deviceProvider = deviceProvider;
        this.dates = dates;
        this.restUtil = restUtil;
    }

    /**
     * Invokes on GET request from client.
     */
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MasterInfoProviderResource(
                    masterInfoProvider,
                    restUtil,
                    deviceProvider,
                    dates,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }

}
