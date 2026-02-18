package ua.com.fielden.platform.web.factories.webui;

import com.google.inject.Injector;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.ApplicationConfigurationResource;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.restlet.data.Status.CLIENT_ERROR_METHOD_NOT_ALLOWED;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.web.resources.RestServerUtil.encodedRepresentation;

/// Resource for web application configuration.
///
public class ApplicationConfigurationResourceFactory extends Restlet {

    private static final String ERR_UNSUPPORTED_HTTP_METHOD = "Method not supported. Refer header Access-Control-Allow-Methods for more detail.";

    private final RestServerUtil restUtil;
    private final IWebUiConfig webUiConfig;
    private final IApplicationSettings appSettings;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;

    public ApplicationConfigurationResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webUiConfig = webUiConfig;
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
                    webUiConfig,
                    appSettings,
                    deviceProvider,
                    dates,
                    getContext(),
                    request,
                    response)
                    .handle();
        }
        else {
            response.setStatus(CLIENT_ERROR_METHOD_NOT_ALLOWED);
            response.setAccessControlAllowMethods(setOf(Method.GET));
            response.setEntity(encodedRepresentation(new ByteArrayInputStream(ERR_UNSUPPORTED_HTTP_METHOD.getBytes(UTF_8)), MediaType.TEXT_PLAIN));
        }
    }

}
