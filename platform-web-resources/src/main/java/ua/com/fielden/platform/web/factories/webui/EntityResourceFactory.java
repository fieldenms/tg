package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.EntityResource;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * A factory for entity resources which instantiate resources based on entity type.
 *
 * The entity type information is a part of the URI: "/entity/{entityType}/{entity-id}".
 *
 * @author TG Team
 *
 */
public class EntityResourceFactory extends Restlet {
    private final IWebUiConfig webUiConfig;
    private final RestServerUtil restUtil;
    private final EntityFactory factory;
    private final ICriteriaGenerator critGenerator;
    private final ICompanionObjectFinder coFinder;
    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;
    private final ICentreConfigSharingModel sharingModel;
    
    /**
     * Instantiates a factory for entity resources.
     *
     * @param webUiConfig
     *            -- configuration that contains the list of {@link EntityMaster}s from which fetch models and other information arrive
     * @param injector
     */
    public EntityResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webUiConfig = webUiConfig;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.factory = injector.getInstance(EntityFactory.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.coFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
        this.dates = injector.getInstance(IDates.class);
        this.sharingModel = injector.getInstance(ICentreConfigSharingModel.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST == request.getMethod() || Method.PUT == request.getMethod() || Method.DELETE == request.getMethod()) {
            final EntityMaster<? extends AbstractEntity<?>> master = ResourceFactoryUtils.getEntityMaster(request, webUiConfig);
            
            new EntityResource<>(
                    (Class<AbstractEntity<?>>) master.getEntityType(),
                    (IEntityProducer<AbstractEntity<?>>) master.createEntityProducer(),
                    factory,
                    restUtil,
                    critGenerator,
                    coFinder,
                    webUiConfig,
                    userProvider,
                    deviceProvider,
                    dates,
                    sharingModel,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
