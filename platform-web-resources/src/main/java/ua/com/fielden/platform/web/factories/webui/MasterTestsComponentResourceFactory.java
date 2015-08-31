package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.MasterComponentResource;

/**
 * The server resource factory for entity masters testing (entityType is {@link TgPersistentEntityWithProperties}).
 *
 * @author TG Team
 *
 */
public class MasterTestsComponentResourceFactory extends Restlet {
    private final ISourceController sourceController;
    private final RestServerUtil restUtil;

    /**
     * Creates the {@link MasterTestsComponentResourceFactory} instance.
     *
     * @param centres
     */
    public MasterTestsComponentResourceFactory(final ISourceController sourceController, final Injector injector) {
        this.sourceController = sourceController;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MasterComponentResource(
                    restUtil,
                    sourceController,

                    // TODO needs to be modified to get specific master for tests!
                    // TODO needs to be modified to get specific master for tests!
                    // TODO needs to be modified to get specific master for tests!
                    // TODO needs to be modified to get specific master for tests!
                    // TODO needs to be modified to get specific master for tests!
                    // TODO needs to be modified to get specific master for tests!
                    // TODO needs to be modified to get specific master for tests!
                    // TODO needs to be modified to get specific master for tests!
//                    new EntityMaster<TgPersistentEntityWithProperties>(
//                            TgPersistentEntityWithProperties.class,
//                            null,
//                            injector),
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
