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
    private final Injector injector;
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;

    /**
     * Instantiates a factory for centre resources.
     *
     */
    public CentreResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webUiConfig = webUiConfig;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
        this.serverGdtm = injector.getInstance(IServerGlobalDomainTreeManager.class);
        this.userProvider = injector.getInstance(IUserProvider.class);;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST == request.getMethod() || Method.PUT == request.getMethod()) {
            new CentreResource<AbstractEntity<?>>(
                    restUtil,
                    ResourceFactoryUtils.getEntityCentre(request, webUiConfig),
                    serverGdtm,
                    userProvider,
                    companionFinder,
                    critGenerator,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
