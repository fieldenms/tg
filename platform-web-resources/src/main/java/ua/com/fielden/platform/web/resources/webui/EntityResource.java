package ua.com.fielden.platform.web.resources.webui;

import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.development.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * The web resource for entity serves as a back-end mechanism of entity retrieval, saving and deletion. It provides a base implementation for handling the following methods:
 * <ul>
 * <li>retrieve entity -- GET request;
 * <li>save new entity -- PUT request with an envelope containing an instance of an entity to be persisted;
 * <li>save already persisted entity -- POST request with an envelope containing an instance of an modified entity to be changed;
 * <li>delete entity -- DELETE request.
 * </ul>
 *
 * @author TG Team
 *
 */
public class EntityResource<T extends AbstractEntity<?>> extends ServerResource {
    private final EntityResourceUtils<T> utils;
    private final RestServerUtil restUtil;
    private final Long entityId;
    private final Logger logger = Logger.getLogger(getClass());

    private final ICompanionObjectFinder companionFinder;
    private final IGlobalDomainTreeManager gdtm;
    private final ICriteriaGenerator critGenerator;

    public EntityResource(final Class<T> entityType, final IEntityProducer<T> entityProducer, final EntityFactory entityFactory, final RestServerUtil restUtil, final ICompanionObjectFinder companionFinder, final IGlobalDomainTreeManager gdtm, final ICriteriaGenerator critGenerator, final Context context, final Request request, final Response response) {
        init(context, request, response);

        this.companionFinder = companionFinder;
        this.gdtm = gdtm;
        this.critGenerator = critGenerator;
        utils = new EntityResourceUtils<T>(entityType, entityProducer, entityFactory, restUtil, this.companionFinder);
        this.restUtil = restUtil;

        final String entityIdString = request.getAttributes().get("entity-id").toString();
        this.entityId = entityIdString.equalsIgnoreCase("new") ? null : Long.parseLong(entityIdString);
    }

    /**
     * Handles POST requests resulting from tg-entity-master <code>save()</code> method (persisted entity).
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        return tryToSave(envelope);
    }

    /**
     * Handles PUT requests resulting from tg-entity-master <code>retrieve(context)</code> method (new or persisted entity).
     */
    @Put
    @Override
    public Representation put(final Representation envelope) throws ResourceException {
        if (envelope != null) {
            final T entity = utils.createValidationPrototypeWithCentreContext(createCentreContext(EntityResourceUtils.restoreCentreContextHolder(envelope, restUtil)));
            ((AbstractFunctionalEntityWithCentreContext) entity).setContext(null);
            return restUtil.rawListJSONRepresentation(entity);
        } else {
            return restUtil.rawListJSONRepresentation(utils.createValidationPrototype(entityId));
        }
    }

    private CentreContext<T, AbstractEntity<?>> createCentreContext(final CentreContextHolder centreContextHolder) {
        logger.error("centreContextHolder during entity retrieve == " + centreContextHolder + " modfiHolder! == " + centreContextHolder.getModifHolder() + " @@miType == " + centreContextHolder.getModifHolder().get("@@miType"));

        Class<? extends MiWithConfigurationSupport<?>> miType;
        try {
            miType = (Class<? extends MiWithConfigurationSupport<?>>) Class.forName((String) centreContextHolder.getModifHolder().get("@@miType"));
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        final ICentreDomainTreeManagerAndEnhancer originalCdtmae = CentreResourceUtils.getFreshCentre(gdtm, miType);

        final Map<String, Object> modifiedPropertiesHolder = centreContextHolder.getModifHolder();

        CentreResourceUtils.applyMetaValues(originalCdtmae, CentreResourceUtils.getEntityType(miType), modifiedPropertiesHolder);
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> validationPrototype =
                CentreResourceUtils.createCriteriaValidationPrototype(miType, originalCdtmae, critGenerator, EntityResourceUtils.getVersion(modifiedPropertiesHolder));
        final EnhancedCentreEntityQueryCriteria<AbstractEntity<?>, IEntityDao<AbstractEntity<?>>> selectionCrit = EntityResourceUtils.constructEntityAndResetMetaValues(modifiedPropertiesHolder, validationPrototype, companionFinder).getKey();

        logger.error("selectionCrit == " + selectionCrit);

        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        // if (contextConfig.isPresent() && contextConfig.get().withSelectionCrit) {
        context.setSelectionCrit(selectionCrit);
        // }
        //            if (contextConfig.isPresent() && contextConfig.get().withAllSelectedEntities) {
        context.setSelectedEntities(centreContextHolder.getSelectedEntities());
        //            } else if (contextConfig.isPresent() && contextConfig.get().withCurrentEtity) {
        //                context.setSelectedEntities((List<T>) centreContextHolder.getSelectedEntities());
        //            }
        //            if (contextConfig.isPresent() && contextConfig.get().withMasterEntity) {
        context.setMasterEntity(centreContextHolder.getMasterEntity());
        return (CentreContext<T, AbstractEntity<?>>) context;
    }

    @Delete
    @Override
    public Representation delete() {
        if (entityId == null) {
            final String message = String.format("New entity was not persisted and thus can not be deleted. Actually this error should be prevented at the client-side.");
            logger.error(message);
            throw new IllegalStateException(message);
        }

        return delete(entityId);
    }

    /**
     * Tries to save the changes for the entity and returns it in JSON format.
     *
     * @param envelope
     * @return
     */
    private Representation tryToSave(final Representation envelope) {
        final SavingInfoHolder savingInfoHolder = EntityResourceUtils.restoreSavingInfoHolder(envelope, restUtil);
        final Map<String, Object> modifiedPropertiesHolder = savingInfoHolder.getModifHolder();
        final T applied;
        if (savingInfoHolder.getCentreContextHolder() == null) {
            applied = utils.constructEntity(modifiedPropertiesHolder, this.entityId).getKey();
        } else {
            applied = utils.constructEntity(modifiedPropertiesHolder, createCentreContext(savingInfoHolder.getCentreContextHolder())).getKey();
        }

        final T potentiallySaved = applied.isDirty() ? save(applied) : applied;
        if (savingInfoHolder.getCentreContextHolder() != null) {
            ((AbstractFunctionalEntityWithCentreContext) potentiallySaved).setContext(null);
        }
        return restUtil.singleJSONRepresentation(potentiallySaved);
    }

    /**
     * Checks the entity on validation errors and saves it if validation was successful, returns it "as is" if validation was not successful.
     *
     * @param validatedEntity
     * @return
     */
    private T save(final T validatedEntity) {
        try {
            final Result validationResult = validatedEntity.isValid();
            if (validationResult.isSuccessful()) {
                return utils.save(validatedEntity);
            } else {
                return validatedEntity;
            }
        } catch (final Exception ex) {
            logger.error("An undesirable error has occured during saving of already successfully validated entity.", ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Tries to delete the entity with <code>entityId</code> and returns result. If successful -- result instance is <code>null</code>, otherwise -- result instance is also
     * <code>null</code> (not-deletable entity should exist at the client side, no need to send it many times).
     *
     * @param entityId
     *
     * @return
     * @throws JsonProcessingException
     */
    private Representation delete(final Long entityId) {
        try {
            utils.delete(entityId);
            return restUtil.resultJSONRepresentation(Result.successful(null));
        } catch (final Exception e) {
            final String message = String.format("The entity with id [%s] and type [%s] can not be deleted due to existing dependencies.", entityId, utils.getEntityType().getSimpleName());
            logger.error(message, e);
            return restUtil.resultJSONRepresentation(Result.failure(message));
        }
    }

}
