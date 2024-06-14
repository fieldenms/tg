package ua.com.fielden.platform.sample.domain.compound.master.menu.actions;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.compound_master_menu.TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess_Token;

/** 
 * DAO implementation for companion object {@link ITgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem}.
 * 
 * @author TG Team
 *
 */
@EntityType(TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem.class)
public class TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItemDao extends CommonEntityDao<TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem> implements ITgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem {

    @Inject
    public TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItemDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem_CanAccess_Token.class)
    public TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem save(final TgCompoundEntityMaster_OpenTgCompoundEntityChild_MenuItem entity) {
        return super.save(entity);
    }

}