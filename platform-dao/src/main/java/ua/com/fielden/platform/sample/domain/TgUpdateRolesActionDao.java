package ua.com.fielden.platform.sample.domain;

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
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IUserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

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
        
        logger.error("entity.getRoles() = " + entity.getRoles());
        logger.error("entity.getContext().getMasterEntity() = " + entity.getContext().getMasterEntity());
        logger.error("((User) entity.getContext().getMasterEntity()).getRoles() = " + ((User) entity.getContext().getMasterEntity()).getRoles() );
        
        logger.error("entity.getChosenRoleIds() = " + entity.getChosenRoleIds());
        logger.error("entity.getAddedRoleIds() = " + entity.getAddedRoleIds());
        logger.error("entity.getRemovedRoleIds() = " + entity.getRemovedRoleIds());
        
        final User userBeingUpdated = (User) entity.getContext().getMasterEntity();
        final Map<Long, UserRole> roles = mapById(coUserRole.findAll());
        
        final Set<UserAndRoleAssociation> addedAssociations = new LinkedHashSet<>();
        for (final Long addedRoleId : entity.getAddedRoleIds()) {
            if (!roles.containsKey(addedRoleId)) {
                throw Result.failure(String.format("Another user has deleted the role with id = %s.", addedRoleId));
            }
            addedAssociations.add(new UserAndRoleAssociation(userBeingUpdated, roles.get(addedRoleId)));
        }

        final Set<UserAndRoleAssociation> removedAssociations = new LinkedHashSet<>();
        for (final Long removedRoleId : entity.getRemovedRoleIds()) {
            if (!roles.containsKey(removedRoleId)) {
                throw Result.failure(String.format("Another user has deleted the role with id = %s.", removedRoleId));
            }
            removedAssociations.add(new UserAndRoleAssociation(userBeingUpdated, roles.get(removedRoleId)));
        }

        final UserAndRoleAssociationBatchAction action = new UserAndRoleAssociationBatchAction();
        action.setSaveEntities(addedAssociations);
        action.setRemoveEntities(removedAssociations);
        
        coUserAndRoleAssociationBatchAction.save(action);
        
        return entity;
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