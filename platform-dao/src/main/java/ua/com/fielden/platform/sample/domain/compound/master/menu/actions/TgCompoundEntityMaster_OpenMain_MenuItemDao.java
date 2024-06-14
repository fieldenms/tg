package ua.com.fielden.platform.sample.domain.compound.master.menu.actions;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenMain_MenuItem_CanAccess_Token;

/** 
 * DAO implementation for companion object {@link ITgCompoundEntityMaster_OpenMain_MenuItem}.
 * 
 * @author TG Team
 *
 */
@EntityType(TgCompoundEntityMaster_OpenMain_MenuItem.class)
public class TgCompoundEntityMaster_OpenMain_MenuItemDao extends CommonEntityDao<TgCompoundEntityMaster_OpenMain_MenuItem> implements ITgCompoundEntityMaster_OpenMain_MenuItem {

    @Inject
    public TgCompoundEntityMaster_OpenMain_MenuItemDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(TgCompoundEntityMaster_OpenMain_MenuItem_CanAccess_Token.class)
    public TgCompoundEntityMaster_OpenMain_MenuItem save(final TgCompoundEntityMaster_OpenMain_MenuItem entity) {
        return super.save(entity);
    }

}