package ua.com.fielden.platform.sample.domain.compound.master.menu.actions;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityDetail;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem_CanAccess_Token;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.CanLeaveOptions.YES_NO;
import static ua.com.fielden.platform.entity.LeaveReason.CLOSED;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

/** 
 * DAO implementation for companion object {@link ITgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem}.
 * 
 * @author TG Team
 *
 */
@EntityType(TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem.class)
public class TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItemDao extends CommonEntityDao<TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem> implements ITgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem {

    private final IAuthorisationModel authModel;

    @Inject
    public TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItemDao(final IAuthorisationModel authModel) {
        this.authModel = authModel;
    }

    @Override
    @SessionRequired
    public TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem save(final TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem entity) {
        final Result authorisationResult = authModel.authorise(TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem_CanAccess_Token.class);
        if (entity.leaveReason().isPresent()) {
            if (!authorisationResult.isSuccessful()) {
                entity.setCanLeave(true);
            } else {
                final EntityResultQueryModel<TgCompoundEntityDetail> query = select(TgCompoundEntityDetail.class)
                        .where().prop("key").eq().val(entity.getKey()).model();
                final var optionalCompoundEntityDetails = co(TgCompoundEntityDetail.class).getEntityOptional(from(query).with(fetchKeyAndDescOnly(TgCompoundEntityDetail.class)).model());
                optionalCompoundEntityDetails.ifPresentOrElse(details -> {
                    if (details.getDesc().contains("desc")) {
                        entity.setCanLeave(false);
                        entity.setCannotLeaveReason(format("Description should not contain desc. Would you like to %s this master?", entity.leaveReason().get().equals(CLOSED) ? "close" : "leave"));
                        entity.setLeaveInstructions("Please remove desc from description.");
                        entity.setCanLeaveOptions(YES_NO);
                    } else {
                        entity.setCanLeave(true);
                    }
                }, () -> entity.setCanLeave(true));
            }
        } else if (!authorisationResult.isSuccessful()) {
            throw authorisationResult;
        }
        return super.save(entity);
    }

}
