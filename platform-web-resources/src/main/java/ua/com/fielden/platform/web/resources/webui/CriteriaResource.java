package ua.com.fielden.platform.web.resources.webui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.CentreUpdater;
import ua.com.fielden.platform.web.centre.CentreUtils;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for criteria serves as a back-end mechanism of criteria retrieval. It provides a base implementation for handling the following methods:
 * <ul>
 * <li>retrieve entity -- GET request.
 * </ul>
 *
 * @author TG Team
 *
 */
public class CriteriaResource<T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> extends ServerResource {
    private final static Logger logger = Logger.getLogger(CriteriaResource.class);

    private final static String staleCriteriaMessage = "Selection criteria have been changed, but not applied. "
                                                     + "Previously applied values are in effect. "
                                                     + "Please tap action <b>RUN</b> to apply the updated selection criteria.";

    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;

    private final ICriteriaGenerator critGenerator;
    private final EntityCentre<T> centre;

    private final IWebUiConfig webUiConfig;
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IUserProvider userProvider;
    private final EntityFactory entityFactory;

    public CriteriaResource(
            final RestServerUtil restUtil,
            final EntityCentre<T> centre,
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

        this.restUtil = restUtil;
        this.companionFinder = companionFinder;

        this.centre = centre;
        this.critGenerator = critGenerator;

        this.webUiConfig = webUiConfig;
        this.serverGdtm = serverGdtm;
        this.userProvider = userProvider;
        this.entityFactory = entityFactory;
    }

    /**
     * Handles GET requests resulting from tg-selection-criteria <code>retrieve()</code> method (new entity).
     */
    @Get
    @Override
    public Representation get() throws ResourceException {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            final Class<? extends MiWithConfigurationSupport<?>> miType = centre.getMenuItemType();
            final IGlobalDomainTreeManager gdtm = ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider);
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre = CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.FRESH_CENTRE_NAME);
            // NOTE: the following line can be the example how 'criteria retrieval' server errors manifest to the client application
            // throw new IllegalStateException("Illegal state during criteria retrieval.");
            return createCriteriaRetrievalEnvelope(updatedFreshCentre, miType, gdtm, restUtil, companionFinder, critGenerator);
        }, restUtil);
    }

    /**
     * Handles POST request resulting from tg-selection-criteria <code>validate()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            return createCriteriaValidationEnvelope(
                    EntityResourceUtils.restoreModifiedPropertiesHolderFrom(envelope, restUtil), 
                    centre.getMenuItemType(), 
                    ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider), 
                    restUtil, 
                    companionFinder, 
                    critGenerator);
        }, restUtil);
    }

    public static Representation createCriteriaRetrievalEnvelope(
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final IGlobalDomainTreeManager gdtm,
            final RestServerUtil restUtil,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator
                    ) {
        return restUtil.rawListJSONRepresentation(
                CentreResourceUtils.createCriteriaValidationPrototype(miType, updatedFreshCentre, critGenerator, -1L, gdtm),
                CentreResourceUtils.createCriteriaMetaValuesCustomObject(
                        CentreResourceUtils.createCriteriaMetaValues(updatedFreshCentre, CentreResourceUtils.getEntityType(miType)),
                        CentreResourceUtils.isFreshCentreChanged(updatedFreshCentre, CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.SAVED_CENTRE_NAME))
                )//
        );
    }

    public static Representation createCriteriaDiscardEnvelope(
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final IGlobalDomainTreeManager gdtm,
            final RestServerUtil restUtil,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator,
            final String staleCriteriaMessage
                    ) {
        return restUtil.rawListJSONRepresentation(
                CentreResourceUtils.createCriteriaValidationPrototype(miType, updatedFreshCentre, critGenerator, -1L, gdtm),
                CentreResourceUtils.createCriteriaMetaValuesCustomObject(
                        CentreResourceUtils.createCriteriaMetaValues(updatedFreshCentre, CentreResourceUtils.getEntityType(miType)),
                        CentreResourceUtils.isFreshCentreChanged(updatedFreshCentre, CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.SAVED_CENTRE_NAME)),
                        staleCriteriaMessage
                )//
        );
    }

    private static Representation createCriteriaValidationEnvelope(
            final Map<String, Object> modifiedPropertiesHolder,
            final Class<? extends MiWithConfigurationSupport<?>> miType,
            final IGlobalDomainTreeManager gdtm,
            final RestServerUtil restUtil,
            final ICompanionObjectFinder companionFinder,
            final ICriteriaGenerator critGenerator
                    ) {
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, ? extends IEntityDao<AbstractEntity<?>>> appliedCriteriaEntity = CentreResourceUtils.createCriteriaEntity(modifiedPropertiesHolder, companionFinder, critGenerator, miType, gdtm);
        final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre = appliedCriteriaEntity.getCentreDomainTreeMangerAndEnhancer();
        return restUtil.rawListJSONRepresentation(
                appliedCriteriaEntity,
                CentreResourceUtils.createCriteriaMetaValuesCustomObject(
                        CentreResourceUtils.createCriteriaMetaValues(updatedFreshCentre, CentreResourceUtils.getEntityType(miType)),
                        CentreResourceUtils.isFreshCentreChanged(updatedFreshCentre, CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.SAVED_CENTRE_NAME)),
                        createStaleCriteriaMessage((String) modifiedPropertiesHolder.get("@@wasRun"), updatedFreshCentre, miType, gdtm, companionFinder, critGenerator)
                )//
        );
    }

    public static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> String createStaleCriteriaMessage(final String wasRun, final ICentreDomainTreeManagerAndEnhancer freshCentre, final Class<? extends MiWithConfigurationSupport<?>> miType, final IGlobalDomainTreeManager gdtm, final ICompanionObjectFinder companionFinder, final ICriteriaGenerator critGenerator) {
        if (wasRun != null) {
            final boolean isCriteriaStale = !EntityUtils.equalsEx(CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.PREVIOUSLY_RUN_CENTRE_NAME), freshCentre);
            if (isCriteriaStale) {
                logger.info(staleCriteriaMessage);
                return staleCriteriaMessage;
            }
        }
        return null;
    }

    /**
     * Handles PUT request resulting from tg-selection-criteria <code>run()</code> method.
     */
    @SuppressWarnings("unchecked")
    @Put
    @Override
    public Representation put(final Representation envelope) {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            logger.debug("CRITERIA_RESOURCE: run started.");
            //            // NOTE: the following line can be the example how 'centre running' server errors manifest to the client application
            //            throw new IllegalStateException("Illegal state during centre running.");
            final Class<? extends MiWithConfigurationSupport<?>> miType = centre.getMenuItemType();
            final CentreContextHolder centreContextHolder = EntityResourceUtils.restoreCentreContextHolder(envelope, restUtil);

            final Map<String, Object> customObject = new LinkedHashMap<String, Object>(centreContextHolder.getCustomObject());

            final IGlobalDomainTreeManager gdtm = ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider);

            final boolean isRunning = CentreResourceUtils.isRunning(customObject);
            
            final ICentreDomainTreeManagerAndEnhancer updatedFreshCentre;
            if (isRunning) {
                final M freshCentreAppliedCriteriaEntity = CentreResourceUtils.<T, M> createCriteriaEntity(centreContextHolder.getModifHolder(), companionFinder, critGenerator, miType, gdtm);
                updatedFreshCentre = freshCentreAppliedCriteriaEntity.getCentreDomainTreeMangerAndEnhancer();
                
                CentreUpdater.initAndCommit(gdtm, miType, CentreUpdater.PREVIOUSLY_RUN_CENTRE_NAME, updatedFreshCentre);
            } else {
                updatedFreshCentre = null;
            }
            
            final ICentreDomainTreeManagerAndEnhancer previouslyRunCentre = CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.PREVIOUSLY_RUN_CENTRE_NAME);
            final M previouslyRunCriteriaEntity = CentreResourceUtils.<T, M> createCriteriaValidationPrototype(miType, previouslyRunCentre, critGenerator, 0L, gdtm);

            final Pair<Map<String, Object>, ArrayList<?>> pair =
                    CentreResourceUtils.<T, M> createCriteriaMetaValuesCustomObjectWithResult(
                            customObject,
                            previouslyRunCriteriaEntity,
                            centre.getAdditionalFetchProvider(),
                            createQueryEnhancerAndContext(
                                    webUiConfig,
                                    companionFinder,
                                    serverGdtm,
                                    userProvider,
                                    critGenerator,
                                    entityFactory,
                                    centreContextHolder,
                                    centre.getQueryEnhancerConfig(),
                                    previouslyRunCriteriaEntity));
            if (isRunning) {
                pair.getKey().put("isCentreChanged", CentreResourceUtils.isFreshCentreChanged(updatedFreshCentre, CentreUpdater.updateCentre(gdtm, miType, CentreUpdater.SAVED_CENTRE_NAME)));
                pair.getKey().put("metaValues", CentreResourceUtils.createCriteriaMetaValues(updatedFreshCentre, CentreResourceUtils.getEntityType(miType)));
                pair.getKey().put("staleCriteriaMessage", null);
            }

            if (pair.getValue() == null) {
                logger.debug("CRITERIA_RESOURCE: run finished.");
                return restUtil.rawListJSONRepresentation(isRunning ? previouslyRunCriteriaEntity : null, pair.getKey());
            }

            //Running the rendering customiser for result set of entities.
            final Optional<IRenderingCustomiser<T, ?>> renderingCustomiser = centre.getRenderingCustomiser();
            if (renderingCustomiser.isPresent()) {
                final IRenderingCustomiser<T, ?> renderer = renderingCustomiser.get();
                final List<Object> renderingHints = new ArrayList<Object>();
                for (final Object entity : pair.getValue()) {
                    renderingHints.add(renderer.getCustomRenderingFor((T) entity).get());
                }
                pair.getKey().put("renderingHints", renderingHints);
            } else {
                pair.getKey().put("renderingHints", new ArrayList<Object>());
            }

            enhanceResultEntitiesWithCustomPropertyValues(centre, centre.getCustomPropertiesDefinitions(), centre.getCustomPropertiesAsignmentHandler(), (List<AbstractEntity<?>>) pair.getValue());

            final ArrayList<Object> list = new ArrayList<Object>();
            list.add(isRunning ? previouslyRunCriteriaEntity : null);
            list.add(pair.getKey());

            // TODO It looks like adding values directly to the list outside the map object leads to proper type/serialiser correspondence
            // FIXME Need to investigate why this is the case.
            list.addAll(pair.getValue());

            // NOTE: the following line can be the example how 'criteria running' server errors manifest to the client application
            // throw new IllegalStateException("Illegal state during criteria running.");
            logger.debug("CRITERIA_RESOURCE: run finished.");
            return restUtil.rawListJSONRepresentation(list.toArray());
        }, restUtil);
    }

    public static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> Optional<Pair<IQueryEnhancer<T>, Optional<CentreContext<T, ?>>>> createQueryEnhancerAndContext(
            final IWebUiConfig webUiConfig,
            final ICompanionObjectFinder companionFinder,
            final IServerGlobalDomainTreeManager serverGdtm,
            final IUserProvider userProvider,
            final ICriteriaGenerator critGenerator,
            final EntityFactory entityFactory,
            final CentreContextHolder centreContextHolder,
            final Optional<Pair<IQueryEnhancer<T>, Optional<CentreContextConfig>>> queryEnhancerConfig,
            final M criteriaEntity) {
        if (queryEnhancerConfig.isPresent()) {
            return Optional.of(new Pair<>(
                    queryEnhancerConfig.get().getKey(),
                    CentreResourceUtils.<T, M> createCentreContext(
                            webUiConfig,
                            companionFinder,
                            serverGdtm,
                            userProvider,
                            critGenerator,
                            entityFactory,
                            centreContextHolder,
                            criteriaEntity,
                            queryEnhancerConfig.get().getValue())//
            ));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Assigns the values for custom properties.
     *
     * @param propertiesDefinitions
     * @param customPropertiesAsignmentHandler
     * @param entities
     */
    public static <T extends AbstractEntity<?>> void enhanceResultEntitiesWithCustomPropertyValues(final EntityCentre<T> centre, final Optional<List<ResultSetProp>> propertiesDefinitions, final Optional<Class<? extends ICustomPropsAssignmentHandler<? extends AbstractEntity<?>>>> customPropertiesAsignmentHandler, final List<AbstractEntity<?>> entities) {
        if (customPropertiesAsignmentHandler.isPresent()) {
            setCustomValues(entities, centre.createAssignmentHandlerInstance((Class<? extends ICustomPropsAssignmentHandler<T>>) customPropertiesAsignmentHandler.get()));
        }

        if (propertiesDefinitions.isPresent()) {
            for (final ResultSetProp resultSetProp : propertiesDefinitions.get()) {
                if (resultSetProp.propDef.isPresent()) {
                    final PropDef<?> propDef = resultSetProp.propDef.get();
                    final String propertyName = CalculatedProperty.generateNameFrom(propDef.title);
                    if (propDef.value.isPresent()) {
                        setCustomValue(entities, propertyName, propDef.value.get());
                    }
                }
            }
        }
    }

    private static void setCustomValue(final List<AbstractEntity<?>> entities, final String propertyName, final Object value) {
        for (final AbstractEntity<?> entity : entities) {
            entity.set(propertyName, value);
        }
    }

    private static <T extends AbstractEntity<?>> void setCustomValues(final List<AbstractEntity<?>> entities, final ICustomPropsAssignmentHandler<T> assignmentHandler) {
        for (final AbstractEntity<?> entity : entities) {
            assignmentHandler.assignValues((T) entity);
        }
    }
}