package ua.com.fielden.platform.sample.domain.compound.ui_actions.producers;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractProducerForOpenEntityMasterAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.ui_actions.OpenTgCompoundEntityMasterAction;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.open_compound_master.OpenTgCompoundEntityMasterAction_CanOpen_Token;

/**
 * A producer for new instances of entity {@link OpenTgCompoundEntityMasterAction}.
 *
 * @author TG Team
 *
 */
public class OpenTgCompoundEntityMasterActionProducer extends AbstractProducerForOpenEntityMasterAction<TgCompoundEntity, OpenTgCompoundEntityMasterAction> {

    @Inject
    public OpenTgCompoundEntityMasterActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgCompoundEntity.class, OpenTgCompoundEntityMasterAction.class, companionFinder);
    }

    @Override
    @Authorise(OpenTgCompoundEntityMasterAction_CanOpen_Token.class)
    protected OpenTgCompoundEntityMasterAction provideDefaultValues(final OpenTgCompoundEntityMasterAction openAction) {
        return super.provideDefaultValues(openAction);
    }

}