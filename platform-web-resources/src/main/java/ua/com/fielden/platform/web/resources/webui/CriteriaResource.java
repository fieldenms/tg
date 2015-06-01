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
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
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

    private final RestServerUtil restUtil;
    private final ICompanionObjectFinder companionFinder;

    private final IGlobalDomainTreeManager gdtm;
    private final ICriteriaGenerator critGenerator;
    private final EntityCentre<T> centre;

    public CriteriaResource(
            final RestServerUtil restUtil,
            final ICompanionObjectFinder companionFinder,

            final EntityCentre<T> centre,
            final IGlobalDomainTreeManager gdtm,
            final ICriteriaGenerator critGenerator,

            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

        this.restUtil = restUtil;
        this.companionFinder = companionFinder;

        this.centre = centre;
        this.gdtm = gdtm;
        this.critGenerator = critGenerator;
    }

    /**
     * Handles GET requests resulting from tg-selection-criteria <code>retrieve()</code> method (new entity).
     */
    @Get
    @Override
    public Representation get() throws ResourceException {
        return EntityResourceUtils.handleUndesiredExceptions(() -> {
            final Class<? extends MiWithConfigurationSupport<?>> miType = centre.getMenuItemType();
            final ICentreDomainTreeManagerAndEnhancer originalCdtmae = CentreResourceUtils.getFreshCentre(gdtm, miType);
            // NOTE: the following line can be the example how 'criteria retrieval' server errors manifest to the client application
            // throw new IllegalStateException("Illegal state during criteria retrieval.");
            return restUtil.rawListJSONRepresentation(
                    CentreResourceUtils.createCriteriaValidationPrototype(miType, originalCdtmae, critGenerator, -1L),
                    CentreResourceUtils.createCriteriaMetaValuesCustomObject(
                            CentreResourceUtils.createCriteriaMetaValues(originalCdtmae, CentreResourceUtils.getEntityType(miType)),
                            CentreResourceUtils.isFreshCentreChanged(miType, gdtm) //
                    )//
            );
        }, restUtil);
    }

    /**
     * Handles POST request resulting from tg-selection-criteria <code>validate()</code> method.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        return EntityResourceUtils.handleUndesiredExceptions(() -> {
            final Class<? extends MiWithConfigurationSupport<?>> miType = centre.getMenuItemType();
            final ICentreDomainTreeManagerAndEnhancer originalCdtmae = CentreResourceUtils.getFreshCentre(gdtm, miType);
            final Map<String, Object> modifiedPropertiesHolder = EntityResourceUtils.restoreModifiedPropertiesHolderFrom(envelope, restUtil);

            // NOTE: the following line can be the example how 'criteria retrieval' server errors manifest to the client application
            // throw new IllegalStateException("Illegal state during criteria validation.");
            return restUtil.rawListJSONRepresentation(
                    CentreResourceUtils.createCriteriaEntity(modifiedPropertiesHolder, companionFinder, critGenerator, miType, originalCdtmae),
                    CentreResourceUtils.createCriteriaMetaValuesCustomObject(
                            CentreResourceUtils.createCriteriaMetaValues(originalCdtmae, CentreResourceUtils.getEntityType(miType)),
                            CentreResourceUtils.isFreshCentreChanged(miType, gdtm)//
                    )//
            );
        }, restUtil);
    }

    /**
     * Handles PUT request resulting from tg-selection-criteria <code>run()</code> method.
     */
    @Put
    @Override
    public Representation put(final Representation envelope) throws ResourceException {
        return EntityResourceUtils.handleUndesiredExceptions(() -> {
            //            // NOTE: the following line can be the example how 'centre running' server errors manifest to the client application
            //            throw new IllegalStateException("Illegal state during centre running.");
            final Class<? extends MiWithConfigurationSupport<?>> miType = centre.getMenuItemType();
            final CentreContextHolder centreContextHolder = EntityResourceUtils.restoreCentreContextHolder(envelope, restUtil);

            final ICentreDomainTreeManagerAndEnhancer originalCdtmae = CentreResourceUtils.getFreshCentre(gdtm, miType);
            final M appliedCriteriaEntity = CentreResourceUtils.<T, M> createCriteriaEntity(centreContextHolder.getModifHolder(), companionFinder, critGenerator, miType, originalCdtmae);

            final Map<String, Object> customObject = new LinkedHashMap<String, Object>(centreContextHolder.getCustomObject());
            final Pair<Map<String, Object>, ArrayList<?>> pair =
                    CentreResourceUtils.<T, M> createCriteriaMetaValuesCustomObjectWithResult(
                            customObject,
                            CentreResourceUtils.createCriteriaMetaValues(originalCdtmae, CentreResourceUtils.getEntityType(miType)),
                            appliedCriteriaEntity,
                            CentreResourceUtils.isFreshCentreChanged(miType, gdtm),
                            centre.getAdditionalFetchProvider(),
                            createQueryEnhancerAndContext(centreContextHolder, centre.getQueryEnhancerConfig(), appliedCriteriaEntity));
            if (pair.getValue() == null) {
                return restUtil.rawListJSONRepresentation(appliedCriteriaEntity, pair.getKey());
            }

            //Running the rendering customiser for result set of entities.
            @SuppressWarnings("unchecked")
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

            enhanceResultEntitiesWithCustomPropertyValues(centre.getCustomPropertiesDefinitions(), centre.getCustomPropertiesAsignmentHandler(), (List<AbstractEntity<?>>) pair.getValue());

            final ArrayList<Object> list = new ArrayList<Object>();
            list.add(appliedCriteriaEntity);
            list.add(pair.getKey());

            list.addAll(pair.getValue()); // TODO why is this needed for serialisation to perform without problems?!

            // NOTE: the following line can be the example how 'criteria running' server errors manifest to the client application
            // throw new IllegalStateException("Illegal state during criteria running.");
            return restUtil.rawListJSONRepresentation(list.toArray());
        }, restUtil);
    }

    private static <T extends AbstractEntity<?>, M extends EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>>> Optional<Pair<IQueryEnhancer<T>, Optional<CentreContext<T, ?>>>> createQueryEnhancerAndContext(final CentreContextHolder centreContextHolder, final Optional<Pair<IQueryEnhancer<T>, Optional<CentreContextConfig>>> queryEnhancerConfig, final M criteriaEntity) {
        if (queryEnhancerConfig.isPresent()) {
            return Optional.of(new Pair<>(
                    queryEnhancerConfig.get().getKey(),
                    CentreResourceUtils.<T, M> createCentreContext(centreContextHolder, criteriaEntity, queryEnhancerConfig.get().getValue())//
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
    private void enhanceResultEntitiesWithCustomPropertyValues(final Optional<List<ResultSetProp>> propertiesDefinitions, final Optional<Class<? extends ICustomPropsAssignmentHandler<? extends AbstractEntity<?>>>> customPropertiesAsignmentHandler, final List<AbstractEntity<?>> entities) {
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

    private void setCustomValue(final List<AbstractEntity<?>> entities, final String propertyName, final Object value) {
        for (final AbstractEntity<?> entity : entities) {
            entity.set(propertyName, value);
        }
    }

    private void setCustomValues(final List<AbstractEntity<?>> entities, final ICustomPropsAssignmentHandler<T> assignmentHandler) {
        for (final AbstractEntity<?> entity : entities) {
            assignmentHandler.assignValues((T) entity);
        }
    }
}
