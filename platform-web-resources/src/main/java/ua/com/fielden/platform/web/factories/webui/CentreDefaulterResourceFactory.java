package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CentreDefaulterResource;

/**
 * A factory for centre defaulting resources which instantiate resources based on mi type.
 *
 * The centre identification information is a part of the URI: "/centre/default/{mitype}".
 *
 * @author TG Team
 *
 */
public class CentreDefaulterResourceFactory extends Restlet {
    private final IWebUiConfig webUiConfig;
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    
    /**
     * Instantiates a factory for centre resources.
     *
     */
    public CentreDefaulterResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webUiConfig = webUiConfig;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.serverGdtm = injector.getInstance(IServerGlobalDomainTreeManager.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
    }
    
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        
        if (Method.PUT == request.getMethod()) {
            new CentreDefaulterResource<AbstractEntity<?>>(
                    restUtil,
                    ResourceFactoryUtils.getEntityCentre(request, webUiConfig),
                    serverGdtm,
                    userProvider,
                    deviceProvider,
                    companionFinder,
                    critGenerator,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
    
}