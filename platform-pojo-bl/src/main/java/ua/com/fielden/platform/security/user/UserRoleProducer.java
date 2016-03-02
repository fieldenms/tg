package ua.com.fielden.platform.security.user;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link UserRole}.
 *
 * @author TG Team
 *
 */
public class UserRoleProducer extends DefaultEntityProducerWithContext<UserRole, UserRole> implements IEntityProducer<UserRole> {
    @Inject
    public UserRoleProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, UserRole.class, companionFinder);
    }

    @Override
    protected UserRole provideDefaultValues(final UserRole entity) {
        return entity;
    }
}