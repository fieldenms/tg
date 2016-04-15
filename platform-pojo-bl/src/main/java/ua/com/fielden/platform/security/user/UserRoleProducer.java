package ua.com.fielden.platform.security.user;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.EntityProducerWithNewEditActions;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.UserRoleSaveToken;

/**
 * A producer for new instances of entity {@link UserRole}.
 *
 * @author TG Team
 *
 */
public class UserRoleProducer extends EntityProducerWithNewEditActions<UserRole, UserRole> {
    
    @Inject
    public UserRoleProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, UserRole.class, companionFinder);
    }

    @Override
    @Authorise(UserRoleSaveToken.class)
    protected UserRole provideDefaultValuesForNewEntity(final UserRole entity) {
        return super.provideDefaultValuesForNewEntity(entity);
    }
    
    @Override
    @Authorise(UserRoleSaveToken.class)
    protected UserRole provideDefaultValues(final UserRole entity) {
        return super.provideDefaultValues(entity);
    }

}