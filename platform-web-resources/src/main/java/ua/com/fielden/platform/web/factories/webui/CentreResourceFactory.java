package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CentreResource;

import com.google.inject.Injector;

/**
 * A factory for centre resources which instantiate resources based on mi type.
 *
 * The centre identification information is a part of the URI: "/centre/{mitype}".
 *
 * @author TG Team
 *
 */
public class CentreResourceFactory extends Restlet {
    private final IWebUiConfig webUiConfig;
    private final Injector injector;
    private final RestServerUtil restUtil;

    /**
     * Instantiates a factory for centre resources.
     *
     */
    public CentreResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webUiConfig = webUiConfig;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST == request.getMethod() || Method.DELETE == request.getMethod()) {
            new CentreResource<AbstractEntity<?>>(
                    restUtil,
                    ResourceFactoryUtils.getEntityCentre(request, webUiConfig),
                    ResourceFactoryUtils.getUserSpecificGlobalManager(injector),
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
