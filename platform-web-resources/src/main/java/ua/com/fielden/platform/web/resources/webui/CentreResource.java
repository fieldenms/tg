package ua.com.fielden.platform.web.resources.webui;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.*;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.*;
import static ua.com.fielden.platform.web.centre.CentreUpdater.*;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.updateInheritedFromShared;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.createCriteriaDiscardEnvelope;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.createCriteriaIndication;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreModifiedPropertiesHolderFrom;

/// The web resource for criteria serves as a back-end mechanism of centre management.
///
/// It provides a base implementation for handling the following methods:
///
/// * save centre -- POST request.
///
public class CentreResource<CRITERIA_TYPE extends AbstractEntity<?>> extends AbstractWebResource {
    private final RestServerUtil restUtil;
    
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final Optional<String> saveAsName;
    
    private final IUserProvider userProvider;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    
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
        this.webUiConfig = webUiConfig;
        this.sharingModel = sharingModel;
    }
    
    /// Handles PUT request resulting from tg-entity-centre <code>discard()</code> method.
    /// Internally validation process is also performed.
    ///
    @Put
    public Representation discard(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            final User user = userProvider.getUser();
            final Map<String, Object> wasRunHolder = restoreModifiedPropertiesHolderFrom(envelope, restUtil);
            final String wasRun = (String) wasRunHolder.get("@@wasRun");
            final var discarded = discardCentre(saveAsName, user, miType, webUiConfig, companionFinder, sharingModel);
            
            final var criteriaIndication = createCriteriaIndication(
                wasRun,
                discarded.freshCentre(),
                miType,
                discarded.saveAsName(),
                user,
                companionFinder,
                webUiConfig
            );
            return createCriteriaDiscardEnvelope(
                discarded.freshCentre(),
                miType,
                discarded.saveAsName(),
                user,
                restUtil,
                companionFinder,
                critGenerator,
                criteriaIndication,
                discarded.inherited() ? of(ofNullable(updateCentreDesc(user, miType, discarded.saveAsName(), companionFinder))) : empty(),
                webUiConfig,
                sharingModel
            );
        }, restUtil);
    }
    
    /// The outcome of discarding the changes of a configuration.
    ///
    /// @param freshCentre  the FRESH centre after discarding
    /// @param saveAsName  the name of the configuration after discarding, following a renamed upstream shared one
    /// @param inherited  whether the configuration is inherited from base or from shared
    ///
    record DiscardedCentre(ICentreDomainTreeManagerAndEnhancer freshCentre, Optional<String> saveAsName, boolean inherited) {}
    
    /// Discards the changes of the `saveAsName`d configuration of `user`.
    /// This is what the Discard action of an Entity Centre does.
    ///
    /// An own save-as, default or link configuration is reverted to its SAVED centre.
    /// A configuration inherited from base is recreated from the base configuration.
    /// It stays preferred on every device profile it was preferred on.
    /// A configuration inherited from shared is updated from the shared configuration.
    ///
    static DiscardedCentre discardCentre(
        final Optional<String> saveAsName,
        final User user,
        final Class<? extends MiWithConfigurationSupport<?>> miType,
        final IWebUiConfig webUiConfig,
        final ICompanionObjectFinder companionFinder,
        final ICentreConfigSharingModel sharingModel
    ) {
        final ICentreDomainTreeManagerAndEnhancer newFreshCentre;
        
        // `findLoadableConfig` will also throw early failure in case where current configuration was deleted.
        final var loadableConfig = findLoadableConfig(
            saveAsName,
            () -> loadableConfigurations(user, miType, companionFinder, sharingModel).apply(of(saveAsName)).stream()
        );
        final var isInherited = inherited(loadableConfig).isPresent();
        final Optional<String> actualSaveAsName;
        if (isInherited) {
            if (inheritedFromBase(loadableConfig).isPresent()) {
                // Inherited from base.
                // Deleting the FRESH centre below also deletes its preferred flags, so they are recorded first.
                final var preferredProfiles = preferredDeviceProfiles(user, miType, saveAsName, companionFinder);
                // Remove cached instances of surrogate centres before updating from base user.
                removeCentres(user, miType, saveAsName, companionFinder, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME);
                // It is necessary to use "fresh" instance of cdtme (after the discarding process).
                newFreshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, saveAsName, webUiConfig, companionFinder);
                // Do not leave only FRESH centre out of two (FRESH + SAVED) => update SAVED centre explicitly.
                updateCentre(user, miType, SAVED_CENTRE_NAME, saveAsName, webUiConfig, companionFinder);
                // Must leave current configuration preferred after deletion.
                // This applies only to named configs, which inherited ones always are.
                makePreferred(user, miType, saveAsName, companionFinder, webUiConfig);
                // It must also stay preferred on any other device profile it was preferred on.
                restorePreferred(user, miType, saveAsName, preferredProfiles, companionFinder, webUiConfig);
                actualSaveAsName = saveAsName;
            } else {
                // Inherited from shared.
                final var upstreamConfig = updateInheritedFromShared(
                    loadableConfig.get().getConfig() != null ? loadableConfig.get().getConfig().getConfigUuid() : null,
                    miType,
                    saveAsName,
                    user,
                    companionFinder,
                    empty()
                );
                if (upstreamConfig.isPresent()) {
                    actualSaveAsName = of(obtainTitleFrom(upstreamConfig.get().getTitle(), SAVED_CENTRE_NAME));
                    newFreshCentre = updateCentre(user, miType, FRESH_CENTRE_NAME, actualSaveAsName, webUiConfig, companionFinder);
                } else {
                    actualSaveAsName = saveAsName;
                    // Very unlikely, but the shared config may have been deleted since `findLoadableConfig` above.
                    // Need to fallback to discarding as if the configuration is own save-as.
                    newFreshCentre = discardOwnSaveAsConfig(actualSaveAsName, user, miType, webUiConfig, companionFinder);
                }
            }
        } else {
            actualSaveAsName = saveAsName;
            newFreshCentre = discardOwnSaveAsConfig(actualSaveAsName, user, miType, webUiConfig, companionFinder);
        }
        return new DiscardedCentre(newFreshCentre, actualSaveAsName, isInherited);
    }
    
    /// Discards configuration that represents own save-as configuration (possibly converted from inherited), default or link.
    ///
    private static ICentreDomainTreeManagerAndEnhancer discardOwnSaveAsConfig(
        final Optional<String> actualSaveAsName,
        final User user,
        final Class<? extends MiWithConfigurationSupport<?>> miType,
        final IWebUiConfig webUiConfig,
        final ICompanionObjectFinder companionFinder
    ) {
        final var updatedSavedCentre = updateCentre(
            user,
            miType,
            SAVED_CENTRE_NAME,
            actualSaveAsName,
            webUiConfig,
            companionFinder
        );
        // discards fresh centre's changes (fresh centre could have no changes)
        return commitCentreWithoutConflicts(
            user,
            miType,
            FRESH_CENTRE_NAME,
            actualSaveAsName,
            updatedSavedCentre,
            null /* newDesc */,
            webUiConfig,
            companionFinder
        );
    }
    
}
