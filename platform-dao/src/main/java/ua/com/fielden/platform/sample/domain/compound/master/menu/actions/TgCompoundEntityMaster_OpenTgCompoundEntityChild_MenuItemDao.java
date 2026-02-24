package ua.com.fielden.platform.sample.domain.compound.master.menu.actions;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.CanLeaveOptions;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChild;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess_Token;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/** 
 * DAO implementation for companion object {@link ITgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem}.
 * 
 * @author TG Team
 *
 */
@EntityType(TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem.class)
public class TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItemDao extends CommonEntityDao<TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem> implements ITgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem {

    private final IAuthorisationModel authModel;

    @Inject
    public TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItemDao(final IAuthorisationModel authModel) {
        this.authModel = authModel;
    }

    @Override
    @SessionRequired
    public TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem save(final TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem entity) {
        final Result authorisationResult = authModel.authorise(TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess_Token.class);
        if (entity.isClosing()) {
            if (!authorisationResult.isSuccessful()) {
                entity.setCanLeave(true);
            } else {
                final EntityResultQueryModel<TgCompoundEntityChild> query = select(TgCompoundEntityChild.class)
                        .where().prop("tgCompoundEntity").eq().val(entity.getKey()).model();
                boolean childExist = co(TgCompoundEntityChild.class).exists(query);
                entity.setCanLeave(!childExist);
                if (childExist) {
                    entity.setCannotLeaveReason("There are not completed items. Would you like to close this master?");
                    entity.setCloseInstructions("Please complete all tasks.");
                    entity.setCanLeaveOptions(CanLeaveOptions.Options.YES_NO.getCanLeaveOptions());
                }
            }
        } else if (!authorisationResult.isSuccessful()) {
            throw authorisationResult;
        }
        return super.save(entity);
    }

}