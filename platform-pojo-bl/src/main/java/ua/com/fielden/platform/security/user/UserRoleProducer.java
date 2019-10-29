package ua.com.fielden.platform.security.user;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.UserRole_CanSave_Token;

/**
 * A producer for new instances of entity {@link UserRole}.
 *
 * @author TG Team
 *
 */
public class UserRoleProducer extends DefaultEntityProducerWithContext<UserRole> {
    
    @Inject
    public UserRoleProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, UserRole.class, companionFinder);
    }

    @Override
    @Authorise(UserRole_CanSave_Token.class)
    protected UserRole provideDefaultValuesForStandardNew(final UserRole entity, final EntityNewAction masterEntity) {
        return super.provideDefaultValuesForStandardNew(entity, masterEntity);
    }
    
    @Override
    @Authorise(UserRole_CanSave_Token.class)
    protected UserRole provideDefaultValuesForStandardEdit(final Long entityId, final EntityEditAction masterEntity) {
        return super.provideDefaultValuesForStandardEdit(entityId, masterEntity);
    }
}