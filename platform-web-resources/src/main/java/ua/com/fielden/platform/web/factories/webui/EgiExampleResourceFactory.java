package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.EgiExampleResource;

/**
 * Resource factory for EGI example.
 *
 * @author TG Team
 *
 */
public class EgiExampleResourceFactory extends Restlet {
    private final EntityFactory factory;
    private final RestServerUtil restUtil;
    private final IDeviceProvider deviceProvider;

    public EgiExampleResourceFactory(final Injector injector) {
        this.factory = injector.getInstance(EntityFactory.class);
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod()) {
            final EgiExampleResource resource = new EgiExampleResource(factory, restUtil, deviceProvider, getContext(), request, response);
            resource.handle();
        }
    }
}
