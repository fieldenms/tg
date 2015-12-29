package ua.com.fielden.platform.sample.domain;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * A producer for new instances of entity {@link TgUpdateRolesAction}.
 *
 * @author TG Team
 *
 */
public class TgUpdateRolesActionProducer extends DefaultEntityProducerWithContext<TgUpdateRolesAction, TgUpdateRolesAction> implements IEntityProducer<TgUpdateRolesAction> {
    private final IUserRoleDao coUserRole;

    @Inject
    public TgUpdateRolesActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final IUserRoleDao coUserRole) {
        super(factory, TgUpdateRolesAction.class, companionFinder);
        this.coUserRole = coUserRole;
    }

    @Override
    protected TgUpdateRolesAction provideDefaultValues(final TgUpdateRolesAction entity) {
        entity.setKey("dummy");
        if (getCentreContext() != null) {
            entity.setContext(getCentreContext());

            final User me = (User) entity.getContext().getMasterEntity();
            if (me.isDirty()) {
                throw Result.failure("This action is applicable only to a saved entity! Please save entity and try again!");
            }
            
            final List<UserRole> allAvailableRoles = coUserRole.findAll();
            final Set<UserRole> roles = new LinkedHashSet<>(allAvailableRoles);
            entity.setRoles(roles);
            
            final Set<Long> chosenRoleIds = new LinkedHashSet<>();
            chosenRoleIds.add(64L);
            chosenRoleIds.add(2L);
            entity.setChosenRoleIds(chosenRoleIds);
        }
        
        return entity;
    }
}