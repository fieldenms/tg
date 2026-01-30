package ua.com.fielden.platform.web.factories.webui;

import com.google.inject.Injector;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.menu.IWebAppConfigProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.ApplicationConfigurationResource;

public class ApplicationConfigurationResourceFactory extends Restlet {

    private final RestServerUtil restUtil;
    private final IWebAppConfigProvider webAppConfigProvider;
    private final IApplicationSettings appSettings;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;

    public ApplicationConfigurationResourceFactory(final IWebAppConfigProvider webAppConfigProvider, final Injector injector) {
        this.webAppConfigProvider = webAppConfigProvider;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.appSettings = injector.getInstance(IApplicationSettings.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
        this.dates = injector.getInstance(IDates.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod()) {
            new ApplicationConfigurationResource(
                    restUtil,
                    webAppConfigProvider,
                    appSettings,
                    deviceProvider,
                    dates,
                    getContext(),
                    request,
                    response)
                    .handle();
        }
    }
}
