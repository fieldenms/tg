package ua.com.fielden.platform.security.user.ui_actions.producers;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractProducerForOpenEntityMasterAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.ui_actions.OpenUserMasterAction;
import ua.com.fielden.security.tokens.open_compound_master.OpenUserMasterAction_CanOpen_Token;

/**
 * A producer for new instances of entity {@link OpenUserMasterAction}.
 *
 * @author TG Team
 *
 */
public class OpenUserMasterActionProducer extends AbstractProducerForOpenEntityMasterAction<User, OpenUserMasterAction> {

    @Inject
    public OpenUserMasterActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, User.class, OpenUserMasterAction.class, companionFinder);
    }

    @Override
    @Authorise(OpenUserMasterAction_CanOpen_Token.class)
    protected OpenUserMasterAction provideDefaultValues(final OpenUserMasterAction openAction) {
        return super.provideDefaultValues(openAction);
    }
}
