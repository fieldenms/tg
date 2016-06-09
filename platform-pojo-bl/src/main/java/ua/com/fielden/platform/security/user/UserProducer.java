package ua.com.fielden.platform.security.user;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityNewAction;
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
public class UserProducer extends DefaultEntityProducerWithContext<User> {
    
    @Inject
    public UserProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, User.class, companionFinder);
    }
    
    @Override
    @Authorise(UserSaveToken.class)
    protected User provideDefaultValuesForStandardNew(final User entity, final EntityNewAction masterEntity) {
        return super.provideDefaultValuesForStandardNew(entity, masterEntity);
    }
    
    @Override
    @Authorise(UserSaveToken.class)
    protected User provideDefaultValuesForStandardEdit(final Long entityId, final EntityEditAction masterEntity) {
        return super.provideDefaultValuesForStandardEdit(entityId, masterEntity);
    }
}