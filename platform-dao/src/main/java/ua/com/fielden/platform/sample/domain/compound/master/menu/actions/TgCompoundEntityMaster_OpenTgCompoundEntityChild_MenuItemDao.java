package ua.com.fielden.platform.sample.domain.compound.master.menu.actions;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess_Token;

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
        authModel.authorise(TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess_Token.class).ifFailure(Result::throwRuntime);
        entity.setCanLeave(true);
        return super.save(entity);
    }

}