package ua.com.fielden.platform.sample.domain.compound.ui_actions.producers;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractProducerForOpenEntityMasterAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.master.menu.actions.TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem;
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
        if (masterEntityEmpty() && selectedEntitiesEmpty()) {
            // custom '+' action on entity centre, that is used in web tests for 'NEW case';
            // we deliberately provide NEWXX key for produced entity to be able to immediately save it (see https://github.com/fieldenms/tg/issues/1992)
            final TgCompoundEntity entity = co(entityType).new_();
            final int currentNEWXXentitiesCount = co(entityType).count(select(TgCompoundEntity.class).where().prop(KEY).like().val("NEW%").model());
            entity.setKey("NEW" + format("%02d", currentNEWXXentitiesCount));
            entity.setDesc(format("%s (%s detail)", entity.getKey(), entity.getKey()));
            openAction.setKey(entity);
            return openAction;
        } else {
            final OpenTgCompoundEntityMasterAction opener = super.provideDefaultValues(openAction);
            // custom prop action on entity centre's 'desc' property, that is used in web tests (tg-compound-master.html) for 'PERSISTED case';
            // we deliberately provide non-Main menu item to test different menu item activation on compound master opening
            if (chosenPropertyEqualsTo(DESC)) {
                opener.setMenuToOpen(TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem.class);
            }
            return opener;
        }
    }

}