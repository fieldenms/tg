package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.EntityValidationResource;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/**
 * A factory for entity validation resources which instantiate resources based on entity type.
 *
 * The entity type information is a part of the URI: "/validation/{entityType}".
 *
 * @author TG Team
 *
 */
public class EntityValidationResourceFactory extends Restlet {
    private final IWebUiConfig webUiConfig;
    private final RestServerUtil restUtil;
    private final EntityFactory factory;
    private final ICompanionObjectFinder coFinder;
    private final ICriteriaGenerator critGenerator;
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;

    /**
     * Instantiates a factory for entity validation resources.
     *
     * @param webUiConfig
     *            -- configuration that contains the list of {@link EntityMaster}s from which fetch models and other information arrive
     * @param injector
     */
    public EntityValidationResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webUiConfig = webUiConfig;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.factory = injector.getInstance(EntityFactory.class);
        this.coFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.serverGdtm = injector.getInstance(IServerGlobalDomainTreeManager.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST == request.getMethod()) {
            final EntityMaster<? extends AbstractEntity<?>> master = ResourceFactoryUtils.getEntityMaster(request, webUiConfig);

            final EntityValidationResource<AbstractEntity<?>> resource = new EntityValidationResource<>(
                    (Class<AbstractEntity<?>>) master.getEntityType(),
                    factory,
                    restUtil,
                    critGenerator,
                    coFinder,
                    webUiConfig,
                    serverGdtm,
                    userProvider,
                    getContext(),
                    request,
                    response //
            );
            resource.handle();
        }
    }
}
