package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.user.User;

/**
 * A producer for new instances of entity {@link User}.
 *
 * @author TG Team
 *
 */
public class UserProducer extends DefaultEntityProducerWithContext<User, User> implements IEntityProducer<User> {
    @Inject
    public UserProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final ITgEntityWithPropertyDependency coTgEntityWithPropertyDependency) {
        super(factory, User.class, companionFinder);
    }

    @Override
    protected User provideDefaultValues(final User entity) {
        return entity;
    }
}