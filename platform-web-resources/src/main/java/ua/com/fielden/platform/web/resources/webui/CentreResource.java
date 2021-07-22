package ua.com.fielden.platform.web.resources.webui;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.findLoadableConfig;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.inherited;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.inheritedFromBase;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.SAVED_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.commitCentreWithoutConflicts;
import static ua.com.fielden.platform.web.centre.CentreUpdater.loadableConfigurations;
import static ua.com.fielden.platform.web.centre.CentreUpdater.makePreferred;
import static ua.com.fielden.platform.web.centre.CentreUpdater.obtainTitleFrom;
import static ua.com.fielden.platform.web.centre.CentreUpdater.removeCentres;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentreDesc;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.updateInheritedFromShared;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.createCriteriaDiscardEnvelope;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.createStaleCriteriaMessage;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreModifiedPropertiesHolderFrom;

import java.util.Map;
import java.util.Optional;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancerCache;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.IMainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.centre.LoadableCentreConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for criteria serves as a back-end mechanism of centre management. It provides a base implementation for handling the following methods:
 * <ul>
 * <li>save centre -- POST request.
 * </ul>
 *
 * @author TG Team
 *
 */
public class CentreResource<CRITERIA_TYPE extends AbstractEntity<?>> extends AbstractWebResource {
    private final RestServerUtil restUtil;
    
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final Optional<String> saveAsName;
    
    private final IUserProvider userProvider;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    
    private final IDomainTreeEnhancerCache domainTreeEnhancerCache;
    private final IWebUiConfig webUiConfig;
    private final ICentreConfigSharingModel sharingModel;
    
    public CentreResource(
            final RestServerUtil restUtil,
            
            final EntityCentre<AbstractEntity<?>> centre,
            final Optional<String> saveAsName,
            
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final IDomainTreeEnhancerCache domainTreeEnhancerCache,
            final IWebUiConfig webUiConfig,
            final ICentreConfigSharingModel sharingModel,
            
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);
        
        this.restUtil = restUtil;
        
        miType = centre.getMenuItemType();
        this.saveAsName = saveAsName;
        this.userProvider = userProvider;
        this.companionFinder = companionFinder;
        this.critGenerator = critGenerator;
        this.domainTreeEnhancerCache = domainTreeEnhancerCache;
        this.webUiConfig = webUiConfig;
        this.sharingModel = sharingModel;
    }
    
    /**
     * Handles PUT request resulting from tg-entity-centre <code>discard()</code> method.
     *
     * Internally validation process is also performed.
     */
    @Put
    public Representation discard(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            final User user = userProvider.getUser();
            final EntityCentreConfigCo eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final IMainMenuItem mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);
            
            final Map<String, Object> wasRunHolder = restoreModifiedPropertiesHolderFrom(envelope, restUtil);
            final String wasRun = (String) wasRunHolder.get("@@wasRun");
            
            final ICentreDomainTreeManagerAndEnhancer newFreshCentre;
            
            final Optional<LoadableCentreConfig> loadableConfig = findLoadableConfig(saveAsName, () -> loadableConfigurations(user, miType, device(), companionFinder, sharingModel).apply(of(saveAsName)).stream()); // this will also throw early failure in case where current configuration was deleted
            final boolean isInherited = inherited(loadableConfig).isPresent();
            final Optional<String> actualSaveAsName;
            if (isInherited) {
                if (inheritedFromBase(loadableConfig).isPresent()) { // inherited from base
                    // remove cached instances of surrogate centres before updating from base user
                    removeCentres(user, miType, device(), saveAsName, eccCompanion, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME);
                    // it is necessary to use "fresh" instance of cdtme (after the discarding process)
                    newFreshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
                    updateCentre(user, miType, SAVED_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder); // do not leave only FRESH centre out of two (FRESH + SAVED) => update SAVED centre explicitly
                    // must leave current configuration preferred after deletion (only for named configs -- always true for inherited ones)
                    makePreferred(user, miType, saveAsName, device(), companionFinder, webUiConfig);
                    actualSaveAsName = saveAsName;
                } else { // inherited from shared
                    final Optional<EntityCentreConfig> upstreamConfig = updateInheritedFromShared(loadableConfig.get().getConfig().getConfigUuid(), miType, device(), saveAsName, user, eccCompanion, empty());
                    if (upstreamConfig.isPresent()) {
                        actualSaveAsName = of(obtainTitleFrom(upstreamConfig.get().getTitle(), SAVED_CENTRE_NAME, device()));
                        newFreshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, actualSaveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
                    } else {
                        actualSaveAsName = saveAsName;
                        newFreshCentre = discardOwnSaveAsConfig(user, eccCompanion, mmiCompanion, userCompanion, actualSaveAsName); // in some very unlikely (but possible) scenario original creator of shared config has deleted it since findLoadableConfig above invocation -- need to fallback to discarding as if the configuration is own save-as
                    }
                }
            } else {
                actualSaveAsName = saveAsName;
                newFreshCentre = discardOwnSaveAsConfig(user, eccCompanion, mmiCompanion, userCompanion, actualSaveAsName);
            }
            
            final String staleCriteriaMessage = createStaleCriteriaMessage(wasRun, newFreshCentre, miType, actualSaveAsName, user, companionFinder, critGenerator, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
            return createCriteriaDiscardEnvelope(newFreshCentre, miType, actualSaveAsName, user, restUtil, companionFinder, critGenerator, staleCriteriaMessage, device(), isInherited ? of(ofNullable(updateCentreDesc(user, miType, actualSaveAsName, device(), eccCompanion))) : empty(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
        }, restUtil);
    }
    
    /**
     * Discards configuration that represents own save-as configuration (possibly converted from inherited), default or link.
     */
    private ICentreDomainTreeManagerAndEnhancer discardOwnSaveAsConfig(final User user, final EntityCentreConfigCo eccCompanion, final IMainMenuItem mmiCompanion, final IUser userCompanion, final Optional<String> actualSaveAsName) {
        final ICentreDomainTreeManagerAndEnhancer updatedSavedCentre = updateCentre(user, miType, SAVED_CENTRE_NAME, actualSaveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
        // discards fresh centre's changes (fresh centre could have no changes)
        return commitCentreWithoutConflicts(user, miType, FRESH_CENTRE_NAME, actualSaveAsName, device(), updatedSavedCentre, null /* newDesc */, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
    }
    
}