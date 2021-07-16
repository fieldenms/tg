package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.EntityByKeyResource;

/**
 * A factory for entity by key resources which instantiate resources based on entity type.
 *
 * The entity type information is a part of the URI: "/entityid/{entityType}".
 *
 * @author TG Team
 *
 */
public class EntityByKeyResourceFactory extends Restlet {

    private final IDeviceProvider deviceProvider;
    private final IDates dates;
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder coFinder;

    /**
     * Instantiates a factory for {@link EntityByKeyResource} resources.
     *
     * @param webUiConfig
     * @param injector
     */
    public EntityByKeyResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
        this.dates = injector.getInstance(IDates.class);
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.coFinder = injector.getInstance(ICompanionObjectFinder.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST == request.getMethod()) {
            final String entityTypeName = (String) request.getAttributes().get("entityType");
            final Class<AbstractEntity<?>> entityType = (Class<AbstractEntity<?>>) ClassesRetriever.findClass(entityTypeName);
            new EntityByKeyResource(
                    entityType,
                    getContext(),
                    request,
                    response,
                    coFinder,
                    deviceProvider,
                    dates,
                    restUtil
            ).handle();
        }
    }
}
