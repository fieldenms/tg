package ua.com.fielden.platform.web.factories.webui;

import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityCentre;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.saveAsName;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CentreResource;

/**
 * A factory for centre resources which instantiate resources based on mi type.
 *
 * The centre identification information is a part of the URI: "/centre/{mitype}".
 *
 * @author TG Team
 *
 */
public class CentreResourceFactory extends Restlet {
    private final IDomainTreeEnhancerCache domainTreeEnhancerCache;
    private final IWebUiConfig webUiConfig;
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;
    
    /**
     * Instantiates a factory for centre resources.
     *
     */
    public CentreResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.domainTreeEnhancerCache = injector.getInstance(IDomainTreeEnhancerCache.class);
        this.webUiConfig = webUiConfig;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
        this.dates = injector.getInstance(IDates.class);
    }
    
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        
        if (Method.PUT == request.getMethod()) {
            new CentreResource<>(
                    restUtil,
                    getEntityCentre(request, webUiConfig),
                    saveAsName(request),
                    userProvider,
                    deviceProvider,
                    dates,
                    companionFinder,
                    critGenerator,
                    domainTreeEnhancerCache,
                    webUiConfig,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
    
}
