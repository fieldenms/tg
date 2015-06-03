package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CriteriaResource;

import com.google.inject.Injector;

/**
 * A factory for criteria resources which instantiate resources based on entity type.
 *
 * The entity type information is a part of the URI: "/criteria/{mitype}".
 *
 * @author TG Team
 *
 */
public class CriteriaResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;
    private final IWebUiConfig webUiConfig;
    private final ICriteriaGenerator critGenerator;

    /**
     * Instantiates a factory for criteria entity resource.
     *
     */
    public CriteriaResourceFactory(final IWebUiConfig webUiConfig, final Injector injector) {
        this.webUiConfig = webUiConfig;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod() || Method.PUT == request.getMethod() || Method.POST == request.getMethod()) {
            new CriteriaResource<AbstractEntity<?>, EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>>>(
                    restUtil,
                    companionFinder,
                    (EntityCentre<AbstractEntity<?>>) ResourceFactoryUtils.getEntityCentre(request, webUiConfig),
                    ResourceFactoryUtils.getUserSpecificGlobalManager(injector),
                    critGenerator,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
