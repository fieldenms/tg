package ua.com.fielden.platform.sample.domain.compound.master.menu.actions;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem_CanAccess_Token;

/** 
 * DAO implementation for companion object {@link ITgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem}.
 * 
 * @author TG Team
 *
 */
@EntityType(TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem.class)
public class TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItemDao extends CommonEntityDao<TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem> implements ITgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem {

    @Inject
    public TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItemDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem_CanAccess_Token.class)
    public TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem save(final TgCompoundEntityMaster_OpenTgCompoundEntityDetail_MenuItem entity) {
        return super.save(entity);
    }

}