package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

/// DAO implementation for companion object {@link PersistentEntityInfoMaster_OpenMain_MenuItemCo}.
///
@EntityType(PersistentEntityInfoMaster_OpenMain_MenuItem.class)
public class PersistentEntityInfoMaster_OpenMain_MenuItemDao extends CommonEntityDao<PersistentEntityInfoMaster_OpenMain_MenuItem> implements PersistentEntityInfoMaster_OpenMain_MenuItemCo {

    @Override
    public PersistentEntityInfoMaster_OpenMain_MenuItem save(final PersistentEntityInfoMaster_OpenMain_MenuItem entity) {
        return super.save(entity);
    }
}
