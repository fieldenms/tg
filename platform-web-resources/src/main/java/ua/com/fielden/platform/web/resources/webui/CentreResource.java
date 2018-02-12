package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaMetaValues;
import static ua.com.fielden.platform.web.resources.webui.CentreResourceUtils.createCriteriaMetaValuesCustomObject;
import static ua.com.fielden.platform.web.resources.webui.CriteriaResource.createStaleCriteriaMessage;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getEntityType;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.restoreModifiedPropertiesHolderFrom;

import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.centre.CentreUpdater;
import ua.com.fielden.platform.web.centre.CentreUtils;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils;
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
public class CentreResource<CRITERIA_TYPE extends AbstractEntity<?>> extends ServerResource {
    private final static Logger logger = Logger.getLogger(CentreResource.class);

    private final RestServerUtil restUtil;

    private final Class<? extends MiWithConfigurationSupport<?>> miType;

    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;
    private final ICompanionObjectFinder companionFinder;
    private final ICriteriaGenerator critGenerator;

    public CentreResource(
            final RestServerUtil restUtil,

            final EntityCentre centre,

            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,

            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

        this.restUtil = restUtil;

        miType = centre.getMenuItemType();
        this.serverGdtm = serverGdtm;
        this.userProvider = userProvider;
        this.companionFinder = companionFinder;
        this.critGenerator = critGenerator;
    }
    
    /**
     * Handles POST request resulting from tg-entity-centre <code>save()</code> method.
     *
     * Internally validation process is also performed.
     */
    @Post
    public Representation save(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            final IGlobalDomainTreeManager gdtm = ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider);
            final Map<String, Object> modifiedPropertiesHolder = restoreModifiedPropertiesHolderFrom(envelope, restUtil);
            
            // before SAVING process there is a need to apply all actual criteria from modifHolder:
            final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = CentreResourceUtils.createCriteriaEntity(modifiedPropertiesHolder, companionFinder, critGenerator, miType, gdtm);
            
            // There is a need to validate criteria entity with the check for 'required' properties. If it is not successful -- immediately return result without saving.
            final Result validationResult = appliedCriteriaEntity.isValid();
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre = appliedCriteriaEntity.getCentreDomainTreeMangerAndEnhancer();
            if (!validationResult.isSuccessful()) {
                logger.debug("CENTRE_RESOURCE: save finished (validation failed).");
                return restUtil.rawListJSONRepresentation(
                    appliedCriteriaEntity,
                    createCriteriaMetaValuesCustomObject(
                            createCriteriaMetaValues(updatedFreshCentre, getEntityType(miType)),
                            CentreResourceUtils.isFreshCentreChanged(updatedFreshCentre, updateCentre(gdtm, miType, CentreUpdater.SAVED_CENTRE_NAME)),
                            createStaleCriteriaMessage((String) modifiedPropertiesHolder.get("@@wasRun"), updatedFreshCentre, miType, gdtm, companionFinder, critGenerator)
                    )
                );
            }
            
            final ICentreDomainTreeManagerAndEnhancer freshCentre = CentreUtils.centre(gdtm, miType, CentreUpdater.FRESH_CENTRE_NAME);
            CentreUpdater.initAndCommit(gdtm, miType, CentreUpdater.SAVED_CENTRE_NAME, freshCentre);
            
            // it is necessary to use "fresh" instance of cdtme (after the saving process)
            return CriteriaResource.createCriteriaRetrievalEnvelope(freshCentre, miType, gdtm, restUtil, companionFinder, critGenerator);
        }, restUtil);
    }
    
    /**
     * Handles PUT request resulting from tg-entity-centre <code>discard()</code> method.
     *
     * Internally validation process is also performed.
     */
    @Put
    public Representation discard(final Representation envelope) {
        return handleUndesiredExceptions(getResponse(), () -> {
            final IGlobalDomainTreeManager gdtm = ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider);
            final Map<String, Object> wasRunHolder = restoreModifiedPropertiesHolderFrom(envelope, restUtil);
            final String wasRun = (String) wasRunHolder.get("@@wasRun");

            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre = CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.FRESH_CENTRE_NAME);
            final ICentreDomainTreeManagerAndEnhancer updatedSavedCentre = CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.SAVED_CENTRE_NAME);
            // discards fresh centre's changes (fresh centre could have no changes)
            if (CentreUtils.isFreshCentreChanged(updatedFreshCentre, updatedSavedCentre)) {
                CentreUpdater.initAndCommit(gdtm, miType, CentreUpdater.FRESH_CENTRE_NAME, updatedSavedCentre);
            }
            
            // it is necessary to use "fresh" instance of cdtme (after the discarding process)
            final ICentreDomainTreeManagerAndEnhancer newFreshCentre = CentreUpdater.centre(gdtm, miType, CentreUpdater.FRESH_CENTRE_NAME);
            final String staleCriteriaMessage = CriteriaResource.createStaleCriteriaMessage(wasRun, newFreshCentre, miType, gdtm, companionFinder, critGenerator);
            return CriteriaResource.createCriteriaDiscardEnvelope(newFreshCentre, miType, gdtm, restUtil, companionFinder, critGenerator, staleCriteriaMessage);
        }, restUtil);
    }
}