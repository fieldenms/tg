package ua.com.fielden.platform.security.user.producers;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

/**
 * A producer for new instances of entity {@link UserAndRoleAssociation}.
 *
 * @author TG Team
 *
 */
public class UserAndRoleAssociationProducer extends DefaultEntityProducerWithContext<UserAndRoleAssociation> {

    @Inject
    public UserAndRoleAssociationProducer(final EntityFactory factory, final ICompanionObjectFinder coFinder) {
        super(factory, UserAndRoleAssociation.class, coFinder);
    }

    @Override
    protected UserAndRoleAssociation provideDefaultValuesForStandardNew(final UserAndRoleAssociation entityIn, final EntityNewAction masterEntity) {
        final UserAndRoleAssociation entityOut = super.provideDefaultValuesForStandardNew(entityIn, masterEntity);
        // This producer can be invoked from two places:
        // 1. Standalone centre
        // 2. Centre embedded in User Master
        // In the second case we want to default the user and make it read-only
        if (ofMasterEntity().keyOfMasterEntityInstanceOf(User.class)) {
            final User shallowUser = ofMasterEntity().keyOfMasterEntity(User.class);
            // shallowUser has been fetched in OpenUserMasterActionProducer with key and desc only
            // It needs to be re-fetched here using a slightly deeper fetch model, as appropriate for UserAndRoleAssociation
            entityOut.setUser(refetch(shallowUser, "user"));
            entityOut.getProperty("user").validationResult().ifFailure(Result::throwRuntime);
            entityOut.getProperty("user").setEditable(false);
        }
        return entityOut;
    }
}
