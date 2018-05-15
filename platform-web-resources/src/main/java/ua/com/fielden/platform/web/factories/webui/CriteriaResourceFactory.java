package ua.com.fielden.platform.web.factories.webui;

import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityCentre;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.saveAsName;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CriteriaResource;

/**
 * A factory for criteria resources which instantiate resources based on entity type.
 *
 * The entity type information is a part of the URI: "/criteria/{mitype}".
 *
 * @author TG Team
 *
 */
public class CriteriaResourceFactory extends Restlet {
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;
    private final IWebUiConfig webUiConfig;
    private final ICriteriaGenerator critGenerator;

    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    private final EntityFactory entityFactory;

    /**
     * Instantiates a factory for criteria entity resource.
     *
     */
    public CriteriaResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webUiConfig = webUiConfig;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.serverGdtm = injector.getInstance(IServerGlobalDomainTreeManager.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
        this.entityFactory = injector.getInstance(EntityFactory.class);
    }
    
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod() || Method.PUT == request.getMethod() || Method.POST == request.getMethod()) {
            new CriteriaResource(
                    restUtil,
                    getEntityCentre(request, webUiConfig),
                    saveAsName(request),
                    webUiConfig,
                    companionFinder,
                    serverGdtm,
                    userProvider,
                    deviceProvider,
                    critGenerator,
                    entityFactory,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
