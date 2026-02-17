package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.utils.EntityUtils;

/// Companion object for the {@link PersistentEntityInfo} entity.
///
public interface PersistentEntityInfoCo extends IEntityDao<PersistentEntityInfo> {

    /// Initialises `info` from `entity`.
    /// If the type of `entity` does not satisfy [EntityUtils#isPersistentWithVersionData(Class)], an {@link InvalidStateException} will be thrown.
    ///
    /// @param entity  source of metadata
    /// @param info  instance to initialise
    /// @return  initialised `entity`
    ///
    PersistentEntityInfo initialise(final AbstractEntity<?> entity, final PersistentEntityInfo info);

}
