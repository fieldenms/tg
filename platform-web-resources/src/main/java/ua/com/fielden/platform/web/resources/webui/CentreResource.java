package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnlyAndInstrument;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.findLoadableConfig;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.inherited;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.inheritedFromShared;
import static ua.com.fielden.platform.web.centre.CentreDiffSerialiser.CENTRE_DIFF_SERIALISER;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.SAVED_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.commitCentreWithoutConflicts;
import static ua.com.fielden.platform.web.centre.CentreUpdater.loadableConfigurations;
import static ua.com.fielden.platform.web.centre.CentreUpdater.makePreferred;
import static ua.com.fielden.platform.web.centre.CentreUpdater.nameOf;
import static ua.com.fielden.platform.web.centre.CentreUpdater.removeCentres;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentreDesc;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfig;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.findConfigOptByUuid;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.restoreDiffFrom;
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
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreUpdater;
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
    private final EntityCentre<AbstractEntity<?>> centre;
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
        this.centre = centre;
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
            final IEntityCentreConfig eccCompanion = companionFinder.find(EntityCentreConfig.class);
            final IMainMenuItem mmiCompanion = companionFinder.find(MainMenuItem.class);
            final IUser userCompanion = companionFinder.find(User.class);
            
            final Map<String, Object> wasRunHolder = restoreModifiedPropertiesHolderFrom(envelope, restUtil);
            final String wasRun = (String) wasRunHolder.get("@@wasRun");
            
            final ICentreDomainTreeManagerAndEnhancer newFreshCentre;
            
            final Optional<LoadableCentreConfig> loadableConfig = findLoadableConfig(saveAsName, () -> loadableConfigurations(user, miType, device(), companionFinder, sharingModel).apply(of(saveAsName)).stream()); // this will also throw early failure in case where current configuration was deleted
            final boolean isInherited = inherited(loadableConfig).isPresent();
            if (isInherited) {
                if (inheritedFromShared(loadableConfig).isPresent()) { // inherited from shared
                    final Optional<EntityCentreConfig> savedConfigOpt = findConfigOptByUuid(eccCompanion.withDbVersion(CentreUpdater.centreConfigQueryFor(miType, device(), SAVED_CENTRE_NAME)), loadableConfig.get().getConfig().getConfigUuid(), eccCompanion);
                    final EntityCentreConfig savedConfig = savedConfigOpt.get(); // it is present otherwise it should not be inherited
                    final Map<String, Object> differences = restoreDiffFrom(savedConfig, eccCompanion, format("for type [%s] with name [%s]", miType.getSimpleName(), savedConfig.getTitle()));
                    final EntityCentreConfig savedConfigForCurrUser = findConfig(miType, user, nameOf.apply(SAVED_CENTRE_NAME).apply(saveAsName).apply(device()), eccCompanion, fetchKeyAndDescOnlyAndInstrument(EntityCentreConfig.class).with("configBody"));
                    savedConfigForCurrUser.setConfigBody(CENTRE_DIFF_SERIALISER.serialise(differences));
                    eccCompanion.quickSave(savedConfigForCurrUser);
                    final EntityCentreConfig freshConfigForCurrUser = findConfig(miType, user, nameOf.apply(FRESH_CENTRE_NAME).apply(saveAsName).apply(device()), eccCompanion, fetchKeyAndDescOnlyAndInstrument(EntityCentreConfig.class).with("configBody"));
                    freshConfigForCurrUser.setConfigBody(CENTRE_DIFF_SERIALISER.serialise(differences));
                    eccCompanion.quickSave(freshConfigForCurrUser);
                    newFreshCentre = updateCentre(user, userProvider, miType, FRESH_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
                } else { // inherited from base
                    // remove cached instances of surrogate centres before updating from base user
                    removeCentres(user, miType, device(), saveAsName, eccCompanion, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME);
                    // it is necessary to use "fresh" instance of cdtme (after the discarding process)
                    newFreshCentre = updateCentre(user, userProvider, miType, FRESH_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
                    updateCentre(user, userProvider, miType, SAVED_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder); // do not leave only FRESH centre out of two (FRESH + SAVED) => update SAVED centre explicitly
                    // must leave current configuration preferred after deletion (only for named configs -- always true for inherited ones, and for non autoRun centres)
                    if (!centre.isRunAutomatically()) {
                        makePreferred(user, miType, saveAsName, device(), companionFinder);
                    }
                }
            } else {
                final ICentreDomainTreeManagerAndEnhancer updatedSavedCentre = updateCentre(user, userProvider, miType, SAVED_CENTRE_NAME, saveAsName, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
                // discards fresh centre's changes (fresh centre could have no changes)
                newFreshCentre = commitCentreWithoutConflicts(user, userProvider, miType, FRESH_CENTRE_NAME, saveAsName, device(), updatedSavedCentre, null /* newDesc */, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
            }
            
            final String staleCriteriaMessage = createStaleCriteriaMessage(wasRun, newFreshCentre, miType, saveAsName, user, userProvider, companionFinder, critGenerator, device(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
            return createCriteriaDiscardEnvelope(newFreshCentre, miType, saveAsName, user, userProvider, restUtil, companionFinder, critGenerator, staleCriteriaMessage, device(), isInherited ? of(ofNullable(updateCentreDesc(user, miType, saveAsName, device(), eccCompanion))) : empty(), domainTreeEnhancerCache, webUiConfig, eccCompanion, mmiCompanion, userCompanion, sharingModel);
        }, restUtil);
    }
    
}