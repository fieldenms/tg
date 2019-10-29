package ua.com.fielden.platform.web.factories.webui;

import java.util.Date;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.jackson.entities.FactoryForTestingEntities;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.SerialisationTestResource;

/**
 * Resource factory for integration test of Java and JavaScript serialisation.
 *
 * @author TG Team
 *
 */
public class SerialisationTestResourceFactory extends Restlet {
    private final RestServerUtil restUtil;
    private final FactoryForTestingEntities testingEntitiesFactory;
    private SerialisationTestResource cachedResource;
    private final IDeviceProvider deviceProvider;

    public SerialisationTestResourceFactory(final Injector injector) {
        this.restUtil = injector.getInstance(RestServerUtil.class);
        // this 'testingEntitiesFactory' should be the same across all resources! (two resources will be created during test lifecycle -- one for GET request and one for POST,
        // but they should use the same getEntities() not to create additional generated types for createGeneratedEntity() in FactoryForTestingEntities)
        this.testingEntitiesFactory = new FactoryForTestingEntities(injector.getInstance(EntityFactory.class), new Date());
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod()) {
            cachedResource = new SerialisationTestResource(restUtil, deviceProvider, getContext(), request, response, testingEntitiesFactory);
            cachedResource.handle();
        } else if (Method.POST == request.getMethod()) {
            new SerialisationTestResource(restUtil, deviceProvider, getContext(), request, response, testingEntitiesFactory, cachedResource.getEntities()).handle();
            cachedResource = null;
        }
    }
}
