package ua.com.fielden.platform.security.user;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.EntityProducerWithNewEditActions;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.UserSaveToken;

/**
 * A producer for new instances of entity {@link User}.
 *
 * @author TG Team
 *
 */
public class UserProducer extends EntityProducerWithNewEditActions<User> {
    
    @Inject
    public UserProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, User.class, companionFinder);
    }
    
    @Override
    @Authorise(UserSaveToken.class)
    protected User provideDefaultValuesForNewEntity(final User entity) {
        return super.provideDefaultValuesForNewEntity(entity);
    }
    
    @Override
    @Authorise(UserSaveToken.class)
    protected User provideDefaultValues(final User entity) {
        return super.provideDefaultValues(entity);
    }
}