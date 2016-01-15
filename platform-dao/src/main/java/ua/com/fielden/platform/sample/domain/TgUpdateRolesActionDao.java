package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.IUniversalConstants;

/** 
 * DAO implementation for companion object {@link ITgUpdateRolesAction}.
 * 
 * @author Developers
 *
 */
@EntityType(TgUpdateRolesAction.class)
public class TgUpdateRolesActionDao extends CommonEntityDao<TgUpdateRolesAction> implements ITgUpdateRolesAction {
    private final Logger logger = Logger.getLogger(getClass());
    private final IUserRoleDao coUserRole;
    private final IUserAndRoleAssociationBatchAction coUserAndRoleAssociationBatchAction;
    
    @Inject
    public TgUpdateRolesActionDao(final IFilter filter, final IUserRoleDao coUserRole, final IUserAndRoleAssociationBatchAction coUserAndRoleAssociationBatchAction) {
        super(filter);
        this.coUserRole = coUserRole;
        this.coUserAndRoleAssociationBatchAction = coUserAndRoleAssociationBatchAction;
    }
    
    @Override
    @SessionRequired
    public TgUpdateRolesAction save(final TgUpdateRolesAction entity) {
        final Result res = entity.isValid();
        if (!res.isSuccessful()) {
            throw res;
        }
        
        logger.error("surrogate version after returning to the server == " + entity.getSurrogateVersion());
        
        // logger.error("entity.getRoles() = " + entity.getRoles());
        // logger.error("entity.getContext().getMasterEntity() = " + entity.getContext().getMasterEntity());
        // logger.error("((User) entity.getContext().getMasterEntity()).getRoles() = " + ((User) entity.getContext().getMasterEntity()).getRoles() );
        
        logger.error("entity.getChosenIds() = " + entity.getChosenIds());
        logger.error("entity.getAddedIds() = " + entity.getAddedIds());
        logger.error("entity.getRemovedIds() = " + entity.getRemovedIds());
        
        final User userBeingUpdated = (User) entity.getContext().getMasterEntity();
        final Map<Long, UserRole> roles = mapById(coUserRole.findAll());
        
        final Set<UserAndRoleAssociation> addedAssociations = new LinkedHashSet<>();
        for (final Long addedId : entity.getAddedIds()) {
            if (!roles.containsKey(addedId)) {
                throw Result.failure(String.format("Another user has deleted the role with id = %s.", addedId)); // TODO need to have a description of non-existent entity?
            }
            addedAssociations.add(new UserAndRoleAssociation(userBeingUpdated, roles.get(addedId)));
        }

        final Set<UserAndRoleAssociation> removedAssociations = new LinkedHashSet<>();
        for (final Long removedId : entity.getRemovedIds()) {
            if (!roles.containsKey(removedId)) {
                throw Result.failure(String.format("Another user has deleted the role with id = %s.", removedId)); // TODO need to have a description of non-existent entity?
            }
            removedAssociations.add(new UserAndRoleAssociation(userBeingUpdated, roles.get(removedId)));
        }

        final UserAndRoleAssociationBatchAction action = new UserAndRoleAssociationBatchAction();
        action.setSaveEntities(addedAssociations);
        action.setRemoveEntities(removedAssociations);
        
        final TgUpdateRolesAction persistedEntity = TgUpdateRolesActionProducer.retrieveActionFor(userBeingUpdated, this);
        final TgUpdateRolesAction entityToSave = persistedEntity == null ? new TgUpdateRolesAction().setKey(userBeingUpdated) : persistedEntity;
        final Integer currentVersion = TgUpdateRolesActionProducer.surrogateVersion(persistedEntity);
        // entityToSave.setDirtinessMarker(Long.valueOf(universalConstants.now().getMillis()).toString() );
        if (persistedEntity != null) {
            entityToSave.setDirtinessMarker(!persistedEntity.getDirtinessMarker());
        }
        
        if (currentVersion > entity.getSurrogateVersion()) {
            throw Result.failure(String.format("Another user has changed 'roles' collection of [%s]. entityToSave.getVersion() = %s entity.getVersion() = %s", userBeingUpdated, entityToSave.getVersion(), entity.getVersion()));
        }
        
        coUserAndRoleAssociationBatchAction.save(action);
        final TgUpdateRolesAction saved = super.save(entityToSave);
        logger.error("saved.getVersion() = " + saved.getVersion());
        return saved;
    }
    
    /**
     * Returns the map between id and the entity with that id.
     * 
     * @param entities
     * @return
     */
    private <T extends AbstractEntity<?>> Map<Long, T> mapById(final Collection<T> entities) {
        final Map<Long, T> map = new HashMap<>();
        for (final T entity : entities) {
            map.put(entity.getId(), entity);
        }
        return map;
    }
}