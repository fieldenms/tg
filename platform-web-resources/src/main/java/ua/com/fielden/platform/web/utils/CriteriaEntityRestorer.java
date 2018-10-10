package ua.com.fielden.platform.web.utils;

import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaEntityForContext;

import com.google.inject.Inject;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * An implementation of pojo-bl's interface that is dependent on Web UI infrastructure (centre configuration etc.).
 * 
 * @author TG Team
 *
 */
public class CriteriaEntityRestorer implements ICriteriaEntityRestorer {
    private final ICompanionObjectFinder companionFinder;
    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    private final ICriteriaGenerator critGenerator;
    private final ISerialiser serialiser;
    private final IDomainTreeEnhancerCache domainTreeEnhancerCache;
    private final IWebUiConfig webUiConfig;
    private final EntityFactory entityFactory;
    
    @Inject
    public CriteriaEntityRestorer(
            final ICompanionObjectFinder companionFinder,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final ICriteriaGenerator critGenerator,
            final ISerialiser serialiser,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final EntityFactory entityFactory) {
        this.companionFinder = companionFinder;
        this.userProvider = userProvider;
        this.deviceProvider = deviceProvider;
        this.critGenerator = critGenerator;
        this.serialiser = serialiser;
        this.domainTreeEnhancerCache = domainTreeEnhancerCache;
        this.webUiConfig = webUiConfig;
        this.entityFactory = entityFactory;
    }

    @Override
    public EnhancedCentreEntityQueryCriteria<?, ?> restoreCriteriaEntity(final CentreContextHolder centreContextHolder) {
        final User user = userProvider.getUser();
        final IEntityCentreConfig eccCompanion = companionFinder.find(EntityCentreConfig.class);
        final IMainMenuItem mmiCompanion = companionFinder.find(MainMenuItem.class);
        final IUser userCompanion = companionFinder.find(User.class);
        
        return createCriteriaEntityForContext(centreContextHolder, companionFinder, user, critGenerator, userProvider, webUiConfig, entityFactory, deviceProvider.getDeviceProfile(), serialiser, domainTreeEnhancerCache, eccCompanion, mmiCompanion, userCompanion);
    }
}
