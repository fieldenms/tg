package ua.com.fielden.platform.web.resources.webui;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.web.centre.CentreConfigUtils.isInherited;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.SAVED_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.initAndCommit;
import static ua.com.fielden.platform.web.centre.CentreUpdater.loadableConfigurations;
import static ua.com.fielden.platform.web.centre.CentreUpdater.makePreferred;
import static ua.com.fielden.platform.web.centre.CentreUpdater.removeCentres;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentreDesc;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.createCriteriaDiscardEnvelope;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.createStaleCriteriaMessage;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreModifiedPropertiesHolderFrom;

import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.api.IEntityCentreConfig;
import ua.com.fielden.platform.ui.config.api.IMainMenuItem;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
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
    private static final Logger LOGGER = Logger.getLogger(CentreResource.class);
    
    private final RestServerUtil restUtil;
    
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final Optional<String> saveAsName;
    
    private final IUserProvider userProvider;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    
    private final ISerialiser serialiser;
    private final IWebUiConfig webUiConfig;
    
    public CentreResource(
            final RestServerUtil restUtil,
            
            final EntityCentre centre,
            final Optional<String> saveAsName,
            
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final ISerialiser serialiser,
            final IWebUiConfig webUiConfig,
            
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider);
        
        this.restUtil = restUtil;
        
        miType = centre.getMenuItemType();
        this.saveAsName = saveAsName;
        this.userProvider = userProvider;
        this.companionFinder = companionFinder;
        this.critGenerator = critGenerator;
        this.serialiser = serialiser;
        this.webUiConfig = webUiConfig;
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
            
            final boolean isInherited = isInherited(saveAsName, () -> loadableConfigurations(user, miType, device(), companionFinder).stream()); // this will also throw early failure in case where current configuration was deleted
            if (isInherited) {
                // remove cached instances of surrogate centres before updating from base user
                removeCentres(user, miType, device(), saveAsName, eccCompanion, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME);
                // it is necessary to use "fresh" instance of cdtme (after the discarding process)
                newFreshCentre = updateCentre(user, userProvider, miType, FRESH_CENTRE_NAME, saveAsName, device(), serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
                // must leave current configuration preferred after deletion
                makePreferred(user, miType, saveAsName, device(), companionFinder);
            } else {
                final ICentreDomainTreeManagerAndEnhancer updatedSavedCentre = updateCentre(user, userProvider, miType, SAVED_CENTRE_NAME, saveAsName, device(), serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
                // discards fresh centre's changes (fresh centre could have no changes)
                newFreshCentre = initAndCommit(user, userProvider, miType, FRESH_CENTRE_NAME, saveAsName, device(), updatedSavedCentre, null, serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
            }
            
            final String staleCriteriaMessage = createStaleCriteriaMessage(wasRun, newFreshCentre, miType, saveAsName, user, userProvider, companionFinder, critGenerator, device(), serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
            return createCriteriaDiscardEnvelope(newFreshCentre, miType, saveAsName, user, userProvider, restUtil, companionFinder, critGenerator, staleCriteriaMessage, device(), isInherited ? ofNullable(updateCentreDesc(user, miType, saveAsName, device(), eccCompanion)) : empty(), serialiser, webUiConfig, eccCompanion, mmiCompanion, userCompanion);
        }, restUtil);
    }
    
}