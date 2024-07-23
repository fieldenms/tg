package ua.com.fielden.platform.web.utils;

import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.complementCriteriaEntityBeforeRunning;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaEntityForContext;

import com.google.inject.Inject;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
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
    private final IWebUiConfig webUiConfig;
    private final EntityFactory entityFactory;
    private final ICentreConfigSharingModel sharingModel;

    @Inject
    public CriteriaEntityRestorer(
            final ICompanionObjectFinder companionFinder,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final ICriteriaGenerator critGenerator,
            final IWebUiConfig webUiConfig,
            final EntityFactory entityFactory,
            final ICentreConfigSharingModel sharingModel) {
        this.companionFinder = companionFinder;
        this.userProvider = userProvider;
        this.deviceProvider = deviceProvider;
        this.critGenerator = critGenerator;
        this.webUiConfig = webUiConfig;
        this.entityFactory = entityFactory;
        this.sharingModel = sharingModel;
    }

    @Override
    public EnhancedCentreEntityQueryCriteria<?, ?> restoreCriteriaEntity(final CentreContextHolder centreContextHolder) {
        final User user = userProvider.getUser();
        final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
        final MainMenuItemCo mmiCompanion = companionFinder.find(MainMenuItem.class);
        final IUser userCompanion = companionFinder.find(User.class);
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ?> criteriaEntity = createCriteriaEntityForContext(centreContextHolder, companionFinder, user, critGenerator, webUiConfig, entityFactory, deviceProvider.getDeviceProfile(), eccCompanion, mmiCompanion, userCompanion, sharingModel);
        return complementCriteriaEntityBeforeRunning(criteriaEntity, webUiConfig, companionFinder, user, critGenerator, entityFactory, centreContextHolder, eccCompanion, mmiCompanion, userCompanion, sharingModel);
    }

}