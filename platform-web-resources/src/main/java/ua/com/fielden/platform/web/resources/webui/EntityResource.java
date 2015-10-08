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
import org.restlet.resource.ServerResource;

import com.fasterxml.jackson.core.JsonProcessingException;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.view.master.EntityMaster;

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
    public Representation post(final Representation envelope) {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> tryToSave(envelope), restUtil);
    }

    /**
     * Handles PUT requests resulting from tg-entity-master <code>retrieve(context)</code> method (new or persisted entity).
     */
    @Put
    @Override
    public Representation put(final Representation envelope) {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            if (envelope != null) {
                final CentreContextHolder centreContextHolder = EntityResourceUtils.restoreCentreContextHolder(envelope, restUtil);
                final T entity = utils.createValidationPrototypeWithCentreContext(
                        CentreResourceUtils.createCentreContext(
                                centreContextHolder,
                                CentreResourceUtils.createCriteriaEntity(centreContextHolder, companionFinder, gdtm, critGenerator)//
                        ),
                        centreContextHolder.getChosenProperty()
                        );
                ((AbstractFunctionalEntityWithCentreContext) entity).setContext(null); // it is necessary to reset centreContext not to send it back to the client!
                return restUtil.rawListJSONRepresentation(entity);
            } else {
                return restUtil.rawListJSONRepresentation(utils.createValidationPrototype(entityId));
            }
        }, restUtil);
    }

    @Delete
    @Override
    public Representation delete() {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            if (entityId == null) {
                final String message = String.format("New entity was not persisted and thus can not be deleted. Actually this error should be prevented at the client-side.");
                logger.error(message);
                throw new IllegalStateException(message);
            }

            return delete(entityId);
        }, restUtil);
    }

    /**
     * Tries to save the changes for the entity and returns it in JSON format.
     *
     * @param envelope
     * @return
     */
    private Representation tryToSave(final Representation envelope) {
        final SavingInfoHolder savingInfoHolder = EntityResourceUtils.restoreSavingInfoHolder(envelope, restUtil);
        final T applied = restoreEntityFrom(savingInfoHolder, utils, this.entityId, companionFinder, gdtm, critGenerator);

        final T potentiallySaved = applied.isDirty() ? save(applied) : applied;
        if (savingInfoHolder.getCentreContextHolder() != null) {
            ((AbstractFunctionalEntityWithCentreContext) potentiallySaved).setContext(null); // it is necessary to reset centreContext not to send it back to the client!
        }
        return restUtil.singleJSONRepresentation(potentiallySaved);
    }

    /**
     * Restores the functional entity from the <code>savingInfoHolder</code>, that represents it. The <code>savingInfoHolder</code> could potentially contain <code>contreContextHolder</code> inside, which will be deserialised as well.
     * <p>
     * All parameters, except <code>savingInfoHolder</code> and <code>functionalEntityType</code>, could be taken from injector -- they are needed for centre context deserialisation.
     *
     * @param savingInfoHolder -- the actual holder of information about functional entity
     * @param functionalEntityType -- the type of functional entity to be restored into
     * @param entityFactory
     * @param restUtil
     * @param webUiConfig
     * @param companionFinder
     * @param serverGdtm
     * @param userProvider
     * @param critGenerator
     * @return
     */
    public static <T extends AbstractEntity<?>> T restoreEntityFrom(final SavingInfoHolder savingInfoHolder, final Class<T> functionalEntityType, final EntityFactory entityFactory, final RestServerUtil restUtil, final IWebUiConfig webUiConfig, final ICompanionObjectFinder companionFinder, final IServerGlobalDomainTreeManager serverGdtm, final IUserProvider userProvider, final ICriteriaGenerator critGenerator) {
        final IGlobalDomainTreeManager gdtm = ResourceFactoryUtils.getUserSpecificGlobalManager(serverGdtm, userProvider);
        final EntityMaster<T> master = (EntityMaster<T>) webUiConfig.getMasters().get(functionalEntityType);
        final EntityResourceUtils<T> utils = new EntityResourceUtils<T>(functionalEntityType, master.createEntityProducer(), entityFactory, restUtil, companionFinder);
        final Map<String, Object> modifHolder = savingInfoHolder.getModifHolder();

        final Object arrivedIdVal = modifHolder.get(AbstractEntity.ID);
        final Long longId = arrivedIdVal == null ? null : Long.parseLong(arrivedIdVal + "");

        return restoreEntityFrom(savingInfoHolder, utils, longId, companionFinder, gdtm, critGenerator);
    }

    private static <T extends AbstractEntity<?>> T restoreEntityFrom(final SavingInfoHolder savingInfoHolder, final EntityResourceUtils<T> utils, final Long entityId, final ICompanionObjectFinder companionFinder, final IGlobalDomainTreeManager gdtm, final ICriteriaGenerator critGenerator) {
        final Map<String, Object> modifiedPropertiesHolder = savingInfoHolder.getModifHolder();
        final T applied;
        if (savingInfoHolder.getCentreContextHolder() == null) {
            applied = utils.constructEntity(modifiedPropertiesHolder, entityId).getKey();
        } else {
            applied = utils.constructEntity(
                    modifiedPropertiesHolder,
                    CentreResourceUtils.createCentreContext(
                            savingInfoHolder.getCentreContextHolder(),
                            CentreResourceUtils.createCriteriaEntity(savingInfoHolder.getCentreContextHolder(), companionFinder, gdtm, critGenerator)),
                    savingInfoHolder.getCentreContextHolder().getChosenProperty()
                    ).getKey();
        }
        return applied;
    }

    /**
     * Checks the entity on validation errors and saves it if validation was successful, returns it "as is" if validation was not successful.
     *
     * @param validatedEntity
     * @return
     */
    private T save(final T validatedEntity) {
        // the next action validates the entity one more time, but with the check for 'required' properties
        validatedEntity.isValid();

        EntityResourceUtils.disregardCritOnlyRequiredProperties(validatedEntity);
        for (final Map.Entry<String, MetaProperty<?>> entry : validatedEntity.getProperties().entrySet()) {
            if (!entry.getValue().isValid()) {
                return validatedEntity;
            }
        }

        return utils.save(validatedEntity);
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
            throw new IllegalStateException(e);
        }
    }

}
