package ua.com.fielden.platform.security.user;

import static ua.com.fielden.platform.entity.CollectionModificationUtils.persistedActionVersionFor;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IUserRole;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Controller for {@link UserRolesUpdater}.
 * 
 * @author TG Team
 *
 */
public class UserRolesUpdaterController implements ICollectionModificationController<User, UserRolesUpdater, Long, UserRole> {
    private final IEntityDao<User> coUser;
    private final IEntityDao<UserRolesUpdater> co$UserRolesUpdater;
    private final IUserRole coUserRole;
    
    public UserRolesUpdaterController(final IEntityDao<User> coUser, final IEntityDao<UserRolesUpdater> co$UserRolesUpdater, final IUserRole coUserRole) {
        this.coUser = coUser;
        this.co$UserRolesUpdater = co$UserRolesUpdater;
        this.coUserRole = coUserRole;
    }
    
    @Override
    public User getMasterEntityFromContext(final CentreContext<?, ?> context) {
        return context.getMasterEntity() == null ? (context.getSelectedEntities().isEmpty() ? null : (User) context.getCurrEntity()) : (User) context.getMasterEntity();
    }
    
    @Override
    public User refetchMasterEntity(final User masterEntityFromContext) {
        return coUser.findById(masterEntityFromContext.getId(), coUser.getFetchProvider().with("roles").fetchModel());
    }
    
    @Override
    public UserRolesUpdater refetchActionEntity(final User masterEntity) {
        return co$UserRolesUpdater.findByKey(masterEntity.getId());
    }
    
    @Override
    public Collection<UserRole> refetchAvailableItems(final User masterEntity) {
        return loadAvailableRoles(coUserRole);
    }
    
    @Override
    public UserRolesUpdater setAvailableItems(final UserRolesUpdater action, final Collection<UserRole> items) {
        return action.setRoles((Set<UserRole>) items);
    }
    
    @Override
    public Long persistedActionVersion(final Long masterEntityId) {
        return persistedActionVersionFor(masterEntityId, co$UserRolesUpdater);
    }
    
    private static Set<UserRole> loadAvailableRoles(final IUserRole coUserRole) {
        return new LinkedHashSet<>(coUserRole.findAll());
    }
    
}
