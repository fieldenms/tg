package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.MasterTestsComponentResource;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * The server resource factory for entity masters testing (entityType is {@link TgPersistentEntityWithProperties}).
 *
 * @author TG Team
 *
 */
public class MasterTestsComponentResourceFactory extends Restlet {
    private final Injector injector;
    private final IDeviceProvider deviceProvider;

    /**
     * Creates the {@link MasterTestsComponentResourceFactory} instance.
     *
     * @param centres
     */
    public MasterTestsComponentResourceFactory(final Injector injector) {
        this.injector = injector;
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MasterTestsComponentResource(
                    new EntityMaster<TgPersistentEntityWithProperties>(
                            TgPersistentEntityWithProperties.class,
                            null,
                            injector),
                    deviceProvider,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}