package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.SAVED_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.centre;
import static ua.com.fielden.platform.web.centre.CentreUpdater.initAndCommit;
import static ua.com.fielden.platform.web.centre.CentreUpdater.loadableConfigurations;
import static ua.com.fielden.platform.web.centre.CentreUpdater.removeCentres;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.centre.CentreUtils.isFreshCentreChanged;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getUserSpecificGlobalManager;
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
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.centre.CentreConfigEditActionProducer;
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
    private final static Logger logger = Logger.getLogger(CentreResource.class);
    
    private final RestServerUtil restUtil;
    
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final Optional<String> saveAsName;
    
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;
    
    public CentreResource(
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
     * Handles PUT request resulting from tg-entity-centre <code>discard()</code> method.
     *
     * Internally validation process is also performed.
     */
    @Put
    public Representation discard(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            final IGlobalDomainTreeManager gdtm = getUserSpecificGlobalManager(serverGdtm, userProvider);
            final Map<String, Object> wasRunHolder = restoreModifiedPropertiesHolderFrom(envelope, restUtil);
            final String wasRun = (String) wasRunHolder.get("@@wasRun");
            
            final ICentreDomainTreeManagerAndEnhancer newFreshCentre;
            
            if (CentreConfigEditActionProducer.isInherited(saveAsName, () -> loadableConfigurations(gdtm, miType, device(), companionFinder).stream()) ) { 
                removeCentres(gdtm, miType, device(), saveAsName, FRESH_CENTRE_NAME, SAVED_CENTRE_NAME);
                
                // it is necessary to use "fresh" instance of cdtme (after the defaulting process)
                newFreshCentre = updateCentre(gdtm, miType, FRESH_CENTRE_NAME, saveAsName, device());
            } else {
                final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre = updateCentre(gdtm, miType, FRESH_CENTRE_NAME, saveAsName, device());
                final ICentreDomainTreeManagerAndEnhancer updatedSavedCentre = updateCentre(gdtm, miType, SAVED_CENTRE_NAME, saveAsName, device());
                // discards fresh centre's changes (fresh centre could have no changes)
                if (isFreshCentreChanged(updatedFreshCentre, updatedSavedCentre)) {
                    initAndCommit(gdtm, miType, FRESH_CENTRE_NAME, saveAsName, device(), updatedSavedCentre, null);
                }
                
                // it is necessary to use "fresh" instance of cdtme (after the discarding process)
                newFreshCentre = centre(gdtm, miType, FRESH_CENTRE_NAME, saveAsName, device());
            }
            
            final String staleCriteriaMessage = createStaleCriteriaMessage(wasRun, newFreshCentre, miType, saveAsName, gdtm, companionFinder, critGenerator, device());
            return createCriteriaDiscardEnvelope(newFreshCentre, miType, saveAsName, gdtm, restUtil, companionFinder, critGenerator, staleCriteriaMessage, device());
        }, restUtil);
    }
    
}