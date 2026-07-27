package ua.com.fielden.platform.security.user.ui_actions.producers;

import com.google.inject.Inject;
import ua.com.fielden.platform.entity.AbstractProducerForOpenEntityMasterAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.master.menu.actions.UserMaster_OpenUserAndRoleAssociation_MenuItem;
import ua.com.fielden.platform.security.user.ui_actions.OpenUserMasterAction;
import ua.com.fielden.security.tokens.open_compound_master.OpenUserMasterAction_CanOpen_Token;

import static ua.com.fielden.platform.entity.AbstractEntity.KEY;

/// A producer for new instances of entity [OpenUserMasterAction].
///
public class OpenUserMasterActionProducer extends AbstractProducerForOpenEntityMasterAction<User, OpenUserMasterAction> {

    @Inject
    public OpenUserMasterActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, User.class, OpenUserMasterAction.class, companionFinder);
    }

    @Override
    @Authorise(OpenUserMasterAction_CanOpen_Token.class)
    protected OpenUserMasterAction provideDefaultValues(final OpenUserMasterAction openAction) {
        if (currentEntityInstanceOf(User.class)
            && (chosenPropertyEqualsTo(User.ACTIVE_ROLES) || chosenPropertyEqualsTo(User.INACTIVE_ROLES)))
        {
            openAction.setMenuToOpen(UserMaster_OpenUserAndRoleAssociation_MenuItem.class);
            openAction.setKey(refetch(currentEntity(User.class).getId(), User.class, KEY));
            return openAction;
        }
        return super.provideDefaultValues(openAction);
    }

}
