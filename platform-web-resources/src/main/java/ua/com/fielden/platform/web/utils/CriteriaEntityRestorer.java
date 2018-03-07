package ua.com.fielden.platform.web.utils;

import com.google.inject.Inject;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils;
import ua.com.fielden.platform.web.resources.webui.CentreResourceUtils;
import ua.com.fielden.platform.web.utils.ICriteriaEntityRestorer;

/**
 * An implementation of pojo-bl's interface that is dependent on Web UI infrastructure (centre configuration etc.).
 * 
 * @author TG Team
 *
 */
public class CriteriaEntityRestorer implements ICriteriaEntityRestorer {
    private final ICompanionObjectFinder companionFinder;
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;
    private final ICriteriaGenerator critGenerator;
    private final IWebUiConfig webUiConfig;
    private final EntityFactory entityFactory;
    
    @Inject
    public CriteriaEntityRestorer(final ICompanionObjectFinder companionFinder, final IServerGlobalDomainTreeManager serverGdtm, final IUserProvider userProvider, final ICriteriaGenerator critGenerator, final IWebUiConfig webUiConfig, final EntityFactory entityFactory) {
        this.companionFinder = companionFinder;
        this.serverGdtm = serverGdtm;
        this.userProvider = userProvider;
        this.critGenerator = critGenerator;
        this.webUiConfig = webUiConfig;
        this.entityFactory = entityFactory;
    }

    @Override
    public EnhancedCentreEntityQueryCriteria<?, ?> restoreCriteriaEntity(final CentreContextHolder centreContextHolder) {
        return CentreResourceUtils.createCriteriaEntityForContext(centreContextHolder, companionFinder, ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider), critGenerator, serverGdtm, userProvider, webUiConfig, entityFactory, userProvider.getDeviceProfile());
    }
}
