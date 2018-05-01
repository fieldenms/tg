package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.SAVED_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.deviceSpecific;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreModifiedPropertiesHolderFrom;

import java.util.Map;

import org.apache.log4j.Logger;
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
import ua.com.fielden.platform.web.centre.CentreUpdater;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for centre defaulting.
 *
 * @author TG Team
 *
 */
public class CentreDefaulterResource<CRITERIA_TYPE extends AbstractEntity<?>> extends AbstractWebResource {
    private final static Logger logger = Logger.getLogger(CentreDefaulterResource.class);
    
    private final RestServerUtil restUtil;
    
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    
    public CentreDefaulterResource(
            final RestServerUtil restUtil,
            
            final EntityCentre centre,
            
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
            final IGlobalDomainTreeManager gdtm = ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider);
            final Map<String, Object> wasRunHolder = restoreModifiedPropertiesHolderFrom(envelope, restUtil);
            final String wasRun = (String) wasRunHolder.get("@@wasRun");
            
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre = CentreUpdater.updateCentre(gdtm, miType, deviceSpecific(FRESH_CENTRE_NAME, device()));
            final ICentreDomainTreeManagerAndEnhancer updatedSavedCentre = CentreUpdater.updateCentre(gdtm, miType, deviceSpecific(SAVED_CENTRE_NAME, device()));
//            // discards fresh centre's changes (fresh centre could have no changes)
//            if (CentreUtils.isFreshCentreChanged(updatedFreshCentre, updatedSavedCentre)) {
//                CentreUpdater.initAndCommit(gdtm, miType, deviceSpecific(FRESH_CENTRE_NAME, device()), updatedSavedCentre);
//            }
            
            // it is necessary to use "fresh" instance of cdtme (after the discarding process)
            final ICentreDomainTreeManagerAndEnhancer newFreshCentre = CentreUpdater.centre(gdtm, miType, deviceSpecific(FRESH_CENTRE_NAME, device()));
            final String staleCriteriaMessage = CriteriaResource.createStaleCriteriaMessage(wasRun, newFreshCentre, miType, gdtm, companionFinder, critGenerator, device());
            return CriteriaResource.createCriteriaDiscardEnvelope(newFreshCentre, miType, gdtm, restUtil, companionFinder, critGenerator, staleCriteriaMessage, device());
        }, restUtil);
    }
    
}