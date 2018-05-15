package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.SAVED_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.commitCentreDesc;
import static ua.com.fielden.platform.web.centre.CentreUpdater.removeCentres;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentreDesc;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getUserSpecificGlobalManager;
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
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for centre defaulting aka making user-specific configuration synchronised with its base configuration or base configuration synchronised with Centre DSL configured one.
 *
 * @author TG Team
 *
 */
public class CentreDefaulterResource<CRITERIA_TYPE extends AbstractEntity<?>> extends AbstractWebResource {
    private final RestServerUtil restUtil;
    
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final Optional<String> saveAsName;
    
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    
    public CentreDefaulterResource(
            final RestServerUtil restUtil,
            
            final EntityCentre centre,
            final Optional<String> saveAsName,
            
            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider);
        
        this.restUtil = restUtil;
        
        miType = centre.getMenuItemType();
        this.saveAsName = saveAsName;
        this.serverGdtm = serverGdtm;
        this.userProvider = userProvider;
        this.companionFinder = companionFinder;
        this.critGenerator = critGenerator;
    }
    
    /**
     * Handles PUT request resulting from tg-entity-centre <code>_default()</code> method.
     */
    @Put
    public Representation makeDefault(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            final IGlobalDomainTreeManager gdtm = getUserSpecificGlobalManager(serverGdtm, userProvider);
            final Map<String, Object> wasRunHolder = restoreModifiedPropertiesHolderFrom(envelope, restUtil);
            final String wasRun = (String) wasRunHolder.get("@@wasRun");
            
            final String customDesc = updateCentreDesc(gdtm, miType, saveAsName, device());
            
            removeCentres(gdtm, miType, device(), saveAsName, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME);
            
            // it is necessary to use "fresh" instance of cdtme (after the defaulting process)
            final ICentreDomainTreeManagerAndEnhancer newFreshCentre = updateCentre(gdtm, miType, FRESH_CENTRE_NAME, saveAsName, device());
            
            // commit custom description after user-specific copy was deleted and default centre copy made 
            commitCentreDesc(gdtm, miType, saveAsName, device(), customDesc);
            
            final String staleCriteriaMessage = createStaleCriteriaMessage(wasRun, newFreshCentre, miType, saveAsName, gdtm, companionFinder, critGenerator, device());
            return createCriteriaDiscardEnvelope(newFreshCentre, miType, saveAsName, gdtm, restUtil, companionFinder, critGenerator, staleCriteriaMessage, device());
        }, restUtil);
    }
    
}