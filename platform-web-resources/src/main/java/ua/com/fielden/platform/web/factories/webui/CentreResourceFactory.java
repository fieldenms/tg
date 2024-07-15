package ua.com.fielden.platform.web.factories.webui;

import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.extractSaveAsName;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityCentre;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
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
    private final IWebUiConfig webUiConfig;
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;
    private final ICentreConfigSharingModel sharingModel;
    
    /**
     * Instantiates a factory for centre resources.
     *
     */
    public CentreResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webUiConfig = webUiConfig;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.userProvider = injector.getInstance(IUserProvider.class);
        this.deviceProvider = injector.getInstance(IDeviceProvider.class);
        this.dates = injector.getInstance(IDates.class);
        this.sharingModel = injector.getInstance(ICentreConfigSharingModel.class);
    }
    
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        
        if (Method.PUT == request.getMethod()) {
            new CentreResource<>(
                    restUtil,
                    getEntityCentre(request, webUiConfig),
                    extractSaveAsName(request),
                    userProvider,
                    deviceProvider,
                    dates,
                    companionFinder,
                    critGenerator,
                    webUiConfig,
                    sharingModel,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
    
}
