package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.action.CentreConfigShareActionProducer.validateShareActionContext;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaEntityForContext;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreCentreContextHolder;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreConfigShareAction;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Separate resource for validating currently loaded Entity Centre configuration on its ability to be shared.
 * The need for this resource arises because writing to clipboard on Safari browsers is only possible inside user action callbacks.
 * We use synchronous XMLHttpRequest to get response from this resource (promises or async/await constructs result in errors on Safari).
 *
 * @author TG Team
 *
 */
public class ShareActionValidationResource<T extends AbstractEntity<?>> extends AbstractWebResource {
    private final EntityFactory entityFactory;
    private final RestServerUtil restUtil;
    private final ICriteriaGenerator critGenerator;
    private final ICompanionObjectFinder companionFinder;
    private final IDomainTreeEnhancerCache domainTreeEnhancerCache;
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final ICentreConfigSharingModel sharingModel;

    public ShareActionValidationResource(
            final EntityFactory entityFactory,
            final RestServerUtil restUtil,
            final ICriteriaGenerator critGenerator,
            final ICompanionObjectFinder companionFinder,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final ICentreConfigSharingModel sharingModel,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);

        this.entityFactory = entityFactory;
        this.restUtil = restUtil;
        this.critGenerator = critGenerator;
        this.companionFinder = companionFinder;
        this.domainTreeEnhancerCache = domainTreeEnhancerCache;
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
        this.sharingModel = sharingModel;
    }

    /**
     * Handles POST request resulting from tg-ui-action <code>_run()</code> method for {@link CentreConfigShareAction}.
     */
    @Post
    public Representation validate(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            final CentreContextHolder centreContextHolder = restoreCentreContextHolder(envelope, restUtil);
            final User user = userProvider.getUser();
            final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final MainMenuItemCo mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);

            return restUtil.resultJSONRepresentation(validateShareActionContext(
                createCriteriaEntityForContext(centreContextHolder, companionFinder, user, critGenerator, webUiConfig, entityFactory, device(), domainTreeEnhancerCache, eccCompanion, mmiCompanion, userCompanion, sharingModel),
                eccCompanion,
                userProvider
            ));
        }, restUtil);
    }

}