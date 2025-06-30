package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;

/// Companion object for the {@link PersistentEntityInfo} entity.
///
public interface PersistentEntityInfoCo extends IEntityDao<PersistentEntityInfo> {

    /// Initializes the given instance of {@link PersistentEntityInfo} with metadata from the provided {@link AbstractEntity}.
    /// If the entity is not persistent or lacks versioning information (i.e., who and when it was created),
    /// an {@link InvalidStateException} will be thrown with an appropriate description.
    ///
    /// @param persistentEntity the {@link AbstractEntity} instance that serves as the source of metadata
    /// @param entity the {@link PersistentEntityInfo} instance to initialize
    /// @return the initialized `entity`
    PersistentEntityInfo initEntityWith(final AbstractEntity<?> persistentEntity, final PersistentEntityInfo entity);
}
