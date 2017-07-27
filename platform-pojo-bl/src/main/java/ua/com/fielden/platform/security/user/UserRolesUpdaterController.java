package ua.com.fielden.platform.security.user;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAndInstrument;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IUserRole;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ICollectionModificationController;
import ua.com.fielden.platform.web.centre.CentreContext;

public class UserRolesUpdaterController implements ICollectionModificationController<User, UserRolesUpdater, Long> {
    private final IEntityDao<User> coUser;
    private final IEntityDao<UserRolesUpdater> coUserRolesUpdater;
    private final IUserRole coUserRole;
    
    public UserRolesUpdaterController(final IEntityDao<User> coUser, final IEntityDao<UserRolesUpdater> coUserRolesUpdater, final IUserRole coUserRole) {
        this.coUser = coUser;
        this.coUserRolesUpdater = coUserRolesUpdater;
        this.coUserRole = coUserRole;
    }
    
    @Override
    public AbstractEntity<?> getMasterEntityFromContext(final CentreContext<?, ?> context) {
        // this producer is suitable for property actions on User master and for actions on User centre
        return context.getMasterEntity() == null ? context.getCurrEntity() : context.getMasterEntity();
    }
    
    @Override
    public User refetchMasterEntity(final AbstractEntity<?> masterEntityFromContext) {
        return coUser.findById(masterEntityFromContext.getId(), coUser.getFetchProvider().with("roles").fetchModel());
    }
    
    @Override
    public UserRolesUpdater refetchActionEntity(final Long masterEntityId) {
        final UserRolesUpdater refetchedAction = coUserRolesUpdater.getEntity(
            from(select(UserRolesUpdater.class).where().prop(AbstractEntity.KEY).eq().val(masterEntityId).model())
            .with(fetchAndInstrument(UserRolesUpdater.class).with(AbstractEntity.KEY)/*.with("roles")*/)
            .model()
        );
        if (refetchedAction != null) {
            refetchedAction.setRoles(UserRolesUpdaterProducer.loadAvailableRoles(coUserRole));
        }
        return refetchedAction;
    }
}
