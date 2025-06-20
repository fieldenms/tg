package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.compound_master_menu.PersistentEntityInfoMaster_OpenMain_MenuItem_CanAccess_Token;

/// DAO implementation for companion object {@link PersistentEntityInfoMaster_OpenMain_MenuItem}.
///
@EntityType(PersistentEntityInfoMaster_OpenMain_MenuItem.class)
public class PersistentEntityInfoMaster_OpenMain_MenuItemDao extends CommonEntityDao<PersistentEntityInfoMaster_OpenMain_MenuItem> implements PersistentEntityInfoMaster_OpenMain_MenuItemCo {

    @Override
    @Authorise(PersistentEntityInfoMaster_OpenMain_MenuItem_CanAccess_Token.class)
    public PersistentEntityInfoMaster_OpenMain_MenuItem save(final PersistentEntityInfoMaster_OpenMain_MenuItem entity) {
        return super.save(entity);
    }
}
